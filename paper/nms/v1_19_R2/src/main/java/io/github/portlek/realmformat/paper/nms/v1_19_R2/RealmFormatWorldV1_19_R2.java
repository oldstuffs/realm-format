package io.github.portlek.realmformat.paper.nms.v1_19_R2;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.nms.NmsBackend;
import io.github.portlek.realmformat.paper.nms.RealmFormatWorldNmsBase;
import io.github.shiruka.nbt.CompoundTag;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RealmFormatWorldV1_19_R2 extends RealmFormatWorldNmsBase {

  public RealmFormatWorldV1_19_R2(
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
    super(
      nmsBackend,
      name,
      chunks,
      extra,
      properties,
      worldVersion,
      version,
      loader,
      locked,
      readOnly
    );
  }

  @NotNull
  @Override
  public RealmFormatWorldNmsBase createRealmFormatWorld(
    @NotNull final String worldName,
    @Nullable final RealmFormatLoader loader,
    final boolean lock
  ) {
    return null;
  }

  @NotNull
  @Override
  public CompletableFuture<RealmFormatChunk> serializeChunks(
    @NotNull final List<RealmFormatChunk> chunks,
    final byte worldVersion
  ) throws IOException {
    return null;
  }

  @NotNull
  @Override
  public CompletableFuture<byte[]> serialize() throws Exception {
    return null;
  }
}
