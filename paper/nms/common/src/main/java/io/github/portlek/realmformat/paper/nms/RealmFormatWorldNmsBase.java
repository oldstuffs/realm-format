package io.github.portlek.realmformat.paper.nms;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.shiruka.nbt.CompoundTag;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RealmFormatWorldNmsBase implements RealmFormatWorldNms {

  private final Object chunkAccessLock = new Object();

  @Getter
  @NotNull
  private final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks;

  @Getter
  @NotNull
  private final CompoundTag extra;

  @Getter
  private final boolean locked;

  @Getter
  @NotNull
  private final String name;

  @NotNull
  private final NmsBackend nmsBackend;

  @Getter
  @NotNull
  private final RealmFormatPropertyMap properties;

  @Getter
  private final boolean readOnly;

  @Getter
  private final byte version;

  @Getter
  @Setter
  @NotNull
  private RealmFormatLoader loader;

  @Setter
  @Getter
  private byte worldVersion;

  protected RealmFormatWorldNmsBase(
    @NotNull final NmsBackend nmsBackend,
    @NotNull final String name,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks,
    @NotNull final CompoundTag extra,
    @NotNull final RealmFormatPropertyMap properties,
    final byte worldVersion,
    final byte version,
    @NotNull final RealmFormatLoader loader,
    final boolean locked,
    final boolean readOnly
  ) {
    this.nmsBackend = nmsBackend;
    this.name = name;
    this.chunks = chunks;
    this.extra = extra;
    this.properties = properties;
    this.worldVersion = worldVersion;
    this.version = version;
    this.loader = loader;
    this.locked = locked;
    this.readOnly = readOnly;
  }

  @NotNull
  @Override
  public final RealmFormatChunk chunkAt(final int x, final int z) {
    synchronized (this.chunkAccessLock) {
      return this.chunks.get(RealmFormatChunkPosition.builder().x(x).z(z).build());
    }
  }

  @NotNull
  @Override
  public final RealmFormatWorldNms clone(@NotNull final String worldName) throws Exception {
    try {
      return this.clone(worldName, null);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  @Override
  public final RealmFormatWorldNms clone(
    @NotNull final String worldName,
    @Nullable final RealmFormatLoader loader
  ) throws Exception {
    return this.clone(worldName, loader, true);
  }

  @NotNull
  @Override
  public final RealmFormatWorldNms clone(
    @NotNull final String worldName,
    @Nullable final RealmFormatLoader loader,
    final boolean lock
  ) throws Exception {
    Preconditions.checkState(
      !this.name.equals(worldName),
      "The clone world cannot have the same name as the original world!"
    );
    Preconditions.checkState(
      loader == null || !loader.exists(worldName),
      "World '%s' is already exists!",
      worldName
    );
    final var world = this.createRealmFormatWorld(worldName, loader, lock);
    if (loader != null) {
      loader.save(worldName, world.serialize().join(), lock);
    }
    return world;
  }

  @Override
  public final void updateChunk(@NotNull final RealmFormatChunk chunk) {
    synchronized (this.chunkAccessLock) {
      this.chunks.put(RealmFormatChunkPosition.builder().x(chunk.x()).z(chunk.z()).build(), chunk);
    }
  }

  @NotNull
  public abstract RealmFormatWorldNmsBase createRealmFormatWorld(
    @NotNull String worldName,
    @Nullable RealmFormatLoader loader,
    boolean lock
  );

  @NotNull
  public abstract CompletableFuture<RealmFormatChunk> serializeChunks(
    @NotNull final List<RealmFormatChunk> chunks,
    byte worldVersion
  ) throws IOException;
}
