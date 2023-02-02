package io.github.portlek.realmformat.format.realm.v1;

import com.github.luben.zstd.Zstd;
import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.property.RealmFormatProperties;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import io.github.shiruka.nbt.TagTypes;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
class RealmFormatSerializerHelperV1 {

  private final int ARRAY_SIZE = 16 * 16 * 16 / (8 / 4);

  @NotNull
  Map<RealmFormatChunkPosition, RealmFormatChunk> readChunks(
    @NotNull final DataInputStream compressedInput,
    @NotNull final RealmFormatPropertyMap properties
  ) throws IOException {
    final var data = RealmFormatSerializerHelperV1.readCompressed(compressedInput);
    final var result = new HashMap<RealmFormatChunkPosition, RealmFormatChunk>();
    final var input = new DataInputStream(new ByteArrayInputStream(data));
    final var chunks = input.readInt();
    for (var i = 0; i < chunks; i++) {
      final var x = input.readInt();
      final var z = input.readInt();
      final var heightMapData = new byte[input.readInt()];
      input.read(heightMapData);
      final var heightMap = RealmFormatSerializerHelperV1.readCompound(heightMapData);
      final var sectionAmount =
        properties.getValue(RealmFormatProperties.CHUNK_SECTION_MAX) -
        properties.getValue(RealmFormatProperties.CHUNK_SECTION_MIN) +
        1;
      final var sections = new RealmFormatChunkSection[sectionAmount];
      final var sectionCount = input.readInt();
      for (var sectionId = 0; sectionId < sectionCount; sectionId++) {
        final NibbleArray blockLightArray;
        if (input.readBoolean()) {
          final var blockLightByteArray = new byte[RealmFormatSerializerHelperV1.ARRAY_SIZE];
          input.read(blockLightByteArray);
          blockLightArray = new NibbleArray(blockLightByteArray);
        } else {
          blockLightArray = null;
        }
        final NibbleArray skyLightArray;
        if (input.readBoolean()) {
          final var skyLightByteArray = new byte[RealmFormatSerializerHelperV1.ARRAY_SIZE];
          input.read(skyLightByteArray);
          skyLightArray = new NibbleArray(skyLightByteArray);
        } else {
          skyLightArray = null;
        }
        final var blockStateData = new byte[input.readInt()];
        input.read(blockStateData);
        final var blockStateTag = RealmFormatSerializerHelperV1.readCompound(blockStateData);
        final var biomeData = new byte[input.readInt()];
        input.read(biomeData);
        final var biomeTag = RealmFormatSerializerHelperV1.readCompound(biomeData);
        sections[sectionId] =
          RealmFormatChunkSectionV1
            .builder()
            .blockStates(blockStateTag)
            .biomes(biomeTag)
            .blockLight(blockLightArray)
            .skyLight(skyLightArray)
            .build();
      }
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
  CompoundTag readExtra(@NotNull final DataInputStream input) throws IOException {
    return RealmFormatSerializerHelperV1.readCompound(
      RealmFormatSerializerHelperV1.readCompressed(input)
    );
  }

  void writeChunks(
    @NotNull final DataOutputStream output,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks,
    @NotNull final RealmFormatPropertyMap properties
  ) throws IOException {}

  void writeExtra(@NotNull final DataOutputStream output, @NotNull final CompoundTag extra)
    throws IOException {
    final var serializedExtra = RealmFormatSerializerHelperV1.serializeCompound(extra);
    RealmFormatSerializerHelperV1.writeCompresses(output, serializedExtra);
  }

  @NotNull
  private CompoundTag readCompound(final byte@NotNull[] bytes) throws IOException {
    if (bytes.length == 0) {
      return Tag.createCompound();
    }
    @Cleanup
    final var reader = Tag.createReader(new ByteArrayInputStream(bytes));
    return reader.readCompoundTag();
  }

  private byte@NotNull[] readCompressed(@NotNull final DataInputStream input) throws IOException {
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
    final var entities = RealmFormatSerializerHelperV1.readCompressed(input);
    final var compound = RealmFormatSerializerHelperV1.readCompound(entities);
    final var entitiesCompound = compound
      .getListTag("entities", TagTypes.COMPOUND)
      .orElse(Tag.createList());
    for (final var entity : entitiesCompound) {
      final var pos = entity.asCompound().getListTag("Pos", TagTypes.INT).orElse(Tag.createList());
      final var x = pos.getInteger(0).orElse(0) >> 4;
      final var z = pos.getInteger(2).orElse(0) >> 4;
      final var chunk = chunks.get(new RealmFormatChunkPosition(x, z));
      if (chunk != null) {
        chunk.entities().add(entity);
      }
    }
  }

  private void readTileEntities(
    @NotNull final DataInputStream input,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var compressedTileEntities = RealmFormatSerializerHelperV1.readCompressed(input);
    final var compound = RealmFormatSerializerHelperV1.readCompound(compressedTileEntities);
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

  private byte@NotNull[] serializeCompound(@NotNull final CompoundTag tag) throws IOException {
    if (tag.isEmpty()) {
      return new byte[0];
    }
    final var output = new ByteArrayOutputStream();
    final var writer = Tag.createWriter(output);
    writer.write(tag);
    return output.toByteArray();
  }

  private void writeCompresses(@NotNull final DataOutputStream output, final byte@NotNull[] bytes)
    throws IOException {
    final var compressedExtra = Zstd.compress(bytes);
    output.writeInt(compressedExtra.length);
    output.writeInt(bytes.length);
    output.write(compressedExtra);
  }
}
