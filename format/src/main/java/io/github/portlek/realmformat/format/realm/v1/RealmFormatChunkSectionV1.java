package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.realm.BlockDataV1_13;
import io.github.portlek.realmformat.format.realm.BlockDataV1_17;
import io.github.portlek.realmformat.format.realm.BlockDataV1_8;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class RealmFormatChunkSectionV1 implements RealmFormatChunkSection {

  private BlockDataV1_13 blockDataV1_13;

  private BlockDataV1_17 blockDataV1_17;

  private BlockDataV1_8 blockDataV1_8;

  private NibbleArray blockLight;

  private NibbleArray skyLight;
}
