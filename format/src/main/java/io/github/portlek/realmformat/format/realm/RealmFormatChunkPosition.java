package io.github.portlek.realmformat.format.realm;

/**
 * A simple record class that represents {@link RealmFormatChunk}'s position.
 *
 * @param x The x coordinate of the chunk.
 * @param z The z coordinate of the chunk.
 */
public record RealmFormatChunkPosition(int x, int z) {}
