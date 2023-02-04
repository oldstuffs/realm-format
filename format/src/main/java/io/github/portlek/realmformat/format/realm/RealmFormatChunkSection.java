package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import org.jetbrains.annotations.Nullable;

/**
 * An interface that represents {@link RealmFormatChunk}'s sections.
 */
public interface RealmFormatChunkSection {

  /**
   * Gets the block data of the chunk section.
   *
   * @return block data of the chunk section.
   *
   * @since 1
   */
  BlockDataV1_8 blockDataV1_8();

  /**
   * Gets the block data of the chunk section.
   *
   * @return block data of the chunk section.
   *
   * @since 1
   */
  BlockDataV1_13 blockDataV1_13();

  /**
   * Gets the block data of the chunk section.
   *
   * @return block data of the chunk section.
   *
   * @since 1
   */
  BlockDataV1_17 blockDataV1_17();

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
   * Gets the sky light of the chunk section.
   *
   * @return sky light of the chunk section.
   *
   * @since 1
   */
  @Nullable
  NibbleArray skyLight();
}
