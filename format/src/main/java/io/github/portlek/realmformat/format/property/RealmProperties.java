package io.github.portlek.realmformat.format.property;

import io.github.portlek.realmformat.format.property.type.RealmPropertyBoolean;
import io.github.portlek.realmformat.format.property.type.RealmPropertyInt;
import io.github.portlek.realmformat.format.property.type.RealmPropertyString;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;

@UtilityClass
public class RealmProperties {

  public final RealmProperty<Boolean> ALLOW_ANIMALS = new RealmPropertyBoolean(
    "allowAnimals",
    true
  );

  public final RealmProperty<Boolean> ALLOW_MONSTERS = new RealmPropertyBoolean(
    "allowMonsters",
    true
  );

  public static final RealmProperty<String> DEFAULT_BIOME = new RealmPropertyString(
    "defaultBiome",
    "minecraft:plains"
  );

  public final RealmProperty<String> DIFFICULTY = new RealmPropertyString(
    "difficulty",
    "peaceful",
    value ->
      value.equalsIgnoreCase("peaceful") ||
      value.equalsIgnoreCase("easy") ||
      value.equalsIgnoreCase("normal") ||
      value.equalsIgnoreCase("hard")
  );

  public final RealmProperty<Boolean> DRAGON_BATTLE = new RealmPropertyBoolean(
    "dragonBattle",
    false
  );

  public final RealmProperty<String> ENVIRONMENT = new RealmPropertyString(
    "environment",
    "normal",
    value ->
      value.equalsIgnoreCase("normal") ||
      value.equalsIgnoreCase("nether") ||
      value.equalsIgnoreCase("the_end")
  );

  public final RealmProperty<Boolean> PVP = new RealmPropertyBoolean("pvp", true);

  @ApiStatus.Experimental
  public final RealmProperty<Integer> SAVE_MAX_X = new RealmPropertyInt("saveMaxX", 0);

  @ApiStatus.Experimental
  public final RealmProperty<Integer> SAVE_MAX_Z = new RealmPropertyInt("saveMaxZ", 0);

  @ApiStatus.Experimental
  public final RealmProperty<Integer> SAVE_MIN_X = new RealmPropertyInt("saveMinX", 0);

  @ApiStatus.Experimental
  public final RealmProperty<Integer> SAVE_MIN_Z = new RealmPropertyInt("saveMinZ", 0);

  @ApiStatus.Experimental
  public final RealmProperty<Boolean> SHOULD_LIMIT_SAVE = new RealmPropertyBoolean(
    "hasSaveBounds",
    false
  );

  public final RealmProperty<Integer> SPAWN_X = new RealmPropertyInt("spawnX", 0);

  public final RealmProperty<Integer> SPAWN_Y = new RealmPropertyInt("spawnY", 255);

  public final RealmProperty<Integer> SPAWN_Z = new RealmPropertyInt("spawnZ", 0);

  public final RealmProperty<String> WORLD_TYPE = new RealmPropertyString(
    "worldtype",
    "default",
    value ->
      value.equalsIgnoreCase("default") ||
      value.equalsIgnoreCase("flat") ||
      value.equalsIgnoreCase("large_biomes") ||
      value.equalsIgnoreCase("amplified") ||
      value.equalsIgnoreCase("customized") ||
      value.equalsIgnoreCase("debug_all_block_states") ||
      value.equalsIgnoreCase("default_1_1")
  );
}
