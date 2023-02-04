package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.ListTag;

public record BlockDataV1_13(
  ListTag palette,
  long[] blockStates
) {

}
