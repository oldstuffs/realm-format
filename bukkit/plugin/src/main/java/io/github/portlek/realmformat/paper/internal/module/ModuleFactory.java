package io.github.portlek.realmformat.paper.internal.module;

import io.github.portlek.realmformat.paper.api.internal.module.Module;
import io.github.portlek.realmformat.paper.api.internal.module.ModuleContext;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;

@SuppressWarnings("unchecked")
public final class ModuleFactory implements PluginFactory {

    @NotNull
    private final Logger logger;

    @NotNull
    private final Path modulessPath;

    public ModuleFactory(@NotNull final Path modulesPath, @NotNull final Logger logger) {
        this.modulessPath = modulesPath;
        this.logger = logger;
    }

    @Nullable
    @Override
    public Plugin create(@NotNull final PluginWrapper pluginWrapper) {
        final String className = pluginWrapper.getDescriptor().getPluginClass();
        this.logger.info(String.format("Creating instance for plugin '%s'...", className));
        final Class<? extends Module> cls;
        try {
            cls =
            (Class<? extends Module>) pluginWrapper.getPluginClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            this.logger.log(Level.SEVERE, String.format("Class '%s' not found!", className), e);
            return null;
        }
        final int modifiers = cls.getModifiers();
        if (
            Modifier.isAbstract(modifiers) ||
            Modifier.isInterface(modifiers) ||
            !Module.class.isAssignableFrom(cls)
        ) {
            this.logger.severe(String.format("The plugin class '%s' is not valid!", className));
            return null;
        }
        final Path directory = this.modulessPath.resolve(cls.getSimpleName().replace("Module", ""));
        try {
            return (Module) MethodHandles
                .lookup()
                .findConstructor(cls, MethodType.methodType(void.class, ModuleContext.class))
                .invokeExact(new ModuleContext(pluginWrapper, directory));
        } catch (final Throwable e) {
            this.logger.log(
                    Level.SEVERE,
                    String.format("Cannot create '%s' module!", className),
                    e
                );
        }
        return null;
    }
}
