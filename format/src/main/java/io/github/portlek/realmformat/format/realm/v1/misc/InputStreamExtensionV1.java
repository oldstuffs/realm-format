package io.github.portlek.realmformat.format.realm.v1.misc;

import io.github.portlek.realmformat.format.misc.InputStreamExtension;
import io.github.portlek.realmformat.format.misc.Maths;
import io.github.portlek.realmformat.format.realm.BlockDataV1_14;
import io.github.portlek.realmformat.format.realm.BlockDataV1_18;
import io.github.portlek.realmformat.format.realm.BlockDataV1_8;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkSectionV1;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkV1;
import io.github.shiruka.nbt.Tag;
import io.github.shiruka.nbt.TagTypes;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Cleanup;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class InputStreamExtensionV1 extends InputStreamExtension {

  private final byte worldVersion;

  public InputStreamExtensionV1(@NotNull final DataInputStream input, final byte worldVersion) {
    super(input);
    this.worldVersion = worldVersion;
  }

  @NotNull
  public Map<RealmFormatChunkPosition, RealmFormatChunk> readCompressedChunks() throws IOException {
    final var data = this.readCompressed();
    @Cleanup
    final var chunkInput = this.withV1(data);
    return Arrays
      .stream(chunkInput.readChunks())
      .collect(
        Collectors.toMap(
          chunk -> RealmFormatChunkPosition.builder().x(chunk.x()).z(chunk.z()).build(),
          Function.identity()
        )
      );
  }

  public void readEntities(@NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks)
    throws IOException {
    final var compound = this.readCompressedCompound();
    final var entitiesCompound = compound
      .getListTag("entities", TagTypes.COMPOUND)
      .orElse(Tag.createList());
    for (final var entity : entitiesCompound) {
      final var pos = entity
        .asCompound()
        .getListTag("Pos", TagTypes.DOUBLE)
        .orElse(Tag.createList());
      final var x = Maths.floor(pos.getDouble(0).orElse(0.0d)) >> 4;
      final var z = Maths.floor(pos.getDouble(2).orElse(0.0d)) >> 4;
      final var chunk = chunks.get(RealmFormatChunkPosition.builder().x(x).z(z).build());
      if (chunk != null) {
        chunk.entities().add(entity);
      }
    }
  }

  public void readTileEntities(
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var compound = this.readCompressedCompound();
    final var list = compound.getListTag("tiles", TagTypes.COMPOUND).orElse(Tag.createList());
    for (final var tileEntity : list) {
      final var tileEntityCompound = tileEntity.asCompound();
      final var x = tileEntityCompound.getInteger("x").orElse(0) >> 4;
      final var z = tileEntityCompound.getInteger("z").orElse(0) >> 4;
      final var chunk = chunks.get(RealmFormatChunkPosition.builder().x(x).z(z).build());
      if (chunk != null) {
        chunk.tileEntities().add(tileEntity);
      }
    }
  }

  @NotNull
  @ApiStatus.Internal
  protected final InputStreamExtensionV1 withV1(final byte@NotNull[] bytes) {
    return new InputStreamExtensionV1(
      new DataInputStream(new ByteArrayInputStream(bytes)),
      this.worldVersion
    );
  }

  @NotNull
  @ApiStatus.Internal
  protected RealmFormatChunkSection@NotNull[] readChunkSections(
    final int minSection,
    final int maxSection
  ) throws IOException {
    final var chunkSectionArray = new RealmFormatChunkSection[maxSection - minSection];
    final var sectionCount = this.readInt();
    for (var i = 0; i < sectionCount; i++) {
      final var y = this.readInt();
      final var builder = RealmFormatChunkSectionV1.builder();
      final var blockLight = this.readOptionalNibbleArray();
      final var skyLight = this.readOptionalNibbleArray();
      builder.blockLight(blockLight).skyLight(skyLight);
      if (this.worldVersion < 4) {
        final var data = this.readNibbleArray();
        builder.blockDataV1_8(BlockDataV1_8.builder().data(data).build());
      } else if (this.worldVersion < 8) {
        final var palette = this.readListTag();
        final var blockStates = this.readLongArray();
        builder.blockDataV1_14(new BlockDataV1_14(palette, blockStates));
      } else {
        final var blockStates = this.readCompoundTag();
        final var biomes = this.readCompoundTag();
        builder.blockDataV1_18(
          BlockDataV1_18.builder().biomes(biomes).blockStates(blockStates).build()
        );
      }
      chunkSectionArray[y] = builder.build();
    }
    return chunkSectionArray;
  }

  @NotNull
  @ApiStatus.Internal
  protected RealmFormatChunk[] readChunks() throws IOException {
    return this.readArray(
        RealmFormatChunk[]::new,
        () -> {
          final var x = this.readInt();
          final var z = this.readInt();
          final var biomes = this.readOptionalIntArray();
          final var heightMap = this.readCompoundTag();
          final var minSection = this.readInt();
          final var maxSection = this.readInt();
          final var sections = this.readChunkSections(minSection, maxSection);
          return RealmFormatChunkV1
            .builder()
            .x(x)
            .z(z)
            .biomes(biomes)
            .sections(sections)
            .heightMaps(heightMap)
            .minSection(minSection)
            .maxSection(maxSection)
            .entities(Tag.createList())
            .tileEntities(Tag.createList())
            .build();
        }
      );
  }
}
