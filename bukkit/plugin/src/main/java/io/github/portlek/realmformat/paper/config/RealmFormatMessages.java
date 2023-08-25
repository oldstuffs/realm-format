package io.github.portlek.realmformat.paper.config;

import io.github.portlek.realmformat.paper.api.internal.config.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmFormatMessages implements Config {

    @NotNull
    private final ConfigurationLoader<?> loader;

    @Setting
    private final String reloadComplete = "&aReload complete, took %took%ms.";

    public RealmFormatMessages(@NotNull final ConfigurationLoader<?> loader) {
        this.loader = loader;
    }
}
