package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.Nullable;

public interface RealmChunkSection {
  @Nullable
  CompoundTag biomeTag();

  @Nullable
  NibbleArray blockLight();

  long@Nullable[] blockStates();

  @Nullable
  CompoundTag blockStatesTag();

  byte@Nullable[] blocks();

  @Nullable
  NibbleArray data();

  @Nullable
  ListTag palette();

  @Nullable
  NibbleArray skyLight();
}
