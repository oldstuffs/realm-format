package io.github.portlek.realmformat.paper.api.loader;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@Builder
public final class MongoCredential {

    private final String address;

    private final String authSource;

    private final String collection;

    private final String database;

    private final boolean enabled;

    private final String password;

    private final int port;

    private final String uri;

    private final String username;

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
