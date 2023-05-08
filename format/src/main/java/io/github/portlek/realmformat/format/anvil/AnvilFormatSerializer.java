package io.github.portlek.realmformat.format.anvil;

import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.property.RealmFormatProperties;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.*;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkSectionV1;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkV1;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatWorldV1;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class AnvilFormatSerializer {

  private final int SECTOR_SIZE = 4096;

  @NotNull
  public RealmFormatWorld deserialize(@NotNull final Path worldDirectory) throws IOException {
    final var levelPath = worldDirectory.resolve("level.dat");
    final var regionPath = worldDirectory.resolve("region");
    final var entitiesPath = worldDirectory.resolve("entities");
    final var levelData = AnvilFormatSerializer.readLevelData(levelPath);
    final var worldVersion = levelData.version();
    if (!Files.exists(regionPath) || !Files.isDirectory(regionPath)) {
      throw new IllegalArgumentException("'region' directory not found or it's not a directory!");
    }
    final var chunks = new HashMap<RealmFormatChunkPosition, RealmFormatChunk>();
    try (final var regionPathsStream = Files.list(regionPath)) {
      final var regionPaths = regionPathsStream
        .filter(name -> name.toString().endsWith(".mca"))
        .toList();
      for (final var path : regionPaths) {
        chunks.putAll(AnvilFormatSerializer.loadChunks(path, worldVersion));
      }
    }
    if (chunks.isEmpty()) {
      throw new IllegalArgumentException("Chunks not found!");
    }
    if (Files.exists(entitiesPath)) {
      try (final var entityPathsStream = Files.list(entitiesPath)) {
        final var entityPaths = entityPathsStream
          .filter(name -> name.toString().endsWith(".mca"))
          .toList();
        for (final var path : entityPaths) {
          AnvilFormatSerializer.loadEntities(path, worldVersion, chunks);
        }
      }
    }
    final var extra = Tag.createCompound();
    final var properties = new RealmFormatPropertyMap();
    properties.setValue(RealmFormatProperties.SPAWN_X, levelData.spawnX());
    properties.setValue(RealmFormatProperties.SPAWN_Y, levelData.spawnY());
    properties.setValue(RealmFormatProperties.SPAWN_Z, levelData.spawnZ());
    return RealmFormatWorldV1
      .builder()
      .worldVersion(worldVersion)
      .chunks(chunks)
      .properties(properties)
      .extra(extra)
      .build();
  }

  private boolean isEmpty(final long@NotNull[] array) {
    return Arrays.stream(array).noneMatch(b -> b != 0L);
  }

  private boolean isEmpty(final byte[] array) {
    for (final var b : array) {
      if (b != 0) {
        return false;
      }
    }
    return true;
  }

  @NotNull
  private Map<RealmFormatChunkPosition, RealmFormatChunk> loadChunks(
    @NotNull final Path path,
    final int worldVersion
  ) throws IOException {
    final var regionByteArray = Files.readAllBytes(path);
    @Cleanup
    final var inputStream = new DataInputStream(new ByteArrayInputStream(regionByteArray));
    final var chunkEntries = new ArrayList<AnvilFormatChunkEntry>(1024);
    for (var i = 0; i < 1024; i++) {
      final var entry = inputStream.readInt();
      final var chunkOffset = entry >>> 8;
      final var chunkSize = entry & 15;
      if (entry != 0) {
        final var chunkEntry = new AnvilFormatChunkEntry(
          chunkOffset * AnvilFormatSerializer.SECTOR_SIZE,
          chunkSize * AnvilFormatSerializer.SECTOR_SIZE
        );
        chunkEntries.add(chunkEntry);
      }
    }
    final var chunks = new HashMap<RealmFormatChunkPosition, RealmFormatChunk>();
    for (final var entry : chunkEntries) {
      @Cleanup
      final var headerStream = new DataInputStream(
        new ByteArrayInputStream(regionByteArray, entry.offset(), entry.paddedSize())
      );
      final var chunkSize = headerStream.readInt() - 1;
      final var compressionScheme = headerStream.readByte();
      final var chunkStream = new DataInputStream(
        new ByteArrayInputStream(regionByteArray, entry.offset() + 5, chunkSize)
      );
      final var decompressorStream = compressionScheme == 1
        ? new GZIPInputStream(chunkStream)
        : new InflaterInputStream(chunkStream);
      final CompoundTag tag;
      try (final var reader = Tag.createReader(decompressorStream)) {
        tag = reader.readCompoundTag();
      }
      var global = tag.getCompoundTag("").orElseThrow();
      final var innerLevel = global.getCompoundTag("Level");
      if (innerLevel.isPresent()) {
        global = innerLevel.get();
      }
      final var chunk = AnvilFormatSerializer.readChunk(global, worldVersion);
      if (chunk != null) {
        chunks.put(new RealmFormatChunkPosition(chunk.x(), chunk.z()), chunk);
      }
    }
    return chunks;
  }

  private void loadEntities(
    @NotNull final Path path,
    final int version,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var regionByteArray = Files.readAllBytes(path);
    @Cleanup
    final var input = new DataInputStream(new ByteArrayInputStream(regionByteArray));
    final var chunkEntries = new ArrayList<AnvilFormatChunkEntry>(1024);
    for (var i = 0; i < 1024; i++) {
      final var entry = input.readInt();
      if (entry != 0) {
        final var chunkOffset = entry >>> 8;
        final var chunkSize = entry & 15;
        chunkEntries.add(
          new AnvilFormatChunkEntry(
            chunkOffset * AnvilFormatSerializer.SECTOR_SIZE,
            chunkSize * AnvilFormatSerializer.SECTOR_SIZE
          )
        );
      }
    }
    for (final var entry : chunkEntries) {
      @Cleanup
      final var headerStream = new DataInputStream(
        new ByteArrayInputStream(regionByteArray, entry.offset(), entry.paddedSize())
      );
      final var chunkSize = headerStream.readInt() - 1;
      final int compressionScheme = headerStream.readByte();
      final var chunkStream = new DataInputStream(
        new ByteArrayInputStream(regionByteArray, entry.offset() + 5, chunkSize)
      );
      final var decompressorStream = compressionScheme == 1
        ? new GZIPInputStream(chunkStream)
        : new InflaterInputStream(chunkStream);
      @Cleanup
      final var nbtStream = Tag.createReader(decompressorStream);
      final var globalCompound = nbtStream.readCompoundTag().getCompoundTag("").orElseThrow();
      AnvilFormatSerializer.readEntityChunk(globalCompound, version, chunks);
    }
  }

  @Nullable
  private RealmFormatChunk readChunk(@NotNull final CompoundTag compound, final int worldVersion) {
    final var chunkX = compound.getInteger("xPos").orElseThrow();
    final var chunkZ = compound.getInteger("zPos").orElseThrow();
    if (worldVersion >= 8) {
      final var dataVersion = RealmFormat.dataVersionToWorldVersion(
        compound.getInteger("DataVersion").orElse(-1)
      );
      if (dataVersion != worldVersion) {
        System.err.printf(
          "Cannot load chunk at %s,%s: data version %s does not match world version %s%n",
          chunkX,
          chunkZ,
          dataVersion,
          worldVersion
        );
        return null;
      }
    }
    final var status = compound.getString("Status");
    if (
      status.isPresent() &&
      !status.get().equals("postprocessed") &&
      !status.get().startsWith("full")
    ) {
      return null;
    }
    final int[] biomes;
    final var biomesTag = compound.get("Biomes").orElse(Tag.createEnd());
    if (biomesTag.isIntArray()) {
      biomes = biomesTag.asIntArray().primitiveValue();
    } else if (biomesTag.isByteArray()) {
      biomes = AnvilFormatSerializer.toIntArray(biomesTag.asByteArray().primitiveValue());
    } else {
      biomes = null;
    }
    final CompoundTag heightMapsCompound;
    if (worldVersion >= 4) {
      heightMapsCompound = compound.getCompoundTag("Heightmaps").orElse(Tag.createCompound());
    } else {
      final var heightMap = compound.getIntArray("HeightMap").orElseGet(() -> new int[256]);
      heightMapsCompound = Tag.createCompound().set("heightMap", Tag.createIntArray(heightMap));
    }
    final ListTag tileEntities;
    final ListTag entities;
    final ListTag sections;
    var minSection = 0;
    var maxSection = 16;
    if (worldVersion < 8) {
      tileEntities = compound.getListTag("TileEntities").orElse(Tag.createList());
      entities = compound.getListTag("Entities").orElse(Tag.createList());
      sections = compound.getListTag("Sections").orElseThrow();
    } else {
      tileEntities = compound.getListTag("block_entities").orElse(Tag.createList());
      entities = compound.getListTag("entities").orElse(Tag.createList());
      sections = compound.getListTag("sections").orElseThrow();
      final var yPos = compound.get("yPos").orElseThrow();
      if (yPos.isByte()) {
        minSection = yPos.asByte().byteValue();
      } else {
        minSection = yPos.asInt().intValue();
      }
      maxSection =
        sections
          .stream()
          .map(c -> c.asCompound().getByte("Y").orElseThrow())
          .max(Byte::compareTo)
          .orElse((byte) 0) +
        1;
    }
    final var sectionArray = new RealmFormatChunkSection[maxSection - minSection];
    for (final var section : sections) {
      final var compoundTag = section.asCompound();
      final var index = compoundTag.getByte("Y").orElseThrow();
      if (worldVersion < 7 && index < 0) {
        continue;
      }
      final var builder = RealmFormatChunkSectionV1.builder();
      if (worldVersion < 4) {
        final var data = compoundTag.getByteArray("Data").orElseThrow();
        if (AnvilFormatSerializer.isEmpty(data)) {
          continue;
        }
        builder.blockDataV1_8(new BlockDataV1_8(new NibbleArray(data)));
      } else if (worldVersion < 8) {
        final var palette = compoundTag.getListTag("Palette").orElse(null);
        final var blockStates = compoundTag.getLongArray("BlockStates").orElse(null);
        if (palette == null || blockStates == null || AnvilFormatSerializer.isEmpty(blockStates)) {
          continue;
        }
        builder.blockDataV1_14(new BlockDataV1_14(palette, blockStates));
      } else {
        final var blockStatesOptional = compoundTag.getCompoundTag("block_states");
        final var biomesOptional = compoundTag.getCompoundTag("biomes");
        if (blockStatesOptional.isEmpty() || biomesOptional.isEmpty()) {
          continue;
        }
        builder.blockDataV1_18(
          new BlockDataV1_18(blockStatesOptional.orElseThrow(), biomesOptional.orElseThrow())
        );
      }
      final var blockLightArray = compoundTag
        .getByteArray("BlockLight")
        .map(NibbleArray::new)
        .orElse(null);
      final var skyLightArray = compoundTag
        .getByteArray("SkyLight")
        .map(NibbleArray::new)
        .orElse(null);
      sectionArray[index - minSection] =
        builder.blockLight(blockLightArray).skyLight(skyLightArray).build();
    }
    for (final var section : sectionArray) {
      if (section != null) {
        return RealmFormatChunkV1
          .builder()
          .x(chunkX)
          .z(chunkZ)
          .sections(sectionArray)
          .heightMaps(heightMapsCompound)
          .biomes(biomes)
          .tileEntities(tileEntities)
          .entities(entities)
          .minSection(minSection)
          .maxSection(maxSection)
          .build();
      }
    }
    return null;
  }

  private void readEntityChunk(
    @NotNull final CompoundTag compound,
    final int version,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) {
    final var position = compound.getIntArray("Position").orElseThrow();
    final var chunkX = position[0];
    final var chunkZ = position[1];
    final var dataVersion = RealmFormat.dataVersionToWorldVersion(
      compound.getInteger("DataVersion").orElse(-1)
    );
    if (dataVersion != version) {
      System.err.printf(
        "Cannot load entity chunk at %s,%s: data version %s does not match world version %s%n",
        chunkX,
        chunkZ,
        dataVersion,
        version
      );
      return;
    }
    final var chunk = chunks.get(new RealmFormatChunkPosition(chunkX, chunkZ));
    if (chunk == null) {
      System.out.printf("Lost entity chunk data at: %s %s%n", chunkX, chunkZ);
    } else {
      for (final var tag : compound.getListTag("Entities").orElseThrow()) {
        chunk.entities().add(tag);
      }
    }
  }

  @NotNull
  private AnvilFormatLevelData readLevelData(@NotNull final Path path) throws IOException {
    final CompoundTag tag;
    try (final var reader = Tag.createGZIPReader(new FileInputStream(path.toFile()))) {
      tag = reader.readCompoundTag();
    }
    final var dataTag = tag
      .getCompoundTag("")
      .orElseThrow(() -> new IllegalStateException("This file is not a proper level.dat file!"))
      .getCompoundTag("Data")
      .orElseThrow(() -> new IllegalStateException("This file is not a proper level.dat file!"));
    final var worldVersionOptional = dataTag.getInteger("DataVersion");
    final var worldVersion = RealmFormat.dataVersionToWorldVersion(worldVersionOptional.orElse(-1));
    final var spawnX = dataTag.getInteger("SpawnX").orElse(0);
    final var spawnY = dataTag.getInteger("SpawnY").orElse(255);
    final var spawnZ = dataTag.getInteger("SpawnZ").orElse(0);
    final var gameRules = new HashMap<String, String>();
    final var rules = dataTag.getCompoundTag("GameRules").orElse(Tag.createCompound());
    rules.all().forEach((key, value) -> gameRules.put(key, value.asString().value()));
    return new AnvilFormatLevelData(worldVersion, gameRules, spawnX, spawnY, spawnZ);
  }

  private int@NotNull[] toIntArray(final byte@NotNull[] buf) {
    final var buffer = ByteBuffer.wrap(buf).order(ByteOrder.BIG_ENDIAN);
    final var ret = new int[buf.length / 4];
    buffer.asIntBuffer().get(ret);
    return ret;
  }
}
