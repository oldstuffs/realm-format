package io.github.portlek.realmformat.bukkit.config;

import io.github.portlek.realmformat.paper.api.internal.config.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmFormatConfig implements Config {

    @NotNull
    private final ConfigurationLoader<?> loader;

    @Setting
    @Comment(
        "Stores realm worlds in local this local folder.\n" +
        "This is a fallback loader which means enables whenever all the other loaders are disabled."
    )
    private String local = "realms";

    public RealmFormatConfig(@NotNull final ConfigurationLoader<?> loader) {
        this.loader = loader;
    }
}
