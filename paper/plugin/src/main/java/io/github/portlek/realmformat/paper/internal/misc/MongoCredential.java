package io.github.portlek.realmformat.paper.internal.misc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@Builder
@ConfigSerializable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MongoCredential {

  @Setting
  private String address;

  @Setting
  private String authSource;

  @Setting
  private String collection;

  @Setting
  private String database;

  @Setting
  private boolean enabled;

  @Setting
  private String password;

  @Setting
  private int port;

  @Setting
  private String uri;

  @Setting
  private String username;

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
