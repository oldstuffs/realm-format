package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

public interface RealmChunk {
  int@NotNull[] biomes();

  @NotNull
  ListTag entities();

  @NotNull
  CompoundTag heightMaps();

  int maxSection();

  int minSection();

  @NotNull
  RealmChunkSection@NotNull[] sections();

  @NotNull
  ListTag tileEntities();

  int x();

  int z();
}
