package io.github.portlek.realmformat.modules.redis;

import io.github.portlek.realmformat.bukkit.api.internal.module.Module;
import io.github.portlek.realmformat.bukkit.api.internal.module.ModuleContext;
import org.jetbrains.annotations.NotNull;

public final class RedisModule extends Module {

    public RedisModule(@NotNull final ModuleContext context) {
        super(context);
    }

    @Override
    protected void enable() {}
}
