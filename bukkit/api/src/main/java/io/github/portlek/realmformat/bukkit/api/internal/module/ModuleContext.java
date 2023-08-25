package io.github.portlek.realmformat.bukkit.api.internal.module;

import java.nio.file.Path;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.pf4j.PluginWrapper;

@Getter
public final class ModuleContext {

    @NotNull
    private final Path dataFolder;

    @NotNull
    private final PluginWrapper wrapper;

    public ModuleContext(@NotNull final PluginWrapper wrapper, @NotNull final Path dataFolder) {
        this.wrapper = wrapper;
        this.dataFolder = dataFolder;
    }
}
