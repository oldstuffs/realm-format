package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public final class BlockDataV1_8 {

  @NotNull
  private final NibbleArray data;
}
