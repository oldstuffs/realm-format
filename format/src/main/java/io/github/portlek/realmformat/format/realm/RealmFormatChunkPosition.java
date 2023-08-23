package io.github.portlek.realmformat.format.realm;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public final class RealmFormatChunkPosition {

    private final int x;

    private final int z;
}
