package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.shiruka.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RealmFormatChunkSection {
  @NotNull
  CompoundTag biomes();

  @Nullable
  NibbleArray blockLight();

  @NotNull
  CompoundTag blockStates();

  @Nullable
  NibbleArray skyLight();
}
