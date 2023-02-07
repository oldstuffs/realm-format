package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

public record BlockDataV1_14(@NotNull ListTag palette, long@NotNull[] blockStates) {}
