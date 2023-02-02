package io.github.portlek.realmformat.format.property;

import io.github.portlek.realmformat.format.property.type.RealmPropertyBoolean;
import io.github.portlek.realmformat.format.property.type.RealmPropertyInt;
import io.github.portlek.realmformat.format.property.type.RealmPropertyString;
import org.jetbrains.annotations.ApiStatus;

public interface RealmProperties {
  RealmProperty<Boolean> ALLOW_ANIMALS = new RealmPropertyBoolean("allowAnimals", true);

  RealmProperty<Boolean> ALLOW_MONSTERS = new RealmPropertyBoolean("allowMonsters", true);

  @ApiStatus.Experimental
  RealmProperty<String> CHUNK_PRUNING = new RealmPropertyString(
    "pruning",
    "aggressive",
    value -> value.equalsIgnoreCase("aggressive") || value.equalsIgnoreCase("never")
  );

  @ApiStatus.Experimental
  RealmProperty<Integer> CHUNK_SECTION_MAX = new RealmPropertyInt("chunkSectionMin", 19);

  @ApiStatus.Experimental
  RealmProperty<Integer> CHUNK_SECTION_MIN = new RealmPropertyInt("chunkSectionMin", -4);

  RealmProperty<String> DEFAULT_BIOME = new RealmPropertyString("defaultBiome", "minecraft:plains");

  RealmProperty<String> DIFFICULTY = new RealmPropertyString(
    "difficulty",
    "peaceful",
    value ->
      value.equalsIgnoreCase("peaceful") ||
      value.equalsIgnoreCase("easy") ||
      value.equalsIgnoreCase("normal") ||
      value.equalsIgnoreCase("hard")
  );

  RealmProperty<Boolean> DRAGON_BATTLE = new RealmPropertyBoolean("dragonBattle", false);

  RealmProperty<String> ENVIRONMENT = new RealmPropertyString(
    "environment",
    "normal",
    value ->
      value.equalsIgnoreCase("normal") ||
      value.equalsIgnoreCase("nether") ||
      value.equalsIgnoreCase("the_end")
  );

  RealmProperty<Boolean> PVP = new RealmPropertyBoolean("pvp", true);

  @ApiStatus.Experimental
  RealmProperty<Integer> SAVE_MAX_X = new RealmPropertyInt("saveMaxX", 0);

  @ApiStatus.Experimental
  RealmProperty<Integer> SAVE_MAX_Z = new RealmPropertyInt("saveMaxZ", 0);

  @ApiStatus.Experimental
  RealmProperty<Integer> SAVE_MIN_X = new RealmPropertyInt("saveMinX", 0);

  @ApiStatus.Experimental
  RealmProperty<Integer> SAVE_MIN_Z = new RealmPropertyInt("saveMinZ", 0);

  @ApiStatus.Experimental
  RealmProperty<Boolean> SHOULD_LIMIT_SAVE = new RealmPropertyBoolean("hasSaveBounds", false);

  RealmProperty<Integer> SPAWN_X = new RealmPropertyInt("spawnX", 0);

  RealmProperty<Integer> SPAWN_Y = new RealmPropertyInt("spawnY", 255);

  RealmProperty<Integer> SPAWN_Z = new RealmPropertyInt("spawnZ", 0);

  RealmProperty<String> WORLD_TYPE = new RealmPropertyString(
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
