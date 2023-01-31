package io.github.portlek.realmformat.paper.file;

import io.github.portlek.realmformat.paper.configurate.Config;
import io.github.portlek.realmformat.paper.misc.MongoCredential;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class RealmFormatConfig implements Config {

  @NotNull
  final ConfigurationLoader<?> loader;

  @Setting
  MongoCredential mongo = MongoCredential
    .builder()
    .address("localhost")
    .port(27017)
    .database("minecraft")
    .username("root")
    .password("root123")
    .build();

  public RealmFormatConfig(@NotNull final ConfigurationLoader<?> loader) {
    this.loader = loader;
  }
}
