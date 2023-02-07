package io.github.portlek.realmformat.format.misc;

import com.github.luben.zstd.Zstd;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.function.IntFunction;
import lombok.Cleanup;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.function.FailableSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class InputStreamExtension implements Closeable {

  private static final int ARRAY_SIZE = 16 * 16 * 16 / (8 / 4);

  @NotNull
  @Delegate
  private final DataInputStream input;

  protected InputStreamExtension(@NotNull final DataInputStream input) {
    this.input = input;
  }

  @NotNull
  protected static CompoundTag deserializeCompoundTag(final byte@NotNull[] bytes)
    throws IOException {
    if (bytes.length == 0) {
      return Tag.createCompound();
    }
    @Cleanup
    final var reader = Tag.createReader(new ByteArrayInputStream(bytes));
    return reader.readCompoundTag();
  }

  @NotNull
  protected static ListTag deserializeListTag(final byte@NotNull[] bytes) throws IOException {
    if (bytes.length == 0) {
      return Tag.createList();
    }
    @Cleanup
    final var reader = Tag.createReader(new ByteArrayInputStream(bytes));
    return reader.readListTag();
  }

  public final <T> T[] readArray(
    @NotNull final IntFunction<T[]> arrayCreator,
    @NotNull final FailableSupplier<T, IOException> reader
  ) throws IOException {
    final var arrayLength = this.readInt();
    final var arrays = arrayCreator.apply(arrayLength);
    for (var index = 0; index < arrayLength; index++) {
      arrays[index] = reader.get();
    }
    return arrays;
  }

  @NotNull
  public final CompoundTag readCompoundTag() throws IOException {
    final var bytes = new byte[this.readInt()];
    this.read(bytes);
    return InputStreamExtension.deserializeCompoundTag(bytes);
  }

  public final byte@NotNull[] readCompressed() throws IOException {
    final var compressedLength = this.readInt();
    final var resultLength = this.readInt();
    final var compressed = new byte[compressedLength];
    final var result = new byte[resultLength];
    this.read(compressed);
    Zstd.decompress(result, compressed);
    return result;
  }

  @NotNull
  public final CompoundTag readCompressedCompound() throws IOException {
    return InputStreamExtension.deserializeCompoundTag(this.readCompressed());
  }

  public final int@NotNull[] readIntArray() throws IOException {
    final var arrayLength = this.readInt();
    final var ints = new int[arrayLength];
    for (var index = 0; index < arrayLength; index++) {
      ints[index] = this.readInt();
    }
    return ints;
  }

  @NotNull
  public final ListTag readListTag() throws IOException {
    final var bytes = new byte[this.readInt()];
    this.read(bytes);
    return InputStreamExtension.deserializeListTag(bytes);
  }

  public final long@NotNull[] readLongArray() throws IOException {
    final var arrayLength = this.readInt();
    final var longs = new long[arrayLength];
    for (var index = 0; index < arrayLength; index++) {
      longs[index] = this.readLong();
    }
    return longs;
  }

  @NotNull
  public final NibbleArray readNibbleArray() throws IOException {
    final var bytes = new byte[InputStreamExtension.ARRAY_SIZE];
    this.read(bytes);
    return new NibbleArray(bytes);
  }

  public final int@Nullable[] readOptionalIntArray() throws IOException {
    if (!this.readBoolean()) {
      return null;
    }
    return this.readIntArray();
  }

  @Nullable
  public final NibbleArray readOptionalNibbleArray() throws IOException {
    if (!this.readBoolean()) {
      return null;
    }
    return this.readNibbleArray();
  }
}
