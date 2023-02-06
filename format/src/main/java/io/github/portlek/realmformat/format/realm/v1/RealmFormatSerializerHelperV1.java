package io.github.portlek.realmformat.format.realm.v1;

import com.github.luben.zstd.Zstd;
import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.property.RealmFormatProperties;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.BlockDataV1_14;
import io.github.portlek.realmformat.format.realm.BlockDataV1_18;
import io.github.portlek.realmformat.format.realm.BlockDataV1_8;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import io.github.shiruka.nbt.TagTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
class RealmFormatSerializerHelperV1 {

  private final int ARRAY_SIZE = 16 * 16 * 16 / (8 / 4);

  @NotNull
  Map<RealmFormatChunkPosition, RealmFormatChunk> readChunks(
    @NotNull final DataInputStream input,
    @NotNull final RealmFormatPropertyMap properties,
    final byte worldVersion
  ) throws IOException {
    final var result = new Object2ObjectOpenHashMap<RealmFormatChunkPosition, RealmFormatChunk>();
    final var sectionAmount =
      properties.getValue(RealmFormatProperties.CHUNK_SECTION_MAX) -
        properties.getValue(RealmFormatProperties.CHUNK_SECTION_MIN) +
        1;
    final var data = RealmFormatSerializerHelperV1.readCompressed(input);
    final var chunkInput = new DataInputStream(new ByteArrayInputStream(data));
    final var chunks = chunkInput.readInt();
    for (var i = 0; i < chunks; i++) {
      final var x = chunkInput.readInt();
      final var z = chunkInput.readInt();
      final var heightMap = RealmFormatSerializerHelperV1.readCompoundTag(chunkInput);
      final var sections = RealmFormatSerializerHelperV1.readChunkSections(
        chunkInput,
        sectionAmount,
        worldVersion
      );
      result.put(
        new RealmFormatChunkPosition(x, z),
        RealmFormatChunkV1
          .builder()
          .x(x)
          .z(z)
          .sections(sections)
          .heightMaps(heightMap)
          .entities(Tag.createList())
          .tileEntities(Tag.createList())
          .build()
      );
    }
    RealmFormatSerializerHelperV1.readEntities(input, result);
    RealmFormatSerializerHelperV1.readTileEntities(input, result);
    return result;
  }

  @NotNull
  CompoundTag readCompressedCompound(@NotNull final DataInputStream input) throws IOException {
    return RealmFormatSerializerHelperV1.deserializeCompoundTag(
      RealmFormatSerializerHelperV1.readCompressed(input)
    );
  }

  void writeChunks(
    @NotNull final DataOutputStream output,
    @NotNull final RealmFormatPropertyMap properties,
    @NotNull final Collection<RealmFormatChunk> chunks,
    final byte worldVersion
  ) throws IOException {
    final var serializeChunks = RealmFormatSerializerHelperV1.serializeChunks(
      properties,
      chunks,
      worldVersion
    );
    RealmFormatSerializerHelperV1.writeCompressed(output, serializeChunks);
    RealmFormatSerializerHelperV1.writeEntities(output, chunks);
    RealmFormatSerializerHelperV1.writeTileEntities(output, chunks);
  }

  void writeCompressedCompound(
    @NotNull final DataOutputStream output,
    @NotNull final CompoundTag tag
  ) throws IOException {
    final var compound = RealmFormatSerializerHelperV1.serializeCompound(tag);
    RealmFormatSerializerHelperV1.writeCompressed(output, compound);
  }

  private boolean areSectionsEmpty(final RealmFormatChunkSection @NotNull [] sections) {
    for (final var section : sections) {
      try {
        final var compoundTag = section
          .blockDataV1_18()
          .blockStates()
          .getListTag("palette", TagTypes.COMPOUND)
          .orElse(Tag.createList())
          .getCompoundTag(0)
          .orElse(Tag.createCompound());
        if (!compoundTag.getString("Name").orElse("").equals("minecraft:air")) {
          return false;
        }
      } catch (final Exception e) {
        return false;
      }
    }
    return false;
  }

  private boolean canBePruned(
    @NotNull final RealmFormatPropertyMap properties,
    @NotNull final RealmFormatChunk chunk
  ) {
    if (properties.getValue(RealmFormatProperties.SHOULD_LIMIT_SAVE)) {
      final var minX = properties.getValue(RealmFormatProperties.SAVE_MIN_X);
      final var maxX = properties.getValue(RealmFormatProperties.SAVE_MAX_X);
      final var minZ = properties.getValue(RealmFormatProperties.SAVE_MIN_Z);
      final var maxZ = properties.getValue(RealmFormatProperties.SAVE_MAX_Z);
      final var chunkX = chunk.x();
      final var chunkZ = chunk.z();
      if (chunkX < minX || chunkX > maxX) {
        return true;
      }
      if (chunkZ < minZ || chunkZ > maxZ) {
        return true;
      }
    }
    final var pruning = properties.getValue(RealmFormatProperties.CHUNK_PRUNING);
    if (pruning.equals("aggressive")) {
      return chunk.tileEntities().isEmpty() &&
        chunk.entities().isEmpty() &&
        RealmFormatSerializerHelperV1.areSectionsEmpty(chunk.sections());
    }
    return false;
  }

  @NotNull
  private CompoundTag deserializeCompoundTag(final byte @NotNull [] bytes) throws IOException {
    if (bytes.length == 0) {
      return Tag.createCompound();
    }
    @Cleanup final var reader = Tag.createReader(new ByteArrayInputStream(bytes));
    return reader.readCompoundTag();
  }

  @NotNull
  private ListTag deserializeListTag(final byte @NotNull [] bytes) throws IOException {
    if (bytes.length == 0) {
      return Tag.createList();
    }
    @Cleanup final var reader = Tag.createReader(new ByteArrayInputStream(bytes));
    return reader.readListTag();
  }

  private RealmFormatChunkSection @NotNull [] readChunkSections(
    @NotNull final DataInputStream input,
    final int amount,
    final byte worldVersion
  ) throws IOException {
    final var sections = new RealmFormatChunkSection[amount];
    final var sectionCount = input.readInt();
    for (var sectionId = 0; sectionId < sectionCount; sectionId++) {
      final var blockLight = RealmFormatSerializerHelperV1.readOptionalNibbleArray(input);
      final var skyLight = RealmFormatSerializerHelperV1.readOptionalNibbleArray(input);
      NibbleArray data = null;
      CompoundTag blockStateTag = null;
      CompoundTag biomeTag = null;
      ListTag palette = null;
      long[] blockStates = null;
      if (worldVersion < 4) {
        final var bytes = new byte[RealmFormatSerializerHelperV1.ARRAY_SIZE];
        input.read(bytes);
        data = new NibbleArray(bytes);
      } else if (worldVersion < 8) {
        palette = RealmFormatSerializerHelperV1.readListTag(input);
        final var blockStatesArrayLength = input.readInt();
        blockStates = new long[blockStatesArrayLength];
        for (var index = 0; index < blockStatesArrayLength; index++) {
          blockStates[index] = input.readLong();
        }
      } else {
        blockStateTag = RealmFormatSerializerHelperV1.readCompoundTag(input);
        biomeTag = RealmFormatSerializerHelperV1.readCompoundTag(input);
      }
      sections[sectionId] =
        RealmFormatChunkSectionV1
          .builder()
          .blockDataV1_8(new BlockDataV1_8(data))
          .blockDataV1_14(new BlockDataV1_14(palette, blockStates))
          .blockDataV1_18(new BlockDataV1_18(biomeTag, blockStateTag))
          .blockLight(blockLight)
          .skyLight(skyLight)
          .build();
    }
    return sections;
  }

  @NotNull
  private CompoundTag readCompoundTag(@NotNull final DataInputStream input) throws IOException {
    final var bytes = new byte[input.readInt()];
    input.read(bytes);
    return RealmFormatSerializerHelperV1.deserializeCompoundTag(bytes);
  }

  private byte @NotNull [] readCompressed(@NotNull final DataInputStream input) throws IOException {
    final var compressedLength = input.readInt();
    final var resultLength = input.readInt();
    final var compressed = new byte[compressedLength];
    final var result = new byte[resultLength];
    input.read(compressed);
    Zstd.decompress(result, compressed);
    return result;
  }

  private void readEntities(
    @NotNull final DataInputStream input,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var compound = RealmFormatSerializerHelperV1.readCompressedCompound(input);
    final var entitiesCompound = compound
      .getListTag("entities", TagTypes.COMPOUND)
      .orElse(Tag.createList());
    for (final var entity : entitiesCompound) {
      final var pos = entity
        .asCompound()
        .getListTag("Pos", TagTypes.DOUBLE)
        .orElse(Tag.createList());
      final var x = pos.getInteger(0).orElse(0) >> 4;
      final var z = pos.getInteger(2).orElse(0) >> 4;
      final var chunk = chunks.get(new RealmFormatChunkPosition(x, z));
      if (chunk != null) {
        chunk.entities().add(entity);
      }
    }
  }

  @NotNull
  private ListTag readListTag(@NotNull final DataInputStream input) throws IOException {
    final var bytes = new byte[input.readInt()];
    input.read(bytes);
    return RealmFormatSerializerHelperV1.deserializeListTag(bytes);
  }

  @Nullable
  private NibbleArray readOptionalNibbleArray(@NotNull final DataInputStream input)
    throws IOException {
    if (!input.readBoolean()) {
      return null;
    }
    final var bytes = new byte[RealmFormatSerializerHelperV1.ARRAY_SIZE];
    input.read(bytes);
    return new NibbleArray(bytes);
  }

  private void readTileEntities(
    @NotNull final DataInputStream input,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var compound = RealmFormatSerializerHelperV1.readCompressedCompound(input);
    final var list = compound.getListTag("tiles", TagTypes.COMPOUND).orElse(Tag.createList());
    for (final var tileEntity : list) {
      final var tileEntityCompound = tileEntity.asCompound();
      final var x = tileEntityCompound.getInteger("x").orElse(0) >> 4;
      final var z = tileEntityCompound.getInteger("z").orElse(0) >> 4;
      final var chunk = chunks.get(new RealmFormatChunkPosition(x, z));
      if (chunk != null) {
        chunk.tileEntities().add(tileEntity);
      }
    }
  }

  private byte @NotNull [] serializeChunks(
    @NotNull final RealmFormatPropertyMap properties,
    @NotNull final Collection<RealmFormatChunk> chunks,
    final byte worldVersion
  ) throws IOException {
    @Cleanup final var outputStream = new ByteArrayOutputStream(16384);
    @Cleanup final var output = new DataOutputStream(outputStream);
    final var emptyChunks = new ArrayList<>(chunks);
    for (final var chunk : chunks) {
      if (RealmFormatSerializerHelperV1.canBePruned(properties, chunk)) {
        System.out.println("PRUNED: " + chunk);
      } else {
        emptyChunks.add(chunk);
      }
    }
    output.writeInt(chunks.size());
    for (final var chunk : emptyChunks) {
      output.writeInt(chunk.x());
      output.writeInt(chunk.z());
      RealmFormatSerializerHelperV1.writeCompound(output, chunk.heightMaps());
      RealmFormatSerializerHelperV1.writeChunkSections(output, chunk.sections(), worldVersion);
    }
    return outputStream.toByteArray();
  }

  private byte @NotNull [] serializeCompound(@NotNull final CompoundTag tag) throws IOException {
    if (tag.isEmpty()) {
      return new byte[0];
    }
    final var output = new ByteArrayOutputStream();
    @Cleanup final var writer = Tag.createWriter(output);
    writer.write(tag);
    return output.toByteArray();
  }

  private byte @NotNull [] serializeListTag(@NotNull final ListTag tag) throws IOException {
    if (tag.isEmpty()) {
      return new byte[0];
    }
    final var output = new ByteArrayOutputStream();
    @Cleanup final var writer = Tag.createWriter(output);
    writer.write(tag);
    return output.toByteArray();
  }

  private void writeChunkSections(
    @NotNull final DataOutputStream output,
    final RealmFormatChunkSection @NotNull [] sections,
    final byte worldVersion
  ) throws IOException {
    output.writeInt(sections.length);
    for (final var section : sections) {
      if (section == null) {
        continue;
      }
      RealmFormatSerializerHelperV1.writeOptionalNibbleArray(output, section.blockLight());
      RealmFormatSerializerHelperV1.writeOptionalNibbleArray(output, section.skyLight());
      if (worldVersion < 4) {
        output.write(section.blockDataV1_8().data().backing());
      } else if (worldVersion < 8) {
        RealmFormatSerializerHelperV1.writeListTag(output, section.blockDataV1_14().palette());
        final var blockStates = section.blockDataV1_14().blockStates();
        output.writeInt(blockStates.length);
        for (final var blockState : blockStates) {
          output.writeLong(blockState);
        }
      } else {
        RealmFormatSerializerHelperV1.writeCompound(output, section.blockDataV1_18().blockStates());
        RealmFormatSerializerHelperV1.writeCompound(output, section.blockDataV1_18().biomes());
      }
    }
  }

  private void writeCompound(
    @NotNull final DataOutputStream output,
    @NotNull final CompoundTag tag
  ) throws IOException {
    final var bytes = RealmFormatSerializerHelperV1.serializeCompound(tag);
    output.writeInt(bytes.length);
    output.write(bytes);
  }

  private void writeCompressed(@NotNull final DataOutputStream output, final byte @NotNull [] bytes)
    throws IOException {
    final var compressedExtra = Zstd.compress(bytes);
    output.writeInt(compressedExtra.length);
    output.writeInt(bytes.length);
    output.write(compressedExtra);
  }

  private void writeEntities(
    @NotNull final DataOutputStream output,
    @NotNull final Collection<RealmFormatChunk> chunks
  ) throws IOException {
    final var entities = Tag.createList();
    for (final var chunk : chunks) {
      for (final var entity : chunk.entities()) {
        entities.add(entity);
      }
    }
    RealmFormatSerializerHelperV1.writeCompressedCompound(
      output,
      Tag.createCompound().set("entities", entities)
    );
  }

  private void writeListTag(
    @NotNull final DataOutputStream output,
    @NotNull final ListTag tag
  ) throws IOException {
    final var bytes = RealmFormatSerializerHelperV1.serializeListTag(tag);
    output.writeInt(bytes.length);
    output.write(bytes);
  }

  private void writeOptionalNibbleArray(
    @NotNull final DataOutputStream output,
    @Nullable final NibbleArray array
  ) throws IOException {
    final var has = array != null;
    output.writeBoolean(has);
    if (has) {
      output.write(array.backing());
    }
  }

  private void writeTileEntities(
    @NotNull final DataOutputStream output,
    @NotNull final Collection<RealmFormatChunk> chunks
  ) throws IOException {
    final var entities = Tag.createList();
    for (final var chunk : chunks) {
      for (final var entity : chunk.tileEntities()) {
        entities.add(entity);
      }
    }
    RealmFormatSerializerHelperV1.writeCompressedCompound(
      output,
      Tag.createCompound().set("tiles", entities)
    );
  }
}
