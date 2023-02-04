package io.github.portlek.realmformat.format.misc;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * Credits to Minikloon for this class.
 * <p>
 * <a
 * href="https://github.com/Minikloon/CraftyWorld/blob/master/crafty-common/src/main/kotlin/world/crafty/common/utils/NibbleArray.kt">Source</a>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class NibbleArray {

  private final byte@NotNull[] backing;

  public NibbleArray(final byte@NotNull[] backing) {
    this.backing = backing;
  }

  public NibbleArray(final int size) {
    this(new byte[size / 2]);
  }

  public int get(final int index) {
    final var value = this.backing[index / 2];
    return index % 2 == 0 ? value & 0xF : (value & 0xF0) >> 4;
  }

  public void set(final int index, final int value) {
    final var nibble = value & 0xF;
    final var halfIndex = index / 2;
    final int previous = this.backing[halfIndex];
    if (index % 2 == 0) {
      this.backing[halfIndex] = (byte) (previous & 0xF0 | nibble);
    } else {
      this.backing[halfIndex] = (byte) (previous & 0xF | nibble << 4);
    }
  }
}
