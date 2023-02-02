package io.github.portlek.realmformat.paper.nms;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.old.realm.RealmChunk;
import io.github.portlek.realmformat.format.old.realm.RealmWorld;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public interface RealmWorldSource {
  @NotNull
  RealmWorld createRealmWorld(
    @NotNull RealmLoader loader,
    @NotNull String worldName,
    @NotNull Map<Long, RealmChunk> chunks,
    @NotNull CompoundTag extraCompound,
    @NotNull ListTag mapList,
    byte worldVersion,
    @NotNull RealmPropertyMap worldPropertyMap,
    boolean readOnly,
    boolean lock,
    @NotNull Map<Long, ListTag> entities
  );
}
