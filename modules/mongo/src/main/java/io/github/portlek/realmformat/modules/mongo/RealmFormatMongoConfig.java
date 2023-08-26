package io.github.portlek.realmformat.modules.mongo;

import io.github.portlek.realmformat.bukkit.api.internal.config.Config;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmFormatMongoConfig implements Config {

    @Setting(nodeFromParent = true)
    private MongoCredential credential = MongoCredential.builder().build();

    @NotNull
    private final ConfigurationLoader<?> loader;

    public RealmFormatMongoConfig(@NotNull final ConfigurationLoader<?> loader) {
        this.loader = loader;
    }
}
