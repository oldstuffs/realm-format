package io.github.portlek.realmformat.paper.internal.misc;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Misc {

  public boolean isPaper() {
    try {
      Class.forName("com.destroystokyo.paper.PaperConfig");
      return true;
    } catch (final ClassNotFoundException ex) {
      return false;
    }
  }
}
