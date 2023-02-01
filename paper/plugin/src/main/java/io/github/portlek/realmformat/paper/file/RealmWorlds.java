package io.github.portlek.realmformat.paper.file;

import io.github.portlek.realmformat.paper.configurate.Config;
import io.github.portlek.realmformat.paper.misc.WorldData;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmWorlds implements Config {

  @NotNull
  private final ConfigurationLoader<?> loader;

  @Setting(nodeFromParent = true)
  private Map<String, WorldData> worlds = new HashMap<>();

  public RealmWorlds(@NotNull final ConfigurationLoader<?> loader) {
    this.loader = loader;
  }
}
