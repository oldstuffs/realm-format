package io.github.portlek.realmformat.format.anvil;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
final class AnvilFormatChunkEntry {

  private final int offset;

  private final int paddedSize;
}
