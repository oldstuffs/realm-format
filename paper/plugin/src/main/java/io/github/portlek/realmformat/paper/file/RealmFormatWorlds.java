package io.github.portlek.realmformat.paper.file;

import io.github.portlek.realmformat.paper.internal.configurate.Config;
import io.github.portlek.realmformat.paper.internal.configurate.Configs;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import io.github.portlek.realmformat.paper.internal.misc.WorldData;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@ConfigSerializable
public final class RealmFormatWorlds implements Config {

  @NotNull
  private final ConfigurationLoader<?> loader = Configs.yaml(
    Services.load(Path.class).resolve("worlds.yaml")
  );

  @Setting(nodeFromParent = true)
  private Map<String, WorldData> worlds = new HashMap<>();
}
