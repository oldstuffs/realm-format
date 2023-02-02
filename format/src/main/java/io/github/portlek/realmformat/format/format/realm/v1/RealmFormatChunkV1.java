package io.github.portlek.realmformat.format.format.realm.v1;

import io.github.portlek.realmformat.format.format.realm.RealmFormatChunk;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
final class RealmFormatChunkV1 implements RealmFormatChunk {}
