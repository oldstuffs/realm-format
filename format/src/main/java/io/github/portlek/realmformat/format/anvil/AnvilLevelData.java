package io.github.portlek.realmformat.format.anvil;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

public record AnvilLevelData(int version, @NotNull Map<String, String> gameRules) {}
