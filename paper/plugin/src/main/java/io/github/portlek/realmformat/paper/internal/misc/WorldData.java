package io.github.portlek.realmformat.paper.internal.misc;

import io.github.portlek.realmformat.format.property.RealmFormatProperties;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import java.util.Locale;
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
  public RealmFormatPropertyMap properties() {
    try {
      Enum.valueOf(Difficulty.class, this.difficulty.toUpperCase(Locale.ROOT));
    } catch (final IllegalArgumentException ex) {
      throw new IllegalArgumentException("unknown difficulty '" + this.difficulty + "'");
    }
    String environment = this.environment;
    try {
      Enum.valueOf(World.Environment.class, environment.toUpperCase(Locale.ROOT));
    } catch (final IllegalArgumentException ex) {
      try {
        final int envId = Integer.parseInt(environment);
        if (envId < -1 || envId > 1) {
          throw new NumberFormatException(environment);
        }
        environment = World.Environment.getEnvironment(envId).name();
      } catch (final NumberFormatException ex2) {
        throw new IllegalArgumentException("Unknown environment '" + this.environment + "'");
      }
    }
    final RealmFormatPropertyMap propertyMap = new RealmFormatPropertyMap();
    propertyMap.setValue(RealmFormatProperties.SPAWN_X, (int) this.spawn.x());
    propertyMap.setValue(RealmFormatProperties.SPAWN_Y, (int) this.spawn.y());
    propertyMap.setValue(RealmFormatProperties.SPAWN_Z, (int) this.spawn.z());
    propertyMap.setValue(RealmFormatProperties.DIFFICULTY, this.difficulty);
    propertyMap.setValue(RealmFormatProperties.ALLOW_MONSTERS, this.allowMonsters);
    propertyMap.setValue(RealmFormatProperties.ALLOW_ANIMALS, this.allowAnimals);
    propertyMap.setValue(RealmFormatProperties.DRAGON_BATTLE, this.dragonBattle);
    propertyMap.setValue(RealmFormatProperties.PVP, this.pvp);
    propertyMap.setValue(RealmFormatProperties.ENVIRONMENT, environment);
    propertyMap.setValue(RealmFormatProperties.WORLD_TYPE, this.worldType);
    propertyMap.setValue(RealmFormatProperties.DEFAULT_BIOME, this.defaultBiome);
    return propertyMap;
  }
}
