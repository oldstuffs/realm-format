package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import io.github.shiruka.nbt.CompoundTag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RealmFormatChunkSectionV1 implements RealmFormatChunkSection {

  private CompoundTag biomes;

  private NibbleArray blockLight;

  private CompoundTag blockStates;

  private NibbleArray skyLight;
}
