package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that represents {@link RealmFormatWorld}'s chunks.
 */
public interface RealmFormatChunk {
  /**
   * Gets the biomes of the chunk.
   *
   * @return biomes of the chunk.
   *
   * @since 1
   */
  int@Nullable[] biomes();

  /**
   * Gets the entities of the chunk.
   *
   * @return entities of the chunk.
   *
   * @since 1
   */
  ListTag entities();

  /**
   * Gets the height maps of the chunk.
   *
   * @return height maps of the chunk.
   *
   * @since 1
   */
  CompoundTag heightMaps();

  /**
   * Gets the max. section of the chunk.
   *
   * @return max. section of the chunk.
   *
   * @since 1
   */
  int maxSection();

  /**
   * Gets the min. section of the chunk.
   *
   * @return min. section of the chunk.
   *
   * @since 1
   */
  int minSection();

  /**
   * Gets the sections of the chunk.
   *
   * @return sections of the chunk.
   *
   * @since 1
   */
  RealmFormatChunkSection[] sections();

  /**
   * Gets the tile entities of the chunk.
   *
   * @return tile entities of the chunk.
   *
   * @since 1
   */
  ListTag tileEntities();

  /**
   * Gets the x coordinate of the chunk.
   *
   * @return x coordinate of the chunk.
   *
   * @since 1
   */
  int x();

  /**
   * Gets the z coordinate of the chunk.
   *
   * @return z coordinate of the chunk.
   *
   * @since 1
   */
  int z();
}
