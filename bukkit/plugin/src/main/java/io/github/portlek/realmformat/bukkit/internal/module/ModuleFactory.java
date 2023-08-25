package io.github.portlek.realmformat.bukkit.internal.module;

import io.github.portlek.realmformat.bukkit.api.internal.module.Module;
import io.github.portlek.realmformat.bukkit.api.internal.module.ModuleContext;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pf4j.Plugin;
import org.pf4j.PluginFactory;
import org.pf4j.PluginWrapper;

@Log4j2
@SuppressWarnings("unchecked")
public final class ModuleFactory implements PluginFactory {

    @NotNull
    private final Path modulessPath;

    public ModuleFactory(@NotNull final Path modulesPath) {
        this.modulessPath = modulesPath;
    }

    @Nullable
    @Override
    public Plugin create(@NotNull final PluginWrapper pluginWrapper) {
        final String className = pluginWrapper.getDescriptor().getPluginClass();
        ModuleFactory.log.info("Creating instance for plugin '{}'...", className);
        final Class<? extends Module> cls;
        try {
            cls =
            (Class<? extends Module>) pluginWrapper.getPluginClassLoader().loadClass(className);
        } catch (final ClassNotFoundException e) {
            ModuleFactory.log.fatal(String.format("Class '%s' not found!", className), e);
            return null;
        }
        final int modifiers = cls.getModifiers();
        if (
            Modifier.isAbstract(modifiers) ||
            Modifier.isInterface(modifiers) ||
            !Module.class.isAssignableFrom(cls)
        ) {
            ModuleFactory.log.fatal("The plugin class '{}' is not valid!", className);
            return null;
        }
        final Path directory = this.modulessPath.resolve(cls.getSimpleName().replace("Module", ""));
        try {
            return (Module) MethodHandles
                .lookup()
                .findConstructor(cls, MethodType.methodType(void.class, ModuleContext.class))
                .invokeExact(new ModuleContext(pluginWrapper, directory));
        } catch (final Throwable e) {
            ModuleFactory.log.fatal(String.format("Cannot create '%s' module!", className), e);
        }
        return null;
    }
}
