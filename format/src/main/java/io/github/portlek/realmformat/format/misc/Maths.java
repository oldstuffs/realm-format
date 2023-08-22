package io.github.portlek.realmformat.format.misc;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Maths {

  public long asLong(final int chunkX, final int chunkZ) {
    return (long) chunkZ * Integer.MAX_VALUE + (long) chunkX;
  }

  public int floor(final double number) {
    final int floor = (int) number;
    return floor == number ? floor : floor - (int) (Double.doubleToRawLongBits(number) >>> 63);
  }
}
