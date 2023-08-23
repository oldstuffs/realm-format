package io.github.portlek.realmformat.paper.config;

import io.github.portlek.realmformat.paper.internal.config.Config;
import io.github.portlek.realmformat.paper.internal.config.MongoCredential;
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
        "Realm world loader settings.\n" +
        "You can specify how you want to load and save your realm worlds."
    )
    private Loaders loaders = new Loaders();

    public RealmFormatConfig(@NotNull final ConfigurationLoader<?> loader) {
        this.loader = loader;
    }

    @Getter
    @ConfigSerializable
    public static final class Loaders {

        @Setting
        @Comment(
            "Stores realm worlds in local this local folder.\n" +
            "This is a fallback loader which means enables whenever all the other loaders are disabled."
        )
        private String local = "realms";

        @Setting
        @Comment("Stores realm worlds in mongodb.")
        private MongoCredential mongo = MongoCredential.builder().build();
    }
}
