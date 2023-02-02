package io.github.portlek.realmformat.paper.misc;

import io.github.portlek.realmformat.format.property.RealmProperties;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@Getter
@Builder
@ConfigSerializable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorldData {

  @Setting
  @Builder.Default
  private boolean allowAnimals = true;

  @Setting
  @Builder.Default
  private boolean allowMonsters = true;

  @Setting
  @Builder.Default
  private String dataSource = "file";

  @Setting
  @Builder.Default
  private String defaultBiome = "minecraft:plains";

  @Setting
  @Builder.Default
  private String difficulty = "peaceful";

  @Setting
  @Builder.Default
  private boolean dragonBattle = false;

  @Setting
  @Builder.Default
  private String environment = "NORMAL";

  @Setting
  @Builder.Default
  private boolean loadOnStartup = true;

  @Setting
  @Builder.Default
  private boolean pvp = true;

  @Setting
  @Builder.Default
  private boolean readOnly = false;

  @Setting
  @Builder.Default
  private Point3d spawn = Point3d.builder().x(0.5d).y(255.0d).z(0.5d).build();

  @Setting
  @Builder.Default
  private String worldType = "DEFAULT";

  @NotNull
  public RealmPropertyMap toPropertyMap() {
    try {
      Enum.valueOf(Difficulty.class, this.difficulty.toUpperCase());
    } catch (final IllegalArgumentException ex) {
      throw new IllegalArgumentException("unknown difficulty '" + this.difficulty + "'");
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
    propertyMap.setValue(RealmProperties.SPAWN_X, (int) this.spawn.x());
    propertyMap.setValue(RealmProperties.SPAWN_Y, (int) this.spawn.y());
    propertyMap.setValue(RealmProperties.SPAWN_Z, (int) this.spawn.z());
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
