package io.github.portlek.realmformat.format.old.realm;

import io.github.portlek.realmformat.format.exception.WorldAlreadyExistsException;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import java.io.IOException;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tr.com.infumia.task.Promise;

public interface RealmWorld {
  @Nullable
  RealmChunk chunkAt(int x, int z);

  @NotNull
  Map<Long, RealmChunk> chunks();

  @Nullable
  RealmWorld clone(@NotNull String worldName);

  @NotNull
  RealmWorld clone(@NotNull String worldName, @Nullable RealmLoader loader)
    throws WorldAlreadyExistsException, IOException;

  @NotNull
  RealmWorld clone(@NotNull String worldName, @Nullable RealmLoader loader, boolean lock)
    throws WorldAlreadyExistsException, IOException;

  @NotNull
  Map<Long, ListTag> entities();

  @NotNull
  CompoundTag extraData();

  @NotNull
  RealmWorld loader(@NotNull RealmLoader loader);

  @NotNull
  RealmLoader loader();

  boolean locked();

  String name();

  @NotNull
  RealmPropertyMap propertyMap();

  boolean readOnly();

  @NotNull
  Promise<byte@NotNull[]> serialize();

  void updateChunk(@NotNull RealmChunk chunk);

  byte version();

  @NotNull
  RealmWorld version(byte version);

  @NotNull
  ListTag worldMaps();
}
