package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;

public record BlockDataV1_17(
  CompoundTag biomes,
  CompoundTag blockStates
) {

}