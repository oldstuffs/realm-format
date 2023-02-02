package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.shiruka.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface RealmFormatChunkSection {
  @NotNull
  CompoundTag biomes();

  @NotNull
  NibbleArray blockLight();

  @NotNull
  CompoundTag blockStates();

  @NotNull
  NibbleArray skyLight();
}
