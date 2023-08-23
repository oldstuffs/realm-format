package io.github.portlek.realmformat.paper.config;

import io.github.portlek.realmformat.paper.internal.config.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmFormatMessages implements Config {

    @Setting
    private String reloadComplete = "&aReload complete, took %took%ms.";

    @NotNull
    private final ConfigurationLoader<?> loader;

    public RealmFormatMessages(@NotNull final ConfigurationLoader<?> loader) {
        this.loader = loader;
    }
}
