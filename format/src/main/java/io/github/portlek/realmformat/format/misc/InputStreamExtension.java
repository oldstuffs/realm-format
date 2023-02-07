package io.github.portlek.realmformat.format.misc;

import com.github.luben.zstd.Zstd;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import lombok.Cleanup;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InputStreamExtension implements Closeable {

  private static final int ARRAY_SIZE = 16 * 16 * 16 / (8 / 4);

  @NotNull
  @Delegate
  private final DataInputStream input;

  public InputStreamExtension(@NotNull final DataInputStream input) {
    this.input = input;
  }

  @NotNull
  public CompoundTag readCompoundTag() throws IOException {
    final var bytes = new byte[this.readInt()];
    this.read(bytes);
    return this.deserializeCompoundTag(bytes);
  }

  public byte@NotNull[] readCompressed() throws IOException {
    final var compressedLength = this.readInt();
    final var resultLength = this.readInt();
    final var compressed = new byte[compressedLength];
    final var result = new byte[resultLength];
    this.read(compressed);
    Zstd.decompress(result, compressed);
    return result;
  }

  @NotNull
  public CompoundTag readCompressedCompound() throws IOException {
    return this.deserializeCompoundTag(this.readCompressed());
  }

  @NotNull
  public ListTag readListTag() throws IOException {
    final var bytes = new byte[this.readInt()];
    this.read(bytes);
    return this.deserializeListTag(bytes);
  }

  public long@NotNull[] readLongArray() throws IOException {
    final var blockStatesArrayLength = this.readInt();
    final var longs = new long[blockStatesArrayLength];
    for (var index = 0; index < blockStatesArrayLength; index++) {
      longs[index] = this.readLong();
    }
    return longs;
  }

  @NotNull
  public NibbleArray readNibbleArray() throws IOException {
    final var bytes = new byte[InputStreamExtension.ARRAY_SIZE];
    this.read(bytes);
    return new NibbleArray(bytes);
  }

  @Nullable
  public NibbleArray readOptionalNibbleArray() throws IOException {
    if (!this.readBoolean()) {
      return null;
    }
    return this.readNibbleArray();
  }

  @NotNull
  private CompoundTag deserializeCompoundTag(final byte@NotNull[] bytes) throws IOException {
    if (bytes.length == 0) {
      return Tag.createCompound();
    }
    @Cleanup
    final var reader = Tag.createReader(new ByteArrayInputStream(bytes));
    return reader.readCompoundTag();
  }

  @NotNull
  private ListTag deserializeListTag(final byte@NotNull[] bytes) throws IOException {
    if (bytes.length == 0) {
      return Tag.createList();
    }
    @Cleanup
    final var reader = Tag.createReader(new ByteArrayInputStream(bytes));
    return reader.readListTag();
  }
}
