package io.github.portlek.realmformat.paper.misc;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@Builder
@ConfigSerializable
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MongoCredential {

  @Setting
  String address;

  @Setting
  String authSource;

  @Setting
  String database;

  @Setting
  String password;

  @Setting
  int port;

  @Setting
  String uri;

  @Setting
  String username;

  private MongoCredential(
    final String address,
    final String authSource,
    final String database,
    final String password,
    final int port,
    final String uri,
    final String username
  ) {
    this.address = address;
    this.authSource = authSource;
    this.database = database;
    this.password = password;
    this.port = port;
    this.uri = uri;
    this.username = username;
  }

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
