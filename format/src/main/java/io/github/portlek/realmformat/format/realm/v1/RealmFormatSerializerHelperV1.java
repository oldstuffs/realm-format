package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.misc.InputStreamExtension;
import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.misc.OutputStreamExtension;
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

@UtilityClass
class RealmFormatSerializerHelperV1 {

  @NotNull
  Map<RealmFormatChunkPosition, RealmFormatChunk> readChunks(
    @NotNull final InputStreamExtension input,
    @NotNull final RealmFormatPropertyMap properties,
    final byte worldVersion
  ) throws IOException {
    final var result = new Object2ObjectOpenHashMap<RealmFormatChunkPosition, RealmFormatChunk>();
    final var sectionAmount =
      properties.getValue(RealmFormatProperties.CHUNK_SECTION_MAX) -
      properties.getValue(RealmFormatProperties.CHUNK_SECTION_MIN) +
      1;
    final var data = input.readCompressed();
    @Cleanup
    final var chunkInput = new InputStreamExtension(
      new DataInputStream(new ByteArrayInputStream(data))
    );
    final var chunks = chunkInput.readInt();
    for (var i = 0; i < chunks; i++) {
      final var x = chunkInput.readInt();
      final var z = chunkInput.readInt();
      final var heightMap = chunkInput.readCompoundTag();
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

  void writeChunks(
    @NotNull final OutputStreamExtension output,
    @NotNull final RealmFormatPropertyMap properties,
    @NotNull final Collection<RealmFormatChunk> chunks,
    final byte worldVersion
  ) throws IOException {
    final var serializeChunks = RealmFormatSerializerHelperV1.serializeChunks(
      properties,
      chunks,
      worldVersion
    );
    output.writeCompressed(serializeChunks);
    RealmFormatSerializerHelperV1.writeEntities(output, chunks);
    RealmFormatSerializerHelperV1.writeTileEntities(output, chunks);
  }

  private boolean areSectionsEmpty(final RealmFormatChunkSection@NotNull[] sections) {
    for (final var section : sections) {
      try {
        final var compoundTag = section
          .blockDataV1_18()
          .blockStates()
          .getListTag("palette", TagTypes.COMPOUND)
          .orElseThrow()
          .getCompoundTag(0)
          .orElseThrow();
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
      return (
        chunk.tileEntities().isEmpty() &&
        chunk.entities().isEmpty() &&
        RealmFormatSerializerHelperV1.areSectionsEmpty(chunk.sections())
      );
    }
    return false;
  }

  private RealmFormatChunkSection@NotNull[] readChunkSections(
    @NotNull final InputStreamExtension input,
    final int amount,
    final byte worldVersion
  ) throws IOException {
    final var sections = new RealmFormatChunkSection[amount];
    final var sectionCount = input.readInt();
    for (var sectionId = 0; sectionId < sectionCount; sectionId++) {
      final var blockLight = input.readOptionalNibbleArray();
      final var skyLight = input.readOptionalNibbleArray();
      NibbleArray data = null;
      CompoundTag blockStateTag = null;
      CompoundTag biomeTag = null;
      ListTag palette = null;
      long[] blockStates = null;
      if (worldVersion < 4) {
        data = input.readNibbleArray();
      } else if (worldVersion < 8) {
        palette = input.readListTag();
        blockStates = input.readLongArray();
      } else {
        blockStateTag = input.readCompoundTag();
        biomeTag = input.readCompoundTag();
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

  private void readEntities(
    @NotNull final InputStreamExtension input,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var compound = input.readCompressedCompound();
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

  private void readTileEntities(
    @NotNull final InputStreamExtension input,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var compound = input.readCompressedCompound();
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

  private byte@NotNull[] serializeChunks(
    @NotNull final RealmFormatPropertyMap properties,
    @NotNull final Collection<RealmFormatChunk> chunks,
    final byte worldVersion
  ) throws IOException {
    final var outputStream = new ByteArrayOutputStream(16384);
    @Cleanup
    final var output = new OutputStreamExtension(new DataOutputStream(outputStream));
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
      output.writeCompound(chunk.heightMaps());
      RealmFormatSerializerHelperV1.writeChunkSections(output, chunk.sections(), worldVersion);
    }
    return outputStream.toByteArray();
  }

  private void writeChunkSections(
    @NotNull final OutputStreamExtension output,
    final RealmFormatChunkSection@NotNull[] sections,
    final byte worldVersion
  ) throws IOException {
    output.writeInt(sections.length);
    for (final var section : sections) {
      if (section == null) {
        continue;
      }
      output.writeOptionalNibbleArray(section.blockLight());
      output.writeOptionalNibbleArray(section.skyLight());
      if (worldVersion < 4) {
        output.write(section.blockDataV1_8().data().backing());
      } else if (worldVersion < 8) {
        output.writeListTag(section.blockDataV1_14().palette());
        output.writeLongArray(section.blockDataV1_14().blockStates());
      } else {
        output.writeCompound(section.blockDataV1_18().blockStates());
        output.writeCompound(section.blockDataV1_18().biomes());
      }
    }
  }

  private void writeEntities(
    @NotNull final OutputStreamExtension output,
    @NotNull final Collection<RealmFormatChunk> chunks
  ) throws IOException {
    final var entities = Tag.createList();
    for (final var chunk : chunks) {
      for (final var entity : chunk.entities()) {
        entities.add(entity);
      }
    }
    output.writeCompressedCompound(Tag.createCompound().set("entities", entities));
  }

  private void writeTileEntities(
    @NotNull final OutputStreamExtension output,
    @NotNull final Collection<RealmFormatChunk> chunks
  ) throws IOException {
    final var entities = Tag.createList();
    for (final var chunk : chunks) {
      for (final var entity : chunk.tileEntities()) {
        entities.add(entity);
      }
    }
    output.writeCompressedCompound(Tag.createCompound().set("tiles", entities));
  }
}
