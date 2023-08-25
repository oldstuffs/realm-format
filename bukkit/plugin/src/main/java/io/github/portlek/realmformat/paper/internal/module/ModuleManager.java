package io.github.portlek.realmformat.paper.internal.module;

import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginFactory;

public final class ModuleManager extends JarPluginManager {

    @NotNull
    private final ModuleFactory factory;

    public ModuleManager(@NotNull final Path pluginsRoots, @NotNull final ModuleFactory factory) {
        super(pluginsRoots);
        this.factory = factory;
        super.initialize();
    }

    @Override
    protected PluginFactory createPluginFactory() {
        return this.factory;
    }

    @Override
    protected void initialize() {}
}
