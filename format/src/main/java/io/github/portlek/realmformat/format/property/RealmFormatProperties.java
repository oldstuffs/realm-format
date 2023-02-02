package io.github.portlek.realmformat.format.property;

import io.github.portlek.realmformat.format.property.type.RealmFormatPropertyBoolean;
import io.github.portlek.realmformat.format.property.type.RealmFormatPropertyInt;
import io.github.portlek.realmformat.format.property.type.RealmFormatPropertyString;
import org.jetbrains.annotations.ApiStatus;

public interface RealmFormatProperties {
  RealmFormatProperty<Boolean> ALLOW_ANIMALS = new RealmFormatPropertyBoolean("allowAnimals", true);

  RealmFormatProperty<Boolean> ALLOW_MONSTERS = new RealmFormatPropertyBoolean(
    "allowMonsters",
    true
  );

  @ApiStatus.Experimental
  RealmFormatProperty<String> CHUNK_PRUNING = new RealmFormatPropertyString(
    "pruning",
    "aggressive",
    value -> value.equalsIgnoreCase("aggressive") || value.equalsIgnoreCase("never")
  );

  @ApiStatus.Experimental
  RealmFormatProperty<Integer> CHUNK_SECTION_MAX = new RealmFormatPropertyInt(
    "chunkSectionMin",
    19
  );

  @ApiStatus.Experimental
  RealmFormatProperty<Integer> CHUNK_SECTION_MIN = new RealmFormatPropertyInt(
    "chunkSectionMin",
    -4
  );

  RealmFormatProperty<String> DEFAULT_BIOME = new RealmFormatPropertyString(
    "defaultBiome",
    "minecraft:plains"
  );

  RealmFormatProperty<String> DIFFICULTY = new RealmFormatPropertyString(
    "difficulty",
    "peaceful",
    value ->
      value.equalsIgnoreCase("peaceful") ||
      value.equalsIgnoreCase("easy") ||
      value.equalsIgnoreCase("normal") ||
      value.equalsIgnoreCase("hard")
  );

  RealmFormatProperty<Boolean> DRAGON_BATTLE = new RealmFormatPropertyBoolean(
    "dragonBattle",
    false
  );

  RealmFormatProperty<String> ENVIRONMENT = new RealmFormatPropertyString(
    "environment",
    "normal",
    value ->
      value.equalsIgnoreCase("normal") ||
      value.equalsIgnoreCase("nether") ||
      value.equalsIgnoreCase("the_end")
  );

  RealmFormatProperty<Boolean> PVP = new RealmFormatPropertyBoolean("pvp", true);

  @ApiStatus.Experimental
  RealmFormatProperty<Integer> SAVE_MAX_X = new RealmFormatPropertyInt("saveMaxX", 0);

  @ApiStatus.Experimental
  RealmFormatProperty<Integer> SAVE_MAX_Z = new RealmFormatPropertyInt("saveMaxZ", 0);

  @ApiStatus.Experimental
  RealmFormatProperty<Integer> SAVE_MIN_X = new RealmFormatPropertyInt("saveMinX", 0);

  @ApiStatus.Experimental
  RealmFormatProperty<Integer> SAVE_MIN_Z = new RealmFormatPropertyInt("saveMinZ", 0);

  @ApiStatus.Experimental
  RealmFormatProperty<Boolean> SHOULD_LIMIT_SAVE = new RealmFormatPropertyBoolean(
    "hasSaveBounds",
    false
  );

  RealmFormatProperty<Integer> SPAWN_X = new RealmFormatPropertyInt("spawnX", 0);

  RealmFormatProperty<Integer> SPAWN_Y = new RealmFormatPropertyInt("spawnY", 255);

  RealmFormatProperty<Integer> SPAWN_Z = new RealmFormatPropertyInt("spawnZ", 0);

  RealmFormatProperty<String> WORLD_TYPE = new RealmFormatPropertyString(
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
