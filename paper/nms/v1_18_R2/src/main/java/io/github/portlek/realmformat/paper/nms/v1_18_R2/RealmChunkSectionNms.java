package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.realm.impl.RealmChunkSectionBase;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.Nullable;

final class RealmChunkSectionNms extends RealmChunkSectionBase {

  RealmChunkSectionNms(
    final byte@Nullable[] blocks,
    @Nullable final NibbleArray data,
    @Nullable final ListTag palette,
    final long@Nullable[] blockStates,
    @Nullable final NibbleArray blockLight,
    @Nullable final NibbleArray skyLight,
    @Nullable final CompoundTag blockStatesTag,
    @Nullable final CompoundTag biomeTag
  ) {
    super(blocks, data, palette, blockStates, blockLight, skyLight, blockStatesTag, biomeTag);
  }
}
