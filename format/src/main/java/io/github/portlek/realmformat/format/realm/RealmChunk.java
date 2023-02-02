package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RealmChunk {
  int@Nullable [] biomes();

  @Nullable
  ListTag entities();

  @NotNull
  CompoundTag heightMaps();

  int maxSection();

  int minSection();

  @NotNull
  RealmChunkSection@NotNull[] sections();

  @Nullable
  ListTag tileEntities();

  int x();

  int z();
}
