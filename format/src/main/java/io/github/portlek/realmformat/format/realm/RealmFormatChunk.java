package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.Nullable;

public interface RealmFormatChunk {
    int@Nullable[] biomes();

    ListTag entities();

    CompoundTag heightMaps();

    int maxSection();

    int minSection();

    RealmFormatChunkSection[] sections();

    ListTag tileEntities();

    int x();

    int z();
}
