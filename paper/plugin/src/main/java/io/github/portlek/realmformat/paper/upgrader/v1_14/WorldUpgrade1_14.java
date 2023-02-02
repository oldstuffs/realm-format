package io.github.portlek.realmformat.paper.upgrader.v1_14;

import io.github.portlek.realmformat.format.realm.RealmChunk;
import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.paper.upgrader.Upgrade;
import io.github.shiruka.nbt.ContainerTag;
import io.github.shiruka.nbt.Tag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public final class WorldUpgrade1_14 implements Upgrade {

  private static final Map<String, String> NEW_TO_OLD_MAP = new HashMap<>();

  private static final Map<String, String> OLD_TO_NEW_MAP = new HashMap<>();

  private static final int[] VILLAGER_XP = {0, 10, 50, 100, 150};

  static {
    WorldUpgrade1_14.rename("minecraft:tube_coral_fan", "minecraft:tube_coral_wall_fan");
    WorldUpgrade1_14.rename("minecraft:brain_coral_fan", "minecraft:brain_coral_wall_fan");
    WorldUpgrade1_14.rename("minecraft:bubble_coral_fan", "minecraft:bubble_coral_wall_fan");
    WorldUpgrade1_14.rename("minecraft:fire_coral_fan", "minecraft:fire_coral_wall_fan");
    WorldUpgrade1_14.rename("minecraft:horn_coral_fan", "minecraft:horn_coral_wall_fan");
    WorldUpgrade1_14.rename("minecraft:stone_slab", "minecraft:smooth_stone_slab");
    WorldUpgrade1_14.rename("minecraft:sign", "minecraft:oak_sign");
    WorldUpgrade1_14.rename("minecraft:wall_sign", "minecraft:oak_wall_sign");
  }

  private static int clamp(final int i, final int i1, final int i2) {
    return i < i1 ? i1 : Math.min(i, i2);
  }

  private static void rename(final String oldName, final String newName) {
    WorldUpgrade1_14.OLD_TO_NEW_MAP.put(oldName, newName);
    WorldUpgrade1_14.NEW_TO_OLD_MAP.put(newName, oldName);
  }

  private static void updateBlockEntities(@NotNull final RealmChunk chunk, final int sectionIndex, final int paletteIndex,
                                          @NotNull final String oldName, @NotNull final String newName) {
    final var tileEntities = chunk.tileEntities();
    if (tileEntities == null) {
      return;
    }
    final var section = chunk.sections()[sectionIndex];
    final var blockData = section.blockStates();
    if (blockData == null) {
      return;
    }
    final var bitsPerBlock = Math.max(4, blockData.length * 64 / 4096);
    final var maxEntryValue = (1L << bitsPerBlock) - 1;
    for (var y = 0; y < 16; y++) {
      for (var z = 0; z < 16; z++) {
        for (var x = 0; x < 16; x++) {
          final var arrayIndex = y << 8 | z << 4 | x;
          final var bitIndex = arrayIndex * bitsPerBlock;
          final var startIndex = bitIndex / 64;
          final var endIndex = ((arrayIndex + 1) * bitsPerBlock - 1) / 64;
          final var startBitSubIndex = bitIndex % 64;
          final int val;
          if (startIndex == endIndex) {
            val = (int) (blockData[startIndex] >>> startBitSubIndex & maxEntryValue);
          } else {
            final var endBitSubIndex = 64 - startBitSubIndex;
            val = (int) ((blockData[startIndex] >>> startBitSubIndex | blockData[endIndex] << endBitSubIndex) & maxEntryValue);
          }
          if (val == paletteIndex) {
            final var blockX = x + chunk.x() * 16;
            final var blockY = y + sectionIndex * 16;
            final var blockZ = z + chunk.z() * 16;
            for (final var tileEntityTag : tileEntities) {
              final var compoundTag = tileEntityTag.asCompound();
              final int tileX = compoundTag.getInteger("x").get();
              final int tileY = compoundTag.getInteger("y").get();
              final int tileZ = compoundTag.getInteger("z").get();
              if (tileX == blockX && tileY == blockY && tileZ == blockZ) {
                final var type = compoundTag.getString("id").get();
                if (!type.equals(oldName)) {
                  throw new IllegalStateException("Expected block entity to be " + oldName + ", not " + type);
                }
                compoundTag.setString("id", newName);
                break;
              }
            }
          }
        }
      }
    }
  }

  @NotNull
  private static String villagerProfession(final int profession, final int career) {
    return profession == 0 ? career == 2 ? "minecraft:fisherman" : career == 3 ? "minecraft:shepherd" : career == 4 ? "minecraft:fletcher" : "minecraft:farmer"
      : profession == 1 ? career == 2 ? "minecraft:cartographer" : "minecraft:librarian" : profession == 2 ? "minecraft:cleric" :
      profession == 3 ? career == 2 ? "minecraft:weaponsmith" : career == 3 ? "minecraft:toolsmith" : "minecraft:armorer" :
        profession == 4 ? career == 2 ? "minecraft:leatherworker" : "minecraft:butcher" : profession == 5 ? "minecraft:nitwit" : "minecraft:none";
  }

  private static int @NotNull [] villagerProfession(@NotNull final String profession) {
    return switch (profession) {
      case "minecraft:farmer" -> new int[]{0, 1};
      case "minecraft:fisherman" -> new int[]{0, 2};
      case "minecraft:shepherd" -> new int[]{0, 3};
      case "minecraft:fletcher" -> new int[]{0, 4};
      case "minecraft:librarian" -> new int[]{1, 1};
      case "minecraft:cartographer" -> new int[]{1, 2};
      case "minecraft:cleric" -> new int[]{2, 1};
      case "minecraft:armorer" -> new int[]{3, 1};
      case "minecraft:weaponsmith" -> new int[]{3, 2};
      case "minecraft:toolsmith" -> new int[]{3, 3};
      case "minecraft:butcher" -> new int[]{4, 1};
      case "minecraft:leatherworker" -> new int[]{4, 2};
      case "minecraft:nitwit" -> new int[]{5, 1};
      default -> new int[]{0, 0};
    };
  }

  @Override
  public void upgrade(@NotNull final RealmWorld world) {
    for (final var chunk : new ArrayList<>(world.chunks().values())) {
      for (var sectionIndex = 0; sectionIndex < chunk.sections().length; sectionIndex++) {
        final var section = chunk.sections()[sectionIndex];
        if (section != null) {
          final var palette = section.palette();
          if (palette != null) {
            for (var paletteIndex = 0; paletteIndex < palette.size(); paletteIndex++) {
              final var blockTag = palette.get(paletteIndex).get().asCompound();
              final var name = blockTag.getString("Name").get();
              if (name.equals("minecraft:trapped_chest")) {
                WorldUpgrade1_14.updateBlockEntities(chunk, sectionIndex, paletteIndex, "minecraft:chest", "minecraft:trapped_chest");
              }
              final var newName = WorldUpgrade1_14.OLD_TO_NEW_MAP.get(name);
              if (newName != null) {
                blockTag.setString("Name", newName);
              }
            }
          }
        }
      }
      final var entities = chunk.entities();
      if (entities != null) {
        for (final var entityTag : entities) {
          final var compoundTag = entityTag.asCompound();
          final var type = compoundTag.getString("id").get();
          switch (type) {
            case "minecraft:ocelot" -> {
              final int catType = compoundTag.getInteger("CatType").orElse(0);
              if (catType == 0) {
                final var owner = compoundTag.getString("Owner");
                final var ownerId = compoundTag.getString("OwnerUUID");
                if (owner.isPresent() || ownerId.isPresent()) {
                  compoundTag.setByte("Trusting", (byte) 1);
                }
                compoundTag.remove("CatType");
              } else if (catType > 0 && catType < 4) {
                compoundTag.setString("id", "minecraft:cat");
              }
            }
            case "minecraft:villager", "minecraft:zombie_villager" -> {
              final int profession = compoundTag.getInteger("Profession").orElse(0);
              final int career = compoundTag.getInteger("Career").orElse(0);
              int careerLevel = compoundTag.getInteger("CareerLevel").orElse(1);
              final var offersOpt = compoundTag.getCompoundTag("Offers");
              if (offersOpt.isPresent()) {
                if (careerLevel == 0 || careerLevel == 1) {
                  final int amount = offersOpt.flatMap(offers -> offers.getCompoundTag("Recipes")).map(ContainerTag::size).orElse(0);
                  careerLevel = WorldUpgrade1_14.clamp(amount / 2, 1, 5);
                }
              }
              final var xp = compoundTag.getCompoundTag("Xp");
              if (xp.isEmpty()) {
                compoundTag.setInteger("Xp", WorldUpgrade1_14.VILLAGER_XP[WorldUpgrade1_14.clamp(careerLevel - 1, 0, WorldUpgrade1_14.VILLAGER_XP.length - 1)]);
              }
              compoundTag.remove("Profession");
              compoundTag.remove("Career");
              compoundTag.remove("CareerLevel");
              final var dataMap = Tag.createCompound();
              dataMap.setString("type", "minecraft:plains");
              dataMap.setString("profession", WorldUpgrade1_14.villagerProfession(profession, career));
              dataMap.setInteger("level", careerLevel);
              compoundTag.set("VillagerData", dataMap);
            }
            case "minecraft:banner" -> {
              final var customName = compoundTag.getString("CustomName");
              if (customName.isPresent()) {
                final var newName = customName.get().replace("\"translate\":\"block.minecraft.illager_banner\"",
                  "\"translate\":\"block.minecraft.ominous_banner\"");
                compoundTag.setString("CustomName", newName);
              }
            }
          }
        }
      }
    }
  }
}
