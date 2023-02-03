package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that represents {@link RealmFormatWorld}'s chunks.
 */
public interface RealmFormatChunk {

  /**
   * Gets the entities of the chunk.
   *
   * @return entities of the chunk.
   *
   * @since 1
   */
  @NotNull
  ListTag entities();

  /**
   * Gets the height maps of the chunk.
   *
   * @return height maps of the chunk.
   *
   * @since 1
   */
  @NotNull
  CompoundTag heightMaps();

  /**
   * Gets the sections of the chunk.
   *
   * @return sections of the chunk.
   *
   * @since 1
   */
  RealmFormatChunkSection @NotNull [] sections();

  /**
   * Gets the tile entities of the chunk.
   *
   * @return tile entities of the chunk.
   *
   * @since 1
   */
  @NotNull
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
