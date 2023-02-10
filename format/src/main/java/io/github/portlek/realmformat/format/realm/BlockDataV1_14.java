package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.ListTag;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@ToString
@EqualsAndHashCode
public final class BlockDataV1_14 {

  private final long@NotNull[] blockStates;

  @NotNull
  private final ListTag palette;

  public BlockDataV1_14(@NotNull final ListTag palette, final long@NotNull[] blockStates) {
    this.palette = palette;
    this.blockStates = blockStates;
  }
}
