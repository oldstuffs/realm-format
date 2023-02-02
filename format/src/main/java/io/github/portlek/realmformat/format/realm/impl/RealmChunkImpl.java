package io.github.portlek.realmformat.format.realm.impl;

import io.github.portlek.realmformat.format.realm.RealmChunk;
import io.github.portlek.realmformat.format.realm.RealmChunkSection;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
public final class RealmChunkImpl implements RealmChunk {

  private final int@Nullable[] biomes;

  @Nullable
  private final ListTag entities;

  @NotNull
  private final CompoundTag heightMaps;

  private final int maxSection;

  private final int minSection;

  @NotNull
  private final RealmChunkSection@NotNull[] sections;

  @Nullable
  private final ListTag tileEntities;

  @Nullable
  private final CompoundTag upgradeData;

  private final int x;

  private final int z;

  public RealmChunkImpl(
    final int x,
    final int z,
    @NotNull final RealmChunkSection@NotNull[] sections,
    @NotNull final CompoundTag heightMaps,
    final int@Nullable[] biomes,
    @Nullable final ListTag tileEntities,
    @Nullable final ListTag entities,
    final int minSection,
    final int maxSection,
    @Nullable final CompoundTag upgradeData
  ) {
    this.x = x;
    this.z = z;
    this.sections = sections;
    this.heightMaps = heightMaps;
    this.biomes = biomes;
    this.tileEntities = tileEntities;
    this.entities = entities;
    this.minSection = minSection;
    this.maxSection = maxSection;
    this.upgradeData = upgradeData;
  }
}
