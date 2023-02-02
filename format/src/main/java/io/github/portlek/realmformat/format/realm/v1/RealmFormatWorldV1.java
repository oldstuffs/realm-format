package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.shiruka.nbt.CompoundTag;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RealmFormatWorldV1 implements RealmFormatWorld {

  private Map<RealmFormatChunkPosition, RealmFormatChunk> chunks;

  private CompoundTag extra;

  private RealmFormatPropertyMap properties;

  private byte worldVersion;

  @Override
  public byte version() {
    return 1;
  }
}
