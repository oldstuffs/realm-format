package io.github.portlek.realmformat.format.misc;

import com.github.luben.zstd.Zstd;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.Cleanup;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OutputStreamExtension implements Closeable {

  @NotNull
  @Delegate
  private final DataOutputStream output;

  protected OutputStreamExtension(@NotNull final DataOutputStream output) {
    this.output = output;
  }

  private static byte@NotNull[] serializeCompound(@NotNull final CompoundTag tag)
    throws IOException {
    if (tag.isEmpty()) {
      return new byte[0];
    }
    final var output = new ByteArrayOutputStream();
    @Cleanup
    final var writer = Tag.createWriter(output);
    writer.write(tag);
    return output.toByteArray();
  }

  private static byte@NotNull[] serializeListTag(@NotNull final ListTag tag) throws IOException {
    if (tag.isEmpty()) {
      return new byte[0];
    }
    final var output = new ByteArrayOutputStream();
    @Cleanup
    final var writer = Tag.createWriter(output);
    writer.write(tag);
    return output.toByteArray();
  }

  public final <T> void writeArray(
    @NotNull final T[] array,
    @NotNull final FailableBiConsumer<Integer, T, IOException> writer
  ) throws IOException {
    this.writeInt(array.length);
    for (var i = 0; i < array.length; i++) {
      writer.accept(i, array[i]);
    }
  }

  public final void writeCompound(@NotNull final CompoundTag tag) throws IOException {
    final var bytes = OutputStreamExtension.serializeCompound(tag);
    this.writeInt(bytes.length);
    this.write(bytes);
  }

  public final void writeCompressed(final byte@NotNull[] bytes) throws IOException {
    final var compressedExtra = Zstd.compress(bytes);
    this.writeInt(compressedExtra.length);
    this.writeInt(bytes.length);
    this.write(compressedExtra);
  }

  public final void writeCompressedCompound(@NotNull final CompoundTag tag) throws IOException {
    final var compound = OutputStreamExtension.serializeCompound(tag);
    this.writeCompressed(compound);
  }

  public final void writeIntArray(final int@NotNull[] ints) throws IOException {
    this.writeInt(ints.length);
    for (final var l : ints) {
      this.writeInt(l);
    }
  }

  public final void writeListTag(@NotNull final ListTag tag) throws IOException {
    final var bytes = OutputStreamExtension.serializeListTag(tag);
    this.writeInt(bytes.length);
    this.write(bytes);
  }

  public final void writeLongArray(final long@NotNull[] longs) throws IOException {
    this.writeInt(longs.length);
    for (final var l : longs) {
      this.writeLong(l);
    }
  }

  public final void writeNibbleArray(@NotNull final NibbleArray array) throws IOException {
    this.write(array.backing());
  }

  public final void writeOptionalIntArray(final int@Nullable[] ints) throws IOException {
    final var has = ints != null;
    this.writeBoolean(has);
    if (has) {
      this.writeIntArray(ints);
    }
  }

  public final void writeOptionalNibbleArray(@Nullable final NibbleArray array) throws IOException {
    final var has = array != null;
    this.writeBoolean(has);
    if (has) {
      this.writeNibbleArray(array);
    }
  }
}
