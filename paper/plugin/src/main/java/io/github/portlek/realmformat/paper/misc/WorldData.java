package io.github.portlek.realmformat.paper.misc;

import io.github.portlek.realmformat.format.property.RealmProperties;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import lombok.Data;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Data
@ConfigSerializable
public final class WorldData {

  @Setting
  private boolean allowAnimals = true;

  @Setting
  private boolean allowMonsters = true;

  @Setting
  private String dataSource = "file";

  @Setting
  private String defaultBiome = "minecraft:plains";

  @Setting
  private String difficulty = "peaceful";

  @Setting
  private boolean dragonBattle = false;

  @Setting
  private String environment = "NORMAL";

  @Setting
  private boolean loadOnStartup = true;

  @Setting
  private boolean pvp = true;

  @Setting
  private boolean readOnly = false;

  @Setting
  private String spawn = "0.5, 255, 0.5";

  @Setting
  private String worldType = "DEFAULT";

  @NotNull
  public RealmPropertyMap toPropertyMap() {
    try {
      Enum.valueOf(Difficulty.class, this.difficulty.toUpperCase());
    } catch (final IllegalArgumentException ex) {
      throw new IllegalArgumentException("unknown difficulty '" + this.difficulty + "'");
    }
    final var spawnLocationSplit = this.spawn.split(", ");
    final double spawnX;
    final double spawnY;
    final double spawnZ;
    try {
      spawnX = Double.parseDouble(spawnLocationSplit[0]);
      spawnY = Double.parseDouble(spawnLocationSplit[1]);
      spawnZ = Double.parseDouble(spawnLocationSplit[2]);
    } catch (final NumberFormatException | ArrayIndexOutOfBoundsException ex) {
      throw new IllegalArgumentException("invalid spawn location '" + this.spawn + "'");
    }
    var environment = this.environment;
    try {
      Enum.valueOf(World.Environment.class, environment.toUpperCase());
    } catch (final IllegalArgumentException ex) {
      try {
        final var envId = Integer.parseInt(environment);
        if (envId < -1 || envId > 1) {
          throw new NumberFormatException(environment);
        }
        environment = World.Environment.getEnvironment(envId).name();
      } catch (final NumberFormatException ex2) {
        throw new IllegalArgumentException("unknown environment '" + this.environment + "'");
      }
    }
    final var propertyMap = new RealmPropertyMap();
    propertyMap.setValue(RealmProperties.SPAWN_X, (int) spawnX);
    propertyMap.setValue(RealmProperties.SPAWN_Y, (int) spawnY);
    propertyMap.setValue(RealmProperties.SPAWN_Z, (int) spawnZ);
    propertyMap.setValue(RealmProperties.DIFFICULTY, this.difficulty);
    propertyMap.setValue(RealmProperties.ALLOW_MONSTERS, this.allowMonsters);
    propertyMap.setValue(RealmProperties.ALLOW_ANIMALS, this.allowAnimals);
    propertyMap.setValue(RealmProperties.DRAGON_BATTLE, this.dragonBattle);
    propertyMap.setValue(RealmProperties.PVP, this.pvp);
    propertyMap.setValue(RealmProperties.ENVIRONMENT, environment);
    propertyMap.setValue(RealmProperties.WORLD_TYPE, this.worldType);
    propertyMap.setValue(RealmProperties.DEFAULT_BIOME, this.defaultBiome);
    return propertyMap;
  }
}
