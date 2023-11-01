package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.shiruka.nbt.CompoundTag;
import java.util.Map;

public interface RealmFormatWorld {
    Map<RealmFormatChunkPosition, RealmFormatChunk> chunks();

    CompoundTag extra();

    RealmFormatPropertyMap properties();

    byte version();

    byte worldVersion();
}
