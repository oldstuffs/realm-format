package io.github.portlek.realmformat.format.misc;

import java.util.Arrays;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Credits to Minikloon for this class.
 * <p>
 * <a
 * href="https://github.com/Minikloon/CraftyWorld/blob/master/crafty-common/src/main/kotlin/world/crafty/common/utils/NibbleArray.kt">Source</a>
 */
@Getter
public final class NibbleArray {

    private final byte@NotNull[] backing;

    public NibbleArray(final byte@NotNull[] backing) {
        this.backing = backing;
    }

    public int get(final int index) {
        final byte value = this.backing[index / 2];
        return index % 2 == 0 ? value & 0xF : (value & 0xF0) >> 4;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.backing);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final NibbleArray that = (NibbleArray) o;
        return Arrays.equals(this.backing, that.backing);
    }

    @Override
    public String toString() {
        return "NibbleArray{" + "primitiveOriginal=" + Arrays.toString(this.backing) + '}';
    }

    public void set(final int index, final int value) {
        final int nibble = value & 0xF;
        final int halfIndex = index / 2;
        final int previous = this.backing[halfIndex];
        if (index % 2 == 0) {
            this.backing[halfIndex] = (byte) ((previous & 0xF0) | nibble);
        } else {
            this.backing[halfIndex] = (byte) ((previous & 0xF) | (nibble << 4));
        }
    }
}
