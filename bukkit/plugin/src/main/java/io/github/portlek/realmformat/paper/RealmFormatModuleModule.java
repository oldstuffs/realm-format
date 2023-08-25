package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.paper.internal.module.ModuleFactory;
import io.github.portlek.realmformat.paper.internal.module.ModuleManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

final class RealmFormatModuleModule implements TerminableModule {

    @NotNull
    private final Path dataFolder;

    @NotNull
    private final Logger logger;

    RealmFormatModuleModule(@NotNull final Path dataFolder, @NotNull final Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    @Override
    public void setup(@NotNull final TerminableConsumer consumer) {
        final Path modulesFolder = this.dataFolder.resolve("modules");
        final ModuleManager moduleManager = new ModuleManager(
            modulesFolder,
            new ModuleFactory(modulesFolder, this.logger)
        );
        System.setProperty("pf4j.mode", "deployment");
        try {
            if (Files.notExists(modulesFolder)) {
                Files.createDirectories(modulesFolder);
            }
        } catch (final Exception e) {
            this.logger.log(Level.SEVERE, "Failed to create modules directory!", e);
        }
        moduleManager.loadPlugins();
        moduleManager.startPlugins();
        consumer.bind(() -> {
            moduleManager.stopPlugins();
            moduleManager.unloadPlugins();
        });
    }
}
