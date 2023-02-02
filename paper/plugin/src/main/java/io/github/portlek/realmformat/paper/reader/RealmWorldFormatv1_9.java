package io.github.portlek.realmformat.paper.reader;

import com.github.luben.zstd.Zstd;
import io.github.portlek.realmformat.format.exception.CorruptedWorldException;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.misc.Misc;
import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmChunk;
import io.github.portlek.realmformat.format.realm.RealmChunkSection;
import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.format.realm.RealmWorldReader;
import io.github.portlek.realmformat.format.realm.impl.RealmChunkImpl;
import io.github.portlek.realmformat.format.realm.impl.RealmChunkSectionImpl;
import io.github.portlek.realmformat.paper.misc.Services;
import io.github.portlek.realmformat.paper.nms.RealmNmsBackend;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log
public final class RealmWorldFormatv1_9 implements RealmWorldReader {

  private final RealmNmsBackend nms = Services.load(RealmNmsBackend.class);

  private static int floor(final double num) {
    final var floor = (int) num;
    return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
  }

  @NotNull
  private static ChunkSectionData readChunkSections(
    @NotNull final DataInputStream dataStream,
    final byte worldVersion,
    final int version
  ) throws IOException {
    final var chunkSectionArray = new RealmChunkSection[16];
    final var sectionBitmask = new byte[2];
    dataStream.read(sectionBitmask);
    final var sectionBitset = BitSet.valueOf(sectionBitmask);
    for (var i = 0; i < 16; i++) {
      if (sectionBitset.get(i)) {
        final NibbleArray blockLightArray;
        if (version < 5 || dataStream.readBoolean()) {
          final var blockLightByteArray = new byte[2048];
          dataStream.read(blockLightByteArray);
          blockLightArray = new NibbleArray(blockLightByteArray);
        } else {
          blockLightArray = null;
        }
        final var paletteTag = Tag.createList();
        final long[] blockStatesArray;
        final var paletteLength = dataStream.readInt();
        for (var index = 0; index < paletteLength; index++) {
          final var tagLength = dataStream.readInt();
          final var serializedTag = new byte[tagLength];
          dataStream.read(serializedTag);
          final var tag = RealmWorldFormatv1_9.readCompoundTag(serializedTag);
          if (tag != null) {
            paletteTag.add(tag);
          }
        }
        final var blockStatesArrayLength = dataStream.readInt();
        blockStatesArray = new long[blockStatesArrayLength];
        for (var index = 0; index < blockStatesArrayLength; index++) {
          blockStatesArray[index] = dataStream.readLong();
        }
        final NibbleArray skyLightArray;
        if (version < 5 || dataStream.readBoolean()) {
          final var skyLightByteArray = new byte[2048];
          dataStream.read(skyLightByteArray);
          skyLightArray = new NibbleArray(skyLightByteArray);
        } else {
          skyLightArray = null;
        }
        if (version < 4) {
          final var hypixelBlocksLength = dataStream.readShort();
          dataStream.skip(hypixelBlocksLength);
        }
        chunkSectionArray[i] =
          new RealmChunkSectionImpl(
            null,
            null,
            paletteTag,
            blockStatesArray,
            blockLightArray,
            skyLightArray,
            null,
            null
          );
      }
    }
    return new ChunkSectionData(chunkSectionArray, 0, 16);
  }

  @NotNull
  private static ChunkSectionData readChunkSectionsNew(
    @NotNull final DataInputStream dataStream,
    final int worldVersion,
    final int version
  ) throws IOException {
    final var minSectionY = dataStream.readInt();
    final var maxSectionY = dataStream.readInt();
    final var sectionCount = dataStream.readInt();
    final var chunkSectionArray = new RealmChunkSection[maxSectionY - minSectionY];
    for (var i = 0; i < sectionCount; i++) {
      final var y = dataStream.readInt();
      final NibbleArray blockLightArray;
      if (version < 5 || dataStream.readBoolean()) {
        final var blockLightByteArray = new byte[2048];
        dataStream.read(blockLightByteArray);
        blockLightArray = new NibbleArray(blockLightByteArray);
      } else {
        blockLightArray = null;
      }
      final var blockStateData = new byte[dataStream.readInt()];
      dataStream.read(blockStateData);
      final var blockStateTag = RealmWorldFormatv1_9.readCompoundTag(blockStateData);
      final var biomeData = new byte[dataStream.readInt()];
      dataStream.read(biomeData);
      final var biomeTag = RealmWorldFormatv1_9.readCompoundTag(biomeData);
      final NibbleArray skyLightArray;
      if (version < 5 || dataStream.readBoolean()) {
        final var skyLightByteArray = new byte[2048];
        dataStream.read(skyLightByteArray);
        skyLightArray = new NibbleArray(skyLightByteArray);
      } else {
        skyLightArray = null;
      }
      if (version < 4) {
        final var hypixelBlocksLength = dataStream.readShort();
        dataStream.skip(hypixelBlocksLength);
      }
      chunkSectionArray[y] =
        new RealmChunkSectionImpl(
          null,
          null,
          null,
          null,
          blockLightArray,
          skyLightArray,
          blockStateTag,
          biomeTag
        );
    }
    return new ChunkSectionData(chunkSectionArray, minSectionY, maxSectionY);
  }

  @NotNull
  private static Long2ObjectOpenHashMap<RealmChunk> readChunks(
    final byte worldVersion,
    final int version,
    @NotNull final String worldName,
    final int minX,
    final int minZ,
    final int width,
    final int depth,
    @NotNull final BitSet chunkBitset,
    final byte @NotNull [] chunkData
  ) throws IOException {
    @Cleanup final var dataStream = new DataInputStream(new ByteArrayInputStream(chunkData));
    final var chunkMap = new Long2ObjectOpenHashMap<RealmChunk>();
    for (var z = 0; z < depth; z++) {
      for (var x = 0; x < width; x++) {
        final var bitsetIndex = z * width + x;
        if (chunkBitset.get(bitsetIndex)) {
          CompoundTag heightMaps;
          if (worldVersion >= 0x04) {
            final var heightMapsLength = dataStream.readInt();
            final var heightMapsArray = new byte[heightMapsLength];
            dataStream.read(heightMapsArray);
            heightMaps = RealmWorldFormatv1_9.readCompoundTag(heightMapsArray);
            if (heightMaps == null) {
              heightMaps = Tag.createCompound();
            }
          } else {
            final var heightMap = new int[256];
            for (var i = 0; i < 256; i++) {
              heightMap[i] = dataStream.readInt();
            }
            heightMaps = Tag.createCompound();
            heightMaps.setIntArray("heightMap", heightMap);
          }
          int[] biomes = null;
          if (version == 8 && worldVersion < 0x04) {
            dataStream.readInt();
          }
          if (worldVersion < 0x04) {
            final var byteBiomes = new byte[256];
            dataStream.read(byteBiomes);
            biomes = RealmWorldFormatv1_9.toIntArray(byteBiomes);
          } else if (worldVersion < 0x08) {
            final var biomesArrayLength = version >= 8 ? dataStream.readInt() : 256;
            biomes = new int[biomesArrayLength];
            for (var i = 0; i < biomes.length; i++) {
              biomes[i] = dataStream.readInt();
            }
          }
          final var data = worldVersion < 0x08
            ? RealmWorldFormatv1_9.readChunkSections(dataStream, worldVersion, version)
            : RealmWorldFormatv1_9.readChunkSectionsNew(dataStream, worldVersion, version);
          final var chunkX = minX + x;
          final var chunkZ = minZ + z;
          chunkMap.put(
            Misc.asLong(chunkX, chunkZ),
            new RealmChunkImpl(
              chunkX,
              chunkZ,
              data.sections,
              heightMaps,
              biomes,
              Tag.createList(),
              Tag.createList(),
              data.minSectionY,
              data.maxSectionY,
              null
            )
          );
        }
      }
    }
    return chunkMap;
  }

  @Nullable
  private static CompoundTag readCompoundTag(final byte @NotNull [] serializedCompound)
    throws IOException {
    if (serializedCompound.length == 0) {
      return null;
    }
    @Cleanup final var stream = Tag.createReader(new ByteArrayInputStream(serializedCompound));
    return stream.readCompoundTag();
  }

  private static int @NotNull [] toIntArray(final byte @NotNull [] buf) {
    final var buffer = ByteBuffer.wrap(buf).order(ByteOrder.BIG_ENDIAN);
    final var ret = new int[buf.length / 4];
    buffer.asIntBuffer().get(ret);
    return ret;
  }

  @NotNull
  @Override
  public RealmWorld read(
    final byte version,
    @NotNull final RealmLoader loader,
    @NotNull final String worldName,
    @NotNull final DataInputStream dataStream,
    @NotNull final RealmPropertyMap propertyMap,
    final boolean readOnly
  ) throws IOException, CorruptedWorldException {
    try {
      final byte worldVersion;
      if (version >= 6) {
        worldVersion = dataStream.readByte();
      } else if (version >= 4) {
        worldVersion = (byte) (dataStream.readBoolean() ? 0x04 : 0x01);
      } else {
        worldVersion = 0;
      }
      final var minX = dataStream.readShort();
      final var minZ = dataStream.readShort();
      final int width = dataStream.readShort();
      final int depth = dataStream.readShort();
      if (width <= 0 || depth <= 0) {
        throw new CorruptedWorldException(worldName);
      }
      final var bitmaskSize = (int) Math.ceil((width * depth) / 8.0D);
      final var chunkBitmask = new byte[bitmaskSize];
      dataStream.read(chunkBitmask);
      final var chunkBitset = BitSet.valueOf(chunkBitmask);
      final var compressedChunkDataLength = dataStream.readInt();
      final var chunkDataLength = dataStream.readInt();
      final var compressedChunkData = new byte[compressedChunkDataLength];
      final var chunkData = new byte[chunkDataLength];
      dataStream.read(compressedChunkData);
      final var compressedTileEntitiesLength = dataStream.readInt();
      final var tileEntitiesLength = dataStream.readInt();
      final var compressedTileEntities = new byte[compressedTileEntitiesLength];
      final var tileEntities = new byte[tileEntitiesLength];
      dataStream.read(compressedTileEntities);
      var compressedEntities = new byte[0];
      var entities = new byte[0];
      if (version >= 3) {
        final var hasEntities = dataStream.readBoolean();
        if (hasEntities) {
          final var compressedEntitiesLength = dataStream.readInt();
          final var entitiesLength = dataStream.readInt();
          compressedEntities = new byte[compressedEntitiesLength];
          entities = new byte[entitiesLength];
          dataStream.read(compressedEntities);
        }
      }
      var compressedExtraTag = new byte[0];
      var extraTag = new byte[0];
      if (version >= 2) {
        final var compressedExtraTagLength = dataStream.readInt();
        final var extraTagLength = dataStream.readInt();
        compressedExtraTag = new byte[compressedExtraTagLength];
        extraTag = new byte[extraTagLength];
        dataStream.read(compressedExtraTag);
      }
      var compressedMapsTag = new byte[0];
      var mapsTag = new byte[0];
      if (version >= 7) {
        final var compressedMapsTagLength = dataStream.readInt();
        final var mapsTagLength = dataStream.readInt();
        compressedMapsTag = new byte[compressedMapsTagLength];
        mapsTag = new byte[mapsTagLength];
        dataStream.read(compressedMapsTag);
      }
      if (dataStream.read() != -1) {
        throw new CorruptedWorldException(worldName);
      }
      Zstd.decompress(chunkData, compressedChunkData);
      Zstd.decompress(tileEntities, compressedTileEntities);
      Zstd.decompress(entities, compressedEntities);
      Zstd.decompress(extraTag, compressedExtraTag);
      Zstd.decompress(mapsTag, compressedMapsTag);
      final var chunks = RealmWorldFormatv1_9.readChunks(
        worldVersion,
        version,
        worldName,
        minX,
        minZ,
        width,
        depth,
        chunkBitset,
        chunkData
      );
      final var entitiesCompound = RealmWorldFormatv1_9.readCompoundTag(entities);
      final var entityStorage = new Long2ObjectOpenHashMap<ListTag>();
      if (entitiesCompound != null) {
        final var serializedEntities = entitiesCompound
          .getListTag("entities")
          .orElse(Tag.createList());
        RealmWorldFormatv1_9.log.warning("Serialized entities: " + serializedEntities);
        for (final var entityCompound : serializedEntities) {
          final var compoundTag = entityCompound.asCompound();
          final var listTag = compoundTag.getList("Pos").get();
          final var chunkX = RealmWorldFormatv1_9.floor(listTag.get(0).asInt().intValue()) >> 4;
          final var chunkZ = RealmWorldFormatv1_9.floor(listTag.get(2).asInt().intValue()) >> 4;
          final var chunkKey = Misc.asLong(chunkX, chunkZ);
          final var chunk = chunks.get(chunkKey);
          if (chunk != null) {
            chunk.entities().add(compoundTag);
          }
          if (entityStorage.containsKey(chunkKey)) {
            entityStorage.get(chunkKey).add(compoundTag);
          } else {
            final var entityStorageList = Tag.createList();
            entityStorageList.add(compoundTag);
            entityStorage.put(chunkKey, entityStorageList);
          }
        }
      }
      final var tileEntitiesCompound = RealmWorldFormatv1_9.readCompoundTag(tileEntities);
      if (tileEntitiesCompound != null) {
        final var tileEntitiesList = tileEntitiesCompound
          .getListTag("tiles")
          .orElse(Tag.createList());
        for (final var tileEntityCompound : tileEntitiesList) {
          final var compoundTag = tileEntityCompound.asCompound();
          final var chunkX = compoundTag.getInteger("x").get() >> 4;
          final var chunkZ = compoundTag.getInteger("z").get() >> 4;
          final var chunkKey = Misc.asLong(chunkX, chunkZ);
          final var chunk = chunks.get(chunkKey);
          CorruptedWorldException.check(chunk != null, worldName);
          chunk.tileEntities().add(compoundTag);
        }
      }
      var extraCompound = RealmWorldFormatv1_9.readCompoundTag(extraTag);
      if (extraCompound == null) {
        extraCompound = Tag.createCompound();
      }
      final var mapsCompound = RealmWorldFormatv1_9.readCompoundTag(mapsTag);
      final ListTag mapList;
      if (mapsCompound != null) {
        mapList = mapsCompound.getListTag("maps").orElse(Tag.createList());
      } else {
        mapList = Tag.createList();
      }
      var worldPropertyMap = propertyMap;
      final var propertiesMap = extraCompound.getCompoundTag("properties");
      if (propertiesMap.isPresent()) {
        worldPropertyMap = new RealmPropertyMap(propertiesMap.get());
        worldPropertyMap.merge(propertyMap);
      }
      return this.nms.createRealmWorld(
        loader,
        worldName,
        chunks,
        extraCompound,
        mapList,
        worldVersion,
        worldPropertyMap,
        readOnly,
        !readOnly,
        entityStorage
      );
    } catch (final EOFException ex) {
      throw new CorruptedWorldException(worldName, ex);
    }
  }

  @Data
  private static final class ChunkSectionData {

    private final int maxSectionY;

    private final int minSectionY;

    private final RealmChunkSection[] sections;

    private ChunkSectionData(
      final RealmChunkSection[] sections,
      final int minSectionY,
      final int maxSectionY
    ) {
      this.sections = sections;
      this.minSectionY = minSectionY;
      this.maxSectionY = maxSectionY;
    }
  }
}
