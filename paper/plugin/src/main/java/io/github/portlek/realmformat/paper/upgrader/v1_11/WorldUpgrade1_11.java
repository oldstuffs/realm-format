package io.github.portlek.realmformat.paper.upgrader.v1_11;

import io.github.portlek.realmformat.paper.upgrader.Upgrade;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.primitive.StringTag;
import java.util.HashMap;
import java.util.Map;

public class WorldUpgrade1_11 implements Upgrade {

  private static final Map<String, String> newToOldMap = new HashMap<>();

  private static final Map<String, String> oldToNewMap = new HashMap<>();

  static {
    WorldUpgrade1_11.rename("Furnace", "minecraft:furnace");
    WorldUpgrade1_11.rename("Chest", "minecraft:chest");
    WorldUpgrade1_11.rename("EnderChest", "minecraft:ender_chest");
    WorldUpgrade1_11.rename("RecordPlayer", "minecraft:jukebox");
    WorldUpgrade1_11.rename("Trap", "minecraft:dispenser");
    WorldUpgrade1_11.rename("Dropper", "minecraft:dropper");
    WorldUpgrade1_11.rename("Sign", "minecraft:sign");
    WorldUpgrade1_11.rename("MobSpawner", "minecraft:mob_spawner");
    WorldUpgrade1_11.rename("Music", "minecraft:noteblock");
    WorldUpgrade1_11.rename("Piston", "minecraft:piston");
    WorldUpgrade1_11.rename("Cauldron", "minecraft:brewing_stand");
    WorldUpgrade1_11.rename("EnchantTable", "minecraft:enchanting_table");
    WorldUpgrade1_11.rename("Airportal", "minecraft:end_portal");
    WorldUpgrade1_11.rename("Beacon", "minecraft:beacon");
    WorldUpgrade1_11.rename("Skull", "minecraft:skull");
    WorldUpgrade1_11.rename("DLDetector", "minecraft:daylight_detector");
    WorldUpgrade1_11.rename("Hopper", "minecraft:hopper");
    WorldUpgrade1_11.rename("Comparator", "minecraft:comparator");
    WorldUpgrade1_11.rename("FlowerPot", "minecraft:flower_pot");
    WorldUpgrade1_11.rename("Banner", "minecraft:banner");
    WorldUpgrade1_11.rename("Structure", "minecraft:structure_block");
    WorldUpgrade1_11.rename("EndGateway", "minecraft:end_gateway");
    WorldUpgrade1_11.rename("Control", "minecraft:command_block");
    WorldUpgrade1_11.rename(null, "minecraft:bed"); // Patch for issue s#62
  }

  private static void rename(final String oldName, final String newName) {
    if (oldName != null) {
      WorldUpgrade1_11.oldToNewMap.put(oldName, newName);
    }
    WorldUpgrade1_11.newToOldMap.put(newName, oldName);
  }

  @Override
  public void downgrade(final CraftSlimeWorld world) {
    for (final SlimeChunk chunk : world.getChunks().values()) {
      for (final CompoundTag entityTag : chunk.getTileEntities()) {
        final String oldType = entityTag.getAsStringTag("id").get().getValue();
        final String newType = WorldUpgrade1_11.newToOldMap.get(oldType);
        if (newType != null) {
          entityTag.getValue().put("id", new StringTag("id", newType));
        }
      }
    }
  }

  @Override
  public void upgrade(final CraftSlimeWorld world) {
    // 1.11 changed the way Tile Entities are named
    for (final SlimeChunk chunk : world.getChunks().values()) {
      for (final CompoundTag entityTag : chunk.getTileEntities()) {
        final String oldType = entityTag.getAsStringTag("id").get().getValue();
        final String newType = WorldUpgrade1_11.oldToNewMap.get(oldType);
        if (newType == null) {
          if (WorldUpgrade1_11.newToOldMap.containsKey(oldType)) { // Maybe it's in the new format for some reason?
            continue;
          }
          throw new IllegalStateException("Failed to find 1.11 upgrade for tile entity " + oldType);
        }
        entityTag.getValue().put("id", new StringTag("id", newType));
      }
    }
  }
}
