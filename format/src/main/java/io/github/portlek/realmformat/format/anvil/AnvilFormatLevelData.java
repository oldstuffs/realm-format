package io.github.portlek.realmformat.format.anvil;

import java.util.Map;
import org.jetbrains.annotations.NotNull;

record AnvilFormatLevelData(
  byte version,
  @NotNull Map<String, String> gameRules,
  int spawnX,
  int spawnY,
  int spawnZ
) {

}
