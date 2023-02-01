package io.github.portlek.realmformat.format.realm.impl;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.realm.RealmChunkSection;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
public abstract class RealmChunkSectionBase implements RealmChunkSection {

  @Nullable
  private final CompoundTag biomeTag;

  @Nullable
  private final NibbleArray blockLight;

  private final long@Nullable[] blockStates;

  @Nullable
  private final CompoundTag blockStatesTag;

  private final byte@Nullable[] blocks;

  @Nullable
  private final NibbleArray data;

  @Nullable
  private final ListTag palette;

  @Nullable
  private final NibbleArray skyLight;

  protected RealmChunkSectionBase(
    final byte@Nullable[] blocks,
    @Nullable final NibbleArray data,
    @Nullable final ListTag palette,
    final long@Nullable[] blockStates,
    @Nullable final NibbleArray blockLight,
    @Nullable final NibbleArray skyLight,
    @Nullable final CompoundTag blockStatesTag,
    @Nullable final CompoundTag biomeTag
  ) {
    this.blocks = blocks;
    this.data = data;
    this.palette = palette;
    this.blockStates = blockStates;
    this.blockLight = blockLight;
    this.skyLight = skyLight;
    this.blockStatesTag = blockStatesTag;
    this.biomeTag = biomeTag;
  }
}