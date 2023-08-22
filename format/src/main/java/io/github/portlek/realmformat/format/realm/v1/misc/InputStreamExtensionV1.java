package io.github.portlek.realmformat.format.realm.v1.misc;

import io.github.portlek.realmformat.format.misc.InputStreamExtension;
import io.github.portlek.realmformat.format.misc.Maths;
import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.realm.BlockDataV1_14;
import io.github.portlek.realmformat.format.realm.BlockDataV1_18;
import io.github.portlek.realmformat.format.realm.BlockDataV1_8;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkSectionV1;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkV1;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
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
    final byte[] data = this.readCompressed();
    @Cleanup
    final InputStreamExtensionV1 chunkInput = this.withV1(data);
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
    final CompoundTag compound = this.readCompressedCompound();
    final ListTag entitiesCompound = compound
      .getListTag("entities", TagTypes.COMPOUND)
      .orElse(Tag.createList());
    for (final Tag entity : entitiesCompound) {
      final ListTag pos = entity
        .asCompound()
        .getListTag("Pos", TagTypes.DOUBLE)
        .orElse(Tag.createList());
      final int x = Maths.floor(pos.getDouble(0).orElse(0.0d)) >> 4;
      final int z = Maths.floor(pos.getDouble(2).orElse(0.0d)) >> 4;
      final RealmFormatChunk chunk = chunks.get(
        RealmFormatChunkPosition.builder().x(x).z(z).build()
      );
      if (chunk != null) {
        chunk.entities().add(entity);
      }
    }
  }

  public void readTileEntities(
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final CompoundTag compound = this.readCompressedCompound();
    final ListTag list = compound.getListTag("tiles", TagTypes.COMPOUND).orElse(Tag.createList());
    for (final Tag tileEntity : list) {
      final CompoundTag tileEntityCompound = tileEntity.asCompound();
      final int x = tileEntityCompound.getInteger("x").orElse(0) >> 4;
      final int z = tileEntityCompound.getInteger("z").orElse(0) >> 4;
      final RealmFormatChunk chunk = chunks.get(
        RealmFormatChunkPosition.builder().x(x).z(z).build()
      );
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
    final RealmFormatChunkSection[] chunkSectionArray = new RealmFormatChunkSection[maxSection -
    minSection];
    final int sectionCount = this.readInt();
    for (int i = 0; i < sectionCount; i++) {
      final int y = this.readInt();
      final RealmFormatChunkSectionV1.RealmFormatChunkSectionV1Builder builder =
        RealmFormatChunkSectionV1.builder();
      final NibbleArray blockLight = this.readOptionalNibbleArray();
      final NibbleArray skyLight = this.readOptionalNibbleArray();
      builder.blockLight(blockLight).skyLight(skyLight);
      if (this.worldVersion < 4) {
        final NibbleArray data = this.readNibbleArray();
        builder.blockDataV1_8(BlockDataV1_8.builder().data(data).build());
      } else if (this.worldVersion < 8) {
        final ListTag palette = this.readListTag();
        final long[] blockStates = this.readLongArray();
        builder.blockDataV1_14(new BlockDataV1_14(palette, blockStates));
      } else {
        final CompoundTag blockStates = this.readCompoundTag();
        final CompoundTag biomes = this.readCompoundTag();
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
          final int x = this.readInt();
          final int z = this.readInt();
          final int[] biomes = this.readOptionalIntArray();
          final CompoundTag heightMap = this.readCompoundTag();
          final int minSection = this.readInt();
          final int maxSection = this.readInt();
          final RealmFormatChunkSection[] sections = this.readChunkSections(minSection, maxSection);
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
