package io.github.portlek.realmformat.paper.upgrader.v1_16;

import io.github.portlek.realmformat.paper.upgrader.Upgrade;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.array.LongArrayTag;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Tag;

public class WorldUpgrade1_16 implements Upgrade {

  private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[] {
    0,
    1,
    28,
    2,
    29,
    14,
    24,
    3,
    30,
    22,
    20,
    15,
    25,
    17,
    4,
    8,
    31,
    27,
    13,
    23,
    21,
    19,
    16,
    7,
    26,
    12,
    18,
    6,
    11,
    5,
    10,
    9,
  };

  // Taken from DataConverterBitStorageAlign.java
  private static long[] addPadding(
    final int indices,
    final int bitsPerIndex,
    final long[] originalArray
  ) {
    final int k = originalArray.length;
    if (k == 0) {
      return originalArray;
    }
    final long l = (1L << bitsPerIndex) - 1L;
    final int i1 = 64 / bitsPerIndex;
    final int j1 = (indices + i1 - 1) / i1;
    final long[] along1 = new long[j1];
    int k1 = 0;
    int l1 = 0;
    long i2 = 0L;
    int j2 = 0;
    long k2 = originalArray[0];
    long l2 = k > 1 ? originalArray[1] : 0L;
    for (int i3 = 0; i3 < indices; ++i3) {
      final int j3 = i3 * bitsPerIndex;
      final int k3 = j3 >> 6;
      final int l3 = (i3 + 1) * bitsPerIndex - 1 >> 6;
      final int i4 = j3 ^ k3 << 6;
      if (k3 != j2) {
        k2 = l2;
        l2 = k3 + 1 < k ? originalArray[k3 + 1] : 0L;
        j2 = k3;
      }
      final long j4;
      int k4;
      if (k3 == l3) {
        j4 = k2 >>> i4 & l;
      } else {
        k4 = 64 - i4;
        j4 = (k2 >>> i4 | l2 << k4) & l;
      }
      k4 = l1 + bitsPerIndex;
      if (k4 >= 64) {
        along1[k1++] = i2;
        i2 = j4;
        l1 = bitsPerIndex;
      } else {
        i2 |= j4 << l1;
        l1 = k4;
      }
    }
    if (i2 != 0L) {
      along1[k1] = i2;
    }
    return along1;
  }

  private static int ceillog2(int input) {
    input =
      WorldUpgrade1_16.isPowerOfTwo(input)
        ? input
        : WorldUpgrade1_16.smallestEncompassingPowerOfTwo(input);
    return WorldUpgrade1_16.MULTIPLY_DE_BRUIJN_BIT_POSITION[(int) (
        (long) input * 125613361L >> 27
      ) &
      31];
  }

  private static boolean isPowerOfTwo(final int input) {
    return input != 0 && (input & input - 1) == 0;
  }

  private static int smallestEncompassingPowerOfTwo(final int input) {
    int result = input - 1;
    result |= result >> 1;
    result |= result >> 2;
    result |= result >> 4;
    result |= result >> 8;
    result |= result >> 16;
    return result + 1;
  }

  @Override
  public void upgrade(final SlimeLoadedWorld world) {
    for (final SlimeChunk chunk : new ArrayList<>(world.getChunks().values())) {
      // Add padding to height maps and block states
      final CompoundTag heightMaps = chunk.getHeightMaps();
      for (final Tag<?> map : heightMaps.getValue().values()) {
        if (map instanceof LongArrayTag arrayTag) {
          arrayTag.setValue(WorldUpgrade1_16.addPadding(256, 9, arrayTag.getValue()));
        }
      }
      for (int sectionIndex = 0; sectionIndex < chunk.getSections().length; sectionIndex++) {
        SlimeChunkSection section = chunk.getSections()[sectionIndex];
        if (section != null) {
          final int bitsPerBlock = Math.max(
            4,
            WorldUpgrade1_16.ceillog2(section.getPalette().getValue().size())
          );
          if (!WorldUpgrade1_16.isPowerOfTwo(bitsPerBlock)) {
            section =
              new CraftSlimeChunkSection(
                section.getPalette(),
                WorldUpgrade1_16.addPadding(4096, bitsPerBlock, section.getBlockStates()),
                null,
                null,
                section.getBlockLight(),
                section.getSkyLight()
              );
            chunk.getSections()[sectionIndex] = section;
          }
        }
      }
      // Update biome array size
      final int[] newBiomes = new int[1024];
      Arrays.fill(newBiomes, -1);
      final int[] biomes = chunk.getBiomes();
      System.arraycopy(biomes, 0, newBiomes, 0, biomes.length);
      world.updateChunk(
        new CraftSlimeChunk(
          chunk.getWorldName(),
          chunk.getX(),
          chunk.getZ(),
          chunk.getSections(),
          chunk.getHeightMaps(),
          newBiomes,
          chunk.getTileEntities(),
          chunk.getEntities(),
          0,
          16
        )
      );
    }
  }
}
