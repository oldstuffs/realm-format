package io.github.portlek.realmformat.format.realm;

import io.github.shiruka.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public record BlockDataV1_18(@NotNull CompoundTag biomes, @NotNull CompoundTag blockStates) {}
