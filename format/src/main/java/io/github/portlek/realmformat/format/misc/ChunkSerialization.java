package io.github.portlek.realmformat.format.misc;

import io.github.shiruka.nbt.ListTag;

public record ChunkSerialization(byte[] chunks, ListTag tileEntities, ListTag entities) {}
