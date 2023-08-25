package io.github.portlek.realmformat.modules.mongo;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@Builder
@ConfigSerializable
public final class MongoCredential {

    @Setting
    @Builder.Default
    private String address = "localhost";

    @Setting
    @Builder.Default
    private String authSource = "admin";

    @Setting
    @Builder.Default
    private String collection = "realms";

    @Setting
    @Builder.Default
    private String database = "minecraft";

    @Setting
    @Builder.Default
    private boolean enabled = false;

    @Setting
    @Builder.Default
    private String password = "password";

    @Setting
    @Builder.Default
    private int port = 27017;

    @Setting
    private String uri;

    @Setting
    @Builder.Default
    private String username = "root";

    @NotNull
    public String parseUri() {
        return this.uri != null
            ? this.uri
            : "mongodb://" +
            this.parseAuthParams() +
            this.address +
            ":" +
            this.port +
            this.parseAuthSource();
    }

    @NotNull
    private String parseAuthParams() {
        return this.username == null || this.password == null
            ? ""
            : this.username + ":" + this.password + "@";
    }

    @NotNull
    private String parseAuthSource() {
        return this.authSource == null ? "" : "/?authSource=" + this.parseAuthParams();
    }
}
