package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import org.jetbrains.annotations.Nullable;

public interface RealmFormatChunkSection {
    @Nullable
    BlockDataV1_14 blockDataV1_14();

    @Nullable
    BlockDataV1_18 blockDataV1_18();

    @Nullable
    BlockDataV1_8 blockDataV1_8();

    @Nullable
    NibbleArray blockLight();

    @Nullable
    NibbleArray skyLight();
}
