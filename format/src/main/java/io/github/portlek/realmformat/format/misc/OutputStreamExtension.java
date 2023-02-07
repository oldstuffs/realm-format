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

public final class OutputStreamExtension implements Closeable {

  @NotNull
  @Delegate
  private final DataOutputStream output;

  public OutputStreamExtension(@NotNull final DataOutputStream output) {
    this.output = output;
  }

  public void writeLongArray(final long@NotNull[] longs) throws IOException {
    this.writeInt(longs.length);
    for (final var l : longs) {
      this.writeLong(l);
    }
  }

  public void writeCompound(@NotNull final CompoundTag tag) throws IOException {
    final var bytes = this.serializeCompound(tag);
    this.writeInt(bytes.length);
    this.write(bytes);
  }

  public void writeCompressed(final byte@NotNull[] bytes) throws IOException {
    final var compressedExtra = Zstd.compress(bytes);
    this.writeInt(compressedExtra.length);
    this.writeInt(bytes.length);
    this.write(compressedExtra);
  }

  public void writeCompressedCompound(@NotNull final CompoundTag tag) throws IOException {
    final var compound = this.serializeCompound(tag);
    this.writeCompressed(compound);
  }

  public void writeListTag(@NotNull final ListTag tag) throws IOException {
    final var bytes = this.serializeListTag(tag);
    this.writeInt(bytes.length);
    this.write(bytes);
  }

  public void writeNibbleArray(@Nullable final NibbleArray array) throws IOException {
    this.write(array.backing());
  }

  public void writeOptionalNibbleArray(@Nullable final NibbleArray array) throws IOException {
    final var has = array != null;
    this.writeBoolean(has);
    if (has) {
      this.write(array.backing());
    }
  }

  private byte@NotNull[] serializeCompound(@NotNull final CompoundTag tag) throws IOException {
    if (tag.isEmpty()) {
      return new byte[0];
    }
    final var output = new ByteArrayOutputStream();
    @Cleanup
    final var writer = Tag.createWriter(output);
    writer.write(tag);
    return output.toByteArray();
  }

  private byte@NotNull[] serializeListTag(@NotNull final ListTag tag) throws IOException {
    if (tag.isEmpty()) {
      return new byte[0];
    }
    final var output = new ByteArrayOutputStream();
    @Cleanup
    final var writer = Tag.createWriter(output);
    writer.write(tag);
    return output.toByteArray();
  }
}
