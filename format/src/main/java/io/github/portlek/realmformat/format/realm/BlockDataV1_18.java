package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public final class BlockDataV1_18 {

  @NotNull
  private final CompoundTag biomes;

  @NotNull
  private final CompoundTag blockStates;
}
