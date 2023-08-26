package io.github.portlek.realmformat.bukkit;

import io.github.portlek.realmformat.bukkit.internal.module.ModuleFactory;
import io.github.portlek.realmformat.bukkit.internal.module.ModuleManager;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.pf4j.AbstractPluginManager;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

@Log4j2(topic = "RealmFormatModuleModule")
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
        System.setProperty(AbstractPluginManager.MODE_PROPERTY_NAME, "deployment");
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
