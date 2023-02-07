package io.github.portlek.realmformat.format.realm.v1.misc;

import io.github.portlek.realmformat.format.misc.OutputStreamExtension;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import lombok.Cleanup;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OutputStreamExtensionV1 extends OutputStreamExtension {

  @NotNull
  private final RealmFormatPropertyMap properties;

  private final byte worldVersion;

  public OutputStreamExtensionV1(
    @NotNull final DataOutputStream output,
    @NotNull final RealmFormatPropertyMap properties,
    final byte worldVersion
  ) {
    super(output);
    this.properties = properties;
    this.worldVersion = worldVersion;
  }

  public void writeCompressedChunks(@NotNull final Collection<RealmFormatChunk> chunks)
    throws IOException {
    this.writeCompressed(this.serializeChunks(chunks));
  }

  public void writeEntities(@NotNull final Collection<RealmFormatChunk> chunks) throws IOException {
    final var entities = Tag.createList();
    for (final var chunk : chunks) {
      for (final var entity : chunk.entities()) {
        entities.add(entity);
      }
    }
    this.writeCompressedCompound(Tag.createCompound().set("entities", entities));
  }

  public void writeTileEntities(@NotNull final Collection<RealmFormatChunk> chunks)
    throws IOException {
    final var entities = Tag.createList();
    for (final var chunk : chunks) {
      for (final var entity : chunk.tileEntities()) {
        entities.add(entity);
      }
    }
    this.writeCompressedCompound(Tag.createCompound().set("tiles", entities));
  }

  @ApiStatus.Internal
  protected byte@NotNull[] serializeChunks(@NotNull final Collection<RealmFormatChunk> chunks)
    throws IOException {
    final var outputStream = new ByteArrayOutputStream(16384);
    @Cleanup
    final var output = this.with(outputStream);
    output.writeChunks(chunks);
    return outputStream.toByteArray();
  }

  @ApiStatus.Internal
  protected OutputStreamExtensionV1 with(@NotNull final OutputStream stream) {
    return new OutputStreamExtensionV1(
      new DataOutputStream(stream),
      this.properties,
      this.worldVersion
    );
  }

  @ApiStatus.Internal
  protected void writeChunkSections(@Nullable final RealmFormatChunkSection@NotNull[] sections)
    throws IOException {
    final var sectionCount = Math.toIntExact(
      Arrays.stream(sections).filter(Objects::nonNull).count()
    );
    this.writeInt(sectionCount);
    for (var i = 0; i < sections.length; i++) {
      final var section = sections[i];
      if (section == null) {
        continue;
      }
      this.writeInt(i);
      this.writeOptionalNibbleArray(section.blockLight());
      this.writeOptionalNibbleArray(section.skyLight());
      if (this.worldVersion < 4) {
        final var blockDataV1_8 = Objects.requireNonNull(
          section.blockDataV1_8(),
          "Block data for 1.8 not found!"
        );
        this.writeNibbleArray(blockDataV1_8.data());
      } else if (this.worldVersion < 8) {
        final var blockDataV1_14 = Objects.requireNonNull(
          section.blockDataV1_14(),
          "Block data for 1.14 not found!"
        );
        this.writeListTag(blockDataV1_14.palette());
        this.writeLongArray(blockDataV1_14.blockStates());
      } else {
        final var blockDataV1_18 = Objects.requireNonNull(
          section.blockDataV1_18(),
          "Block data for 1.18 not found!"
        );
        this.writeCompound(blockDataV1_18.blockStates());
        this.writeCompound(blockDataV1_18.biomes());
      }
    }
  }

  @ApiStatus.Internal
  protected void writeChunks(@NotNull final Collection<RealmFormatChunk> chunks)
    throws IOException {
    this.writeArray(
        chunks.toArray(RealmFormatChunk[]::new),
        (__, chunk) -> {
          this.writeInt(chunk.x());
          this.writeInt(chunk.z());
          this.writeOptionalIntArray(chunk.biomes());
          this.writeCompound(chunk.heightMaps());
          this.writeInt(chunk.minSection());
          this.writeInt(chunk.maxSection());
          this.writeChunkSections(chunk.sections());
        }
      );
  }
}
