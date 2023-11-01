package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.realm.BlockDataV1_14;
import io.github.portlek.realmformat.format.realm.BlockDataV1_18;
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

    private BlockDataV1_14 blockDataV1_14;

    private BlockDataV1_18 blockDataV1_18;

    private BlockDataV1_8 blockDataV1_8;

    private NibbleArray blockLight;

    private NibbleArray skyLight;
}
