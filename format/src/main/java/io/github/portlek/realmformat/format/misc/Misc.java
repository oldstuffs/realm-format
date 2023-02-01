package io.github.portlek.realmformat.format.misc;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Misc {

  public long asLong(final int chunkX, final int chunkZ) {
    return (long) chunkZ * Integer.MAX_VALUE + (long) chunkX;
  }
}
