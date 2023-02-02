package io.github.portlek.realmformat.format.realm.impl;

import com.github.luben.zstd.Zstd;
import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.exception.WorldAlreadyExistsException;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.misc.ChunkSerialization;
import io.github.portlek.realmformat.format.misc.Misc;
import io.github.portlek.realmformat.format.misc.RealmConstants;
import io.github.portlek.realmformat.format.property.RealmProperties;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmChunk;
import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tr.com.infumia.task.Promise;

public abstract class RealmWorldBase implements RealmWorld {

  protected final Object chunkAccessLock = new Object();

  @NotNull
  private final Map<Long, RealmChunk> chunks;

  @Getter
  @NotNull
  private final Map<Long, ListTag> entities;

  @Getter
  @NotNull
  private final CompoundTag extraData;

  @Getter
  private final boolean locked;

  @Getter
  @NotNull
  private final String name;

  @Getter
  @NotNull
  private final RealmPropertyMap propertyMap;

  @Getter
  private final boolean readOnly;

  @Getter
  private final ListTag worldMaps = Tag.createList();

  @Getter
  @Setter
  @NotNull
  private RealmLoader loader;

  @Getter
  @Setter
  private byte version;

  protected RealmWorldBase(
    final byte version,
    @NotNull final RealmLoader loader,
    @NotNull final String name,
    @NotNull final Map<Long, RealmChunk> chunks,
    @NotNull final CompoundTag extraData,
    @NotNull final RealmPropertyMap propertyMap,
    final boolean readOnly,
    final boolean locked,
    @NotNull final Map<Long, ListTag> entities
  ) {
    this.version = version;
    this.loader = loader;
    this.name = name;
    this.chunks = chunks;
    this.extraData = extraData;
    this.propertyMap = propertyMap;
    this.readOnly = readOnly;
    this.locked = locked;
    this.entities = entities;
  }

  protected static byte@NotNull[] serializeCompoundTag(@Nullable final CompoundTag tag)
    throws IOException {
    if (tag == null || tag.isEmpty()) {
      return new byte[0];
    }
    @Cleanup
    final var outByteStream = new ByteArrayOutputStream();
    @Cleanup
    final var outStream = Tag.createWriter(outByteStream);
    outStream.write(tag);
    return outByteStream.toByteArray();
  }

  private static void writeBitSetAsBytes(
    @NotNull final DataOutputStream outStream,
    @NotNull final BitSet set,
    final int fixedSize
  ) throws IOException {
    final var array = set.toByteArray();
    outStream.write(array);
    final var chunkMaskPadding = fixedSize - array.length;
    for (var i = 0; i < chunkMaskPadding; i++) {
      outStream.write(0);
    }
  }

  @Nullable
  @Override
  public final RealmChunk chunkAt(final int x, final int z) {
    synchronized (this.chunkAccessLock) {
      return this.chunks.get(Misc.asLong(x, z));
    }
  }

  @NotNull
  @Override
  public final Map<Long, RealmChunk> chunks() {
    synchronized (this.chunkAccessLock) {
      return Collections.unmodifiableMap(this.chunks);
    }
  }

  @Nullable
  @Override
  public final RealmWorld clone(@NotNull final String worldName) {
    try {
      return this.clone(worldName, null);
    } catch (final WorldAlreadyExistsException | IOException ignored) {
      return null;
    }
  }

  @NotNull
  @Override
  public final RealmWorld clone(
    @NotNull final String worldName,
    @Nullable final RealmLoader loader
  ) throws WorldAlreadyExistsException, IOException {
    return this.clone(worldName, loader, true);
  }

  @NotNull
  @Override
  public final RealmWorld clone(
    @NotNull final String worldName,
    @Nullable final RealmLoader loader,
    final boolean lock
  ) throws WorldAlreadyExistsException, IOException {
    Preconditions.checkArgument(
      this.name.equals(worldName),
      "The clone world cannot have the same name as the original world!"
    );
    WorldAlreadyExistsException.check(loader == null || loader.worldExists(worldName), worldName);
    final var world = this.deepClone(worldName, loader, lock);
    if (loader != null) {
      loader.saveWorld(worldName, world.serialize().join(), lock);
    }
    return world;
  }

  @NotNull
  @Override
  @SneakyThrows
  public final Promise<byte@NotNull[]> serialize() {
    final var sortedChunks = new ArrayList<>(this.chunks().values());
    sortedChunks.sort(Comparator.comparingLong(chunk -> Misc.asLong(chunk.x(), chunk.z())));
    sortedChunks.removeIf(Objects::isNull);
    if (this.propertyMap.getValue(RealmProperties.SHOULD_LIMIT_SAVE)) {
      final var minX = this.propertyMap.getValue(RealmProperties.SAVE_MIN_X);
      final var maxX = this.propertyMap.getValue(RealmProperties.SAVE_MAX_X);
      final var minZ = this.propertyMap.getValue(RealmProperties.SAVE_MIN_Z);
      final var maxZ = this.propertyMap.getValue(RealmProperties.SAVE_MAX_Z);
      sortedChunks.removeIf(chunk -> {
        final var chunkX = chunk.x();
        final var chunkZ = chunk.z();
        if (chunkX < minX || chunkX > maxX) {
          return true;
        }
        return chunkZ < minZ || chunkZ > maxZ;
      });
    }
    if (this.extraData.containsKey("properties")) {
      this.extraData.set("properties", this.propertyMap.tag());
    } else {
      this.extraData.set("properties", Tag.createCompound());
    }
    @Cleanup
    final var outByteStream = new ByteArrayOutputStream();
    @Cleanup
    final var outStream = new DataOutputStream(outByteStream);
    outStream.write(RealmConstants.HEADER);
    outStream.write(RealmConstants.VERSION);
    outStream.writeByte(this.version);
    final var minX = sortedChunks.stream().mapToInt(RealmChunk::x).min().orElse(0);
    final var minZ = sortedChunks.stream().mapToInt(RealmChunk::z).min().orElse(0);
    final var maxX = sortedChunks.stream().mapToInt(RealmChunk::x).max().orElse(0);
    final var maxZ = sortedChunks.stream().mapToInt(RealmChunk::z).max().orElse(0);
    outStream.writeShort(minX);
    outStream.writeShort(minZ);
    final var width = maxX - minX + 1;
    final var depth = maxZ - minZ + 1;
    outStream.writeShort(width);
    outStream.writeShort(depth);
    final var chunkBitset = new BitSet(width * depth);
    for (final var chunk : sortedChunks) {
      final var bitsetIndex = (chunk.z() - minZ) * width + chunk.x() - minX;
      chunkBitset.set(bitsetIndex, true);
    }
    final var chunkMaskSize = (int) Math.ceil((width * depth) / 8.0D);
    RealmWorldBase.writeBitSetAsBytes(outStream, chunkBitset, chunkMaskSize);
    return this.serializeChunks(sortedChunks, this.version)
      .thenApplyAsync(serializedChunk -> {
        try {
          final var chunkData = serializedChunk.chunks();
          final var compressedChunkData = Zstd.compress(chunkData);
          outStream.writeInt(compressedChunkData.length);
          outStream.writeInt(chunkData.length);
          outStream.write(compressedChunkData);
          final var tileEntitiesList = serializedChunk.tileEntities();
          final var tileEntitiesCompound = Tag.createCompound().set("tiles", tileEntitiesList);
          final var tileEntitiesData = RealmWorldBase.serializeCompoundTag(tileEntitiesCompound);
          final var compressedTileEntitiesData = Zstd.compress(tileEntitiesData);
          outStream.writeInt(compressedTileEntitiesData.length);
          outStream.writeInt(tileEntitiesData.length);
          outStream.write(compressedTileEntitiesData);
          final var entitiesList = serializedChunk.entities();
          outStream.writeBoolean(!entitiesList.isEmpty());
          if (!entitiesList.isEmpty()) {
            final var entitiesCompound = Tag.createCompound().set("entities", entitiesList);
            final var entitiesData = RealmWorldBase.serializeCompoundTag(entitiesCompound);
            final var compressedEntitiesData = Zstd.compress(entitiesData);
            outStream.writeInt(compressedEntitiesData.length);
            outStream.writeInt(entitiesData.length);
            outStream.write(compressedEntitiesData);
          }
          final var extra = RealmWorldBase.serializeCompoundTag(this.extraData);
          final var compressedExtra = Zstd.compress(extra);
          outStream.writeInt(compressedExtra.length);
          outStream.writeInt(extra.length);
          outStream.write(compressedExtra);
          final var worldMaps = Tag.createCompound();
          worldMaps.setList("maps", Collections.emptyList());
          final var mapArray = RealmWorldBase.serializeCompoundTag(worldMaps);
          final var compressedMapArray = Zstd.compress(mapArray);
          outStream.writeInt(compressedMapArray.length);
          outStream.writeInt(mapArray.length);
          outStream.write(compressedMapArray);
          return outByteStream.toByteArray();
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      });
  }

  @Override
  public final void updateChunk(@NotNull final RealmChunk chunk) {
    synchronized (this.chunkAccessLock) {
      this.chunks.put(Misc.asLong(chunk.x(), chunk.z()), chunk);
    }
  }

  @NotNull
  protected abstract RealmWorld deepClone(
    @NotNull String worldName,
    @Nullable RealmLoader loader,
    boolean lock
  );

  @NotNull
  protected abstract Promise<@NotNull ChunkSerialization> serializeChunks(
    @NotNull List<RealmChunk> chunks,
    byte worldVersion
  ) throws IOException;
}
