package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.paper.internal.module.ModuleFactory;
import io.github.portlek.realmformat.paper.internal.module.ModuleManager;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

@Log4j2
final class RealmFormatModuleModule implements TerminableModule {

    @NotNull
    private final Path dataFolder;

    RealmFormatModuleModule(@NotNull final Path dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public void setup(@NotNull final TerminableConsumer consumer) {
        final Path modulesFolder = this.dataFolder.resolve("modules");
        final ModuleManager moduleManager = new ModuleManager(
            modulesFolder,
            new ModuleFactory(modulesFolder)
        );
        System.setProperty("pf4j.mode", "deployment");
        try {
            if (Files.notExists(modulesFolder)) {
                Files.createDirectories(modulesFolder);
            }
        } catch (final Exception e) {
            RealmFormatModuleModule.log.fatal("Failed to create modules directory!", e);
        }
        moduleManager.loadPlugins();
        moduleManager.startPlugins();
        consumer.bind(() -> {
            moduleManager.stopPlugins();
            moduleManager.unloadPlugins();
        });
    }
}
