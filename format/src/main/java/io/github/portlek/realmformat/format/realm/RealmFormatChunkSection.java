package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.shiruka.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that represents {@link RealmFormatChunk}'s sections.
 */
public interface RealmFormatChunkSection {

  /**
   * Gets the biomes of the chunk section.
   *
   * @return biomes of the chunk section.
   *
   * @since 1
   */
  @NotNull
  CompoundTag biomes();

  /**
   * Gets the block light of the chunk section.
   *
   * @return block light of the chunk section.
   *
   * @since 1
   */
  @Nullable
  NibbleArray blockLight();

  /**
   * Gets the block states of the chunk section.
   *
   * @return block states of the chunk section.
   *
   * @since 1
   */
  @NotNull
  CompoundTag blockStates();

  /**
   * Gets the sky light of the chunk section.
   *
   * @return sky light of the chunk section.
   *
   * @since 1
   */
  @Nullable
  NibbleArray skyLight();
}
