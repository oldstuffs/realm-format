package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

public interface RealmFormatChunk {
  @NotNull
  ListTag tileEntities();

  @NotNull
  ListTag entities();

  @NotNull
  CompoundTag heightMaps();

  RealmFormatChunkSection@NotNull[] sections();

  int x();

  int z();
}
