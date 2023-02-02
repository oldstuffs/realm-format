package io.github.portlek.realmformat.paper.upgrader.v1_18;

import com.google.common.collect.ImmutableMap;
import io.github.portlek.realmformat.paper.upgrader.Upgrade;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.array.LongArrayTag;
import io.github.shiruka.nbt.primitive.IntTag;
import io.github.shiruka.nbt.primitive.StringTag;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayList;
import java.util.List;

public class WorldUpgrade1_18 implements Upgrade {

  public static final ImmutableMap<String, String> BIOME_UPDATE = ImmutableMap
    .<String, String>builder()
    .put("minecraft:badlands_plateau", "minecraft:badlands")
    .put("minecraft:bamboo_jungle_hills", "minecraft:bamboo_jungle")
    .put("minecraft:birch_forest_hills", "minecraft:birch_forest")
    .put("minecraft:dark_forest_hills", "minecraft:dark_forest")
    .put("minecraft:desert_hills", "minecraft:desert")
    .put("minecraft:desert_lakes", "minecraft:desert")
    .put("minecraft:giant_spruce_taiga_hills", "minecraft:old_growth_spruce_taiga")
    .put("minecraft:giant_spruce_taiga", "minecraft:old_growth_spruce_taiga")
    .put("minecraft:giant_tree_taiga_hills", "minecraft:old_growth_pine_taiga")
    .put("minecraft:giant_tree_taiga", "minecraft:old_growth_pine_taiga")
    .put("minecraft:gravelly_mountains", "minecraft:windswept_gravelly_hills")
    .put("minecraft:jungle_edge", "minecraft:sparse_jungle")
    .put("minecraft:jungle_hills", "minecraft:jungle")
    .put("minecraft:modified_badlands_plateau", "minecraft:badlands")
    .put("minecraft:modified_gravelly_mountains", "minecraft:windswept_gravelly_hills")
    .put("minecraft:modified_jungle_edge", "minecraft:sparse_jungle")
    .put("minecraft:modified_jungle", "minecraft:jungle")
    .put("minecraft:modified_wooded_badlands_plateau", "minecraft:wooded_badlands")
    .put("minecraft:mountain_edge", "minecraft:windswept_hills")
    .put("minecraft:mountains", "minecraft:windswept_hills")
    .put("minecraft:mushroom_field_shore", "minecraft:mushroom_fields")
    .put("minecraft:shattered_savanna", "minecraft:windswept_savanna")
    .put("minecraft:shattered_savanna_plateau", "minecraft:windswept_savanna")
    .put("minecraft:snowy_mountains", "minecraft:snowy_plains")
    .put("minecraft:snowy_taiga_hills", "minecraft:snowy_taiga")
    .put("minecraft:snowy_taiga_mountains", "minecraft:snowy_taiga")
    .put("minecraft:snowy_tundra", "minecraft:snowy_plains")
    .put("minecraft:stone_shore", "minecraft:stony_shore")
    .put("minecraft:swamp_hills", "minecraft:swamp")
    .put("minecraft:taiga_hills", "minecraft:taiga")
    .put("minecraft:taiga_mountains", "minecraft:taiga")
    .put("minecraft:tall_birch_forest", "minecraft:old_growth_birch_forest")
    .put("minecraft:tall_birch_hills", "minecraft:old_growth_birch_forest")
    .put("minecraft:wooded_badlands_plateau", "minecraft:wooded_badlands")
    .put("minecraft:wooded_hills", "minecraft:forest")
    .put("minecraft:wooded_mountains", "minecraft:windswept_forest")
    .put("minecraft:lofty_peaks", "minecraft:jagged_peaks")
    .put("minecraft:snowcapped_peaks", "minecraft:frozen_peaks")
    .build();

  private static final String[] BIOMES_BY_ID = new String[256]; // rip datapacks

  static {
    WorldUpgrade1_18.BIOMES_BY_ID[0] = "minecraft:ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[1] = "minecraft:plains";
    WorldUpgrade1_18.BIOMES_BY_ID[2] = "minecraft:desert";
    WorldUpgrade1_18.BIOMES_BY_ID[3] = "minecraft:mountains";
    WorldUpgrade1_18.BIOMES_BY_ID[4] = "minecraft:forest";
    WorldUpgrade1_18.BIOMES_BY_ID[5] = "minecraft:taiga";
    WorldUpgrade1_18.BIOMES_BY_ID[6] = "minecraft:swamp";
    WorldUpgrade1_18.BIOMES_BY_ID[7] = "minecraft:river";
    WorldUpgrade1_18.BIOMES_BY_ID[8] = "minecraft:nether_wastes";
    WorldUpgrade1_18.BIOMES_BY_ID[9] = "minecraft:the_end";
    WorldUpgrade1_18.BIOMES_BY_ID[10] = "minecraft:frozen_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[11] = "minecraft:frozen_river";
    WorldUpgrade1_18.BIOMES_BY_ID[12] = "minecraft:snowy_tundra";
    WorldUpgrade1_18.BIOMES_BY_ID[13] = "minecraft:snowy_mountains";
    WorldUpgrade1_18.BIOMES_BY_ID[14] = "minecraft:mushroom_fields";
    WorldUpgrade1_18.BIOMES_BY_ID[15] = "minecraft:mushroom_field_shore";
    WorldUpgrade1_18.BIOMES_BY_ID[16] = "minecraft:beach";
    WorldUpgrade1_18.BIOMES_BY_ID[17] = "minecraft:desert_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[18] = "minecraft:wooded_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[19] = "minecraft:taiga_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[20] = "minecraft:mountain_edge";
    WorldUpgrade1_18.BIOMES_BY_ID[21] = "minecraft:jungle";
    WorldUpgrade1_18.BIOMES_BY_ID[22] = "minecraft:jungle_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[23] = "minecraft:jungle_edge";
    WorldUpgrade1_18.BIOMES_BY_ID[24] = "minecraft:deep_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[25] = "minecraft:stone_shore";
    WorldUpgrade1_18.BIOMES_BY_ID[26] = "minecraft:snowy_beach";
    WorldUpgrade1_18.BIOMES_BY_ID[27] = "minecraft:birch_forest";
    WorldUpgrade1_18.BIOMES_BY_ID[28] = "minecraft:birch_forest_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[29] = "minecraft:dark_forest";
    WorldUpgrade1_18.BIOMES_BY_ID[30] = "minecraft:snowy_taiga";
    WorldUpgrade1_18.BIOMES_BY_ID[31] = "minecraft:snowy_taiga_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[32] = "minecraft:giant_tree_taiga";
    WorldUpgrade1_18.BIOMES_BY_ID[33] = "minecraft:giant_tree_taiga_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[34] = "minecraft:wooded_mountains";
    WorldUpgrade1_18.BIOMES_BY_ID[35] = "minecraft:savanna";
    WorldUpgrade1_18.BIOMES_BY_ID[36] = "minecraft:savanna_plateau";
    WorldUpgrade1_18.BIOMES_BY_ID[37] = "minecraft:badlands";
    WorldUpgrade1_18.BIOMES_BY_ID[38] = "minecraft:wooded_badlands_plateau";
    WorldUpgrade1_18.BIOMES_BY_ID[39] = "minecraft:badlands_plateau";
    WorldUpgrade1_18.BIOMES_BY_ID[40] = "minecraft:small_end_islands";
    WorldUpgrade1_18.BIOMES_BY_ID[41] = "minecraft:end_midlands";
    WorldUpgrade1_18.BIOMES_BY_ID[42] = "minecraft:end_highlands";
    WorldUpgrade1_18.BIOMES_BY_ID[43] = "minecraft:end_barrens";
    WorldUpgrade1_18.BIOMES_BY_ID[44] = "minecraft:warm_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[45] = "minecraft:lukewarm_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[46] = "minecraft:cold_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[47] = "minecraft:deep_warm_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[48] = "minecraft:deep_lukewarm_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[49] = "minecraft:deep_cold_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[50] = "minecraft:deep_frozen_ocean";
    WorldUpgrade1_18.BIOMES_BY_ID[127] = "minecraft:the_void";
    WorldUpgrade1_18.BIOMES_BY_ID[129] = "minecraft:sunflower_plains";
    WorldUpgrade1_18.BIOMES_BY_ID[130] = "minecraft:desert_lakes";
    WorldUpgrade1_18.BIOMES_BY_ID[131] = "minecraft:gravelly_mountains";
    WorldUpgrade1_18.BIOMES_BY_ID[132] = "minecraft:flower_forest";
    WorldUpgrade1_18.BIOMES_BY_ID[133] = "minecraft:taiga_mountains";
    WorldUpgrade1_18.BIOMES_BY_ID[134] = "minecraft:swamp_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[140] = "minecraft:ice_spikes";
    WorldUpgrade1_18.BIOMES_BY_ID[149] = "minecraft:modified_jungle";
    WorldUpgrade1_18.BIOMES_BY_ID[151] = "minecraft:modified_jungle_edge";
    WorldUpgrade1_18.BIOMES_BY_ID[155] = "minecraft:tall_birch_forest";
    WorldUpgrade1_18.BIOMES_BY_ID[156] = "minecraft:tall_birch_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[157] = "minecraft:dark_forest_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[158] = "minecraft:snowy_taiga_mountains";
    WorldUpgrade1_18.BIOMES_BY_ID[160] = "minecraft:giant_spruce_taiga";
    WorldUpgrade1_18.BIOMES_BY_ID[161] = "minecraft:giant_spruce_taiga_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[162] = "minecraft:modified_gravelly_mountains";
    WorldUpgrade1_18.BIOMES_BY_ID[163] = "minecraft:shattered_savanna";
    WorldUpgrade1_18.BIOMES_BY_ID[164] = "minecraft:shattered_savanna_plateau";
    WorldUpgrade1_18.BIOMES_BY_ID[165] = "minecraft:eroded_badlands";
    WorldUpgrade1_18.BIOMES_BY_ID[166] = "minecraft:modified_wooded_badlands_plateau";
    WorldUpgrade1_18.BIOMES_BY_ID[167] = "minecraft:modified_badlands_plateau";
    WorldUpgrade1_18.BIOMES_BY_ID[168] = "minecraft:bamboo_jungle";
    WorldUpgrade1_18.BIOMES_BY_ID[169] = "minecraft:bamboo_jungle_hills";
    WorldUpgrade1_18.BIOMES_BY_ID[170] = "minecraft:soul_sand_valley";
    WorldUpgrade1_18.BIOMES_BY_ID[171] = "minecraft:crimson_forest";
    WorldUpgrade1_18.BIOMES_BY_ID[172] = "minecraft:warped_forest";
    WorldUpgrade1_18.BIOMES_BY_ID[173] = "minecraft:basalt_deltas";
    WorldUpgrade1_18.BIOMES_BY_ID[174] = "minecraft:dripstone_caves";
    WorldUpgrade1_18.BIOMES_BY_ID[175] = "minecraft:lush_caves";
    WorldUpgrade1_18.BIOMES_BY_ID[177] = "minecraft:meadow";
    WorldUpgrade1_18.BIOMES_BY_ID[178] = "minecraft:grove";
    WorldUpgrade1_18.BIOMES_BY_ID[179] = "minecraft:snowy_slopes";
    WorldUpgrade1_18.BIOMES_BY_ID[180] = "minecraft:snowcapped_peaks";
    WorldUpgrade1_18.BIOMES_BY_ID[181] = "minecraft:lofty_peaks";
    WorldUpgrade1_18.BIOMES_BY_ID[182] = "minecraft:stony_peaks";
  }

  public static int ceilLog2(final int value) {
    return value == 0 ? 0 : Integer.SIZE - Integer.numberOfLeadingZeros(value - 1); // see doc of numberOfLeadingZeros
  }

  private static CompoundTag createBiomeSection(
    final int[] biomes,
    final int offset,
    final int mask
  ) {
    final Int2IntLinkedOpenHashMap paletteId = new Int2IntLinkedOpenHashMap();
    for (int idx = 0; idx < 64; ++idx) {
      final int biome = biomes[offset + (idx & mask)];
      paletteId.putIfAbsent(biome, paletteId.size());
    }
    final List<StringTag> paletteString = new ArrayList<>();
    for (final IntIterator iterator = paletteId.keySet().iterator(); iterator.hasNext(); ) {
      final int biomeId = iterator.nextInt();
      String biome = biomeId >= 0 && biomeId < WorldUpgrade1_18.BIOMES_BY_ID.length ? WorldUpgrade1_18.BIOMES_BY_ID[biomeId] : null;
      final String update = WorldUpgrade1_18.BIOME_UPDATE.get(biome);
      if (update != null) {
        biome = update;
      }
      paletteString.add(new StringTag("", biome == null ? "minecraft:plains" : biome));
    }
    final int bitsPerObject = WorldUpgrade1_18.ceilLog2(paletteString.size());
    if (bitsPerObject == 0) {
      return WorldUpgrade1_18.wrapPalette(new ListTag<>("", TagType.TAG_STRING, paletteString), null);
    }
    // manually create packed integer data
    final int objectsPerValue = 64 / bitsPerObject;
    final long[] packed = new long[(64 + objectsPerValue - 1) / objectsPerValue];
    int shift = 0;
    int idx = 0;
    long curr = 0;
    for (int biome_idx = 0; biome_idx < 64; ++biome_idx) {
      final int biome = biomes[offset + (biome_idx & mask)];
      curr |= (long) paletteId.get(biome) << shift;
      shift += bitsPerObject;
      if (shift + bitsPerObject > 64) { // will next write overflow?
        // must move to next idx
        packed[idx++] = curr;
        shift = 0;
        curr = 0L;
      }
    }
    // don't forget to write the last one
    if (shift != 0) {
      packed[idx] = curr;
    }
    return WorldUpgrade1_18.wrapPalette(new ListTag<>("", TagType.TAG_STRING, paletteString), packed);
  }

  private static CompoundTag[] createBiomeSections(
    final int[] biomes,
    final boolean wantExtendedHeight,
    final int minSection
  ) {
    final CompoundTag[] ret = new CompoundTag[wantExtendedHeight ? 24 : 16];
    if (biomes != null && biomes.length == 1536) { // magic value for 24 sections of biomes (24 * 4^3)
      //isAlreadyExtended.setValue(true);
      for (int sectionIndex = 0; sectionIndex < 24; ++sectionIndex) {
        ret[sectionIndex] = WorldUpgrade1_18.createBiomeSection(biomes, sectionIndex * 64, -1); // -1 is all 1s
      }
    } else if (biomes != null && biomes.length == 1024) { // magic value for 24 sections of biomes (16 * 4^3)
      for (int sectionY = 0; sectionY < 16; ++sectionY) {
        ret[sectionY - minSection] = WorldUpgrade1_18.createBiomeSection(biomes, sectionY * 64, -1); // -1 is all 1s
      }
      //            if (wantExtendedHeight) {
      //                // must set the new sections at top and bottom
      //                final MapType<String> bottomCopy = createBiomeSection(biomes, 0, 15); // just want the biomes at y = 0
      //                final MapType<String> topCopy = createBiomeSection(biomes, 1008, 15); // just want the biomes at y = 252
      //
      //                for (int sectionIndex = 0; sectionIndex < 4; ++sectionIndex) {
      //                    ret[sectionIndex] = bottomCopy.copy(); // copy palette so that later possible modifications don't trash all sections
      //                }
      //
      //                for (int sectionIndex = 20; sectionIndex < 24; ++sectionIndex) {
      //                    ret[sectionIndex] = topCopy.copy(); // copy palette so that later possible modifications don't trash all sections
      //                }
      //            }
    } else {
      final ArrayList<StringTag> palette = new ArrayList<>();
      palette.add(new StringTag("", "minecraft:plains"));
      for (int i = 0; i < ret.length; ++i) {
        ret[i] = WorldUpgrade1_18.wrapPalette(new ListTag<>("", TagType.TAG_STRING, palette).clone(), null); // copy palette so that later possible modifications don't trash all sections
      }
    }
    return ret;
  }

  private static CompoundTag wrapPalette(final ListTag<?> palette, final long[] blockStates) {
    final CompoundMap map = new CompoundMap();
    final CompoundTag tag = new CompoundTag("", map);
    map.put(new ListTag<>("palette", palette.getElementType(), palette.getValue()));
    if (blockStates != null) {
      map.put(new LongArrayTag("data", blockStates));
    }
    return tag;
  }

  @Override
  public void upgrade(final SlimeLoadedWorld world) {
    for (final SlimeChunk chunk : new ArrayList<>(world.getChunks().values())) {
      // SpawnerSpawnDataFix
      for (final CompoundTag tileEntity : chunk.getTileEntities()) {
        final CompoundMap value = tileEntity.getValue();
        final Optional<String> id = tileEntity.getStringValue("id");
        if (id.equals(Optional.of("minecraft:mob_spawner"))) {
          final Optional<ListTag<?>> spawnPotentials = tileEntity.getAsListTag("SpawnPotentials");
          final Optional<CompoundTag> spawnData = tileEntity.getAsCompoundTag("SpawnData");
          if (spawnPotentials.isPresent()) {
            final ListTag<CompoundTag> spawnPotentialsList = (ListTag<CompoundTag>) spawnPotentials.get();
            final List<CompoundTag> spawnPotentialsListValue = spawnPotentialsList.getValue();
            for (final CompoundTag spawnPotentialsTag : spawnPotentialsListValue) {
              final CompoundMap spawnPotentialsValue = spawnPotentialsTag.getValue();
              final Optional<Integer> weight = spawnPotentialsTag.getIntValue("Weight");
              if (weight.isPresent()) {
                final int weightVal = weight.get();
                spawnPotentialsValue.remove("Weight");
                spawnPotentialsValue.put("weight", new IntTag("weight", weightVal));
              }
              final Optional<CompoundTag> entity = spawnPotentialsTag.getAsCompoundTag("Entity");
              if (entity.isPresent()) {
                final CompoundTag entityTag = entity.get();
                spawnPotentialsValue.remove("Entity");
                entityTag.getValue();
                final CompoundMap dataMap = new CompoundMap();
                dataMap.put(new CompoundTag("entity", entityTag.getValue()));
                spawnPotentialsValue.put("data", new CompoundTag("data", dataMap));
              }
            }
            value.put("SpawnPotentials", spawnPotentialsList);
            if (!spawnPotentialsListValue.isEmpty()) {
              final CompoundTag compoundTag = spawnPotentialsListValue.get(0);
              final CompoundTag entityTag = compoundTag
                .getAsCompoundTag("data")
                .get()
                .getAsCompoundTag("entity")
                .get();
              final CompoundMap spawnDataMap = new CompoundMap();
              spawnDataMap.put(entityTag.clone());
              value.put("SpawnData", new CompoundTag("SpawnData", spawnDataMap));
            }
          } else if (spawnData.isPresent()) {
            final CompoundTag spawnDataTag = spawnData.get();
            final CompoundMap spawnDataValue = spawnDataTag.getValue();
            final Optional<CompoundTag> entityTag = spawnDataTag.getAsCompoundTag("entity");
            final Optional<StringTag> idTag = spawnDataTag.getAsStringTag("id");
            if (entityTag.isEmpty() && idTag.isPresent()) {
              final StringTag entityTypeTag = idTag.get();
              spawnDataValue.remove("id");
              final CompoundMap entityMap = new CompoundMap();
              entityMap.put(entityTypeTag);
              spawnDataValue.put("entity", new CompoundTag("entity", entityMap));
              value.put("SpawnData", spawnDataTag);
            }
          }
        }
      }
      final CompoundTag[] tags = WorldUpgrade1_18.createBiomeSections(chunk.getBiomes(), false, 0);
      final SlimeChunkSection[] sections = chunk.getSections();
      for (int i = 0; i < sections.length; i++) {
        final SlimeChunkSection section = sections[i];
        if (section == null) {
          continue;
        }
        ((CraftSlimeChunkSection) section).setBlockStatesTag(
          WorldUpgrade1_18.wrapPalette(section.getPalette(), section.getBlockStates())
        );
        ((CraftSlimeChunkSection) section).setBiomeTag(tags[i]);
      }
      final SlimeChunkSection[] shiftedSections = new SlimeChunkSection[sections.length + 4];
      System.arraycopy(sections, 0, shiftedSections, 4, sections.length);
      ((CraftSlimeChunk) chunk).setSections(shiftedSections); // Shift all sections up 4
    }
  }
}
