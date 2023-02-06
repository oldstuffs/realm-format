package io.github.portlek.realmformat.format.anvil;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.misc.NibbleArray;
import io.github.portlek.realmformat.format.property.RealmFormatProperties;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.BlockDataV1_14;
import io.github.portlek.realmformat.format.realm.BlockDataV1_18;
import io.github.portlek.realmformat.format.realm.BlockDataV1_8;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkSection;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkSectionV1;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatChunkV1;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatWorldV1;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
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
  public RealmFormatWorld deserialize(@NotNull final File worldDirectory) throws IOException {
    final var levelFile = new File(worldDirectory, "level.dat");
    final var regionDirectory = new File(worldDirectory, "region");
    final var entitiesDirectory = new File(worldDirectory, "entities");
    final var levelData = AnvilFormatSerializer.readLevelData(levelFile);
    final var worldVersion = levelData.version();
    Preconditions.checkArgument(
      regionDirectory.exists() && regionDirectory.isDirectory(),
      "'region' directory not found or it's not a directory!"
    );
    final var chunks = new HashMap<RealmFormatChunkPosition, RealmFormatChunk>();
    final var regionFiles = Preconditions.checkNotNull(
      regionDirectory.listFiles((dir, name) -> name.endsWith(".mca")),
      "Not a single file, that has 'mca' extension, found!"
    );
    for (final var regionFile : regionFiles) {
      chunks.putAll(AnvilFormatSerializer.loadChunks(regionFile, worldVersion));
    }
    final var entityFiles = entitiesDirectory.listFiles((dir, name) -> name.endsWith(".mca"));
    if (entityFiles != null) {
      for (final var entityFile : entityFiles) {
        AnvilFormatSerializer.loadEntities(entityFile, worldVersion, chunks);
      }
    }
    Preconditions.checkArgument(!chunks.isEmpty(), "Chunks not found!");
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

  private byte dataVersionToWorldVersion(final int dataVersion) {
    if (dataVersion <= 0) {
      return (byte) 1;
    } else if (dataVersion < 818) {
      return (byte) 2;
    } else if (dataVersion < 1501) {
      return (byte) 3;
    } else if (dataVersion < 1517) {
      return (byte) 4;
    } else if (dataVersion < 2566) {
      return (byte) 5;
    } else if (dataVersion <= 2586) {
      return (byte) 6;
    } else if (dataVersion <= 2730) {
      return (byte) 7;
    } else if (dataVersion <= 3218) {
      return (byte) 8;
    }
    throw new UnsupportedOperationException("Unsupported world version: " + dataVersion);
  }

  private boolean isEmpty(final long@NotNull[] array) {
    return Arrays.stream(array).noneMatch(b -> b != 0L);
  }

  @NotNull
  private Map<RealmFormatChunkPosition, RealmFormatChunk> loadChunks(
    @NotNull final File file,
    final int version
  ) throws IOException {
    final var regionByteArray = Files.readAllBytes(file.toPath());
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
      @Cleanup
      final var reader = Tag.createReader(decompressorStream);
      var global = reader.readCompoundTag().getCompoundTag("").orElseThrow();
      final var innerLevel = global.getCompoundTag("Level");
      if (innerLevel.isPresent()) {
        global = innerLevel.get();
      }
      final var chunk = AnvilFormatSerializer.readChunk(global, version);
      if (chunk != null) {
        chunks.put(new RealmFormatChunkPosition(chunk.x(), chunk.z()), chunk);
      }
    }
    return chunks;
  }

  private void loadEntities(
    @NotNull final File file,
    final int version,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
  ) throws IOException {
    final var regionByteArray = Files.readAllBytes(file.toPath());
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
      final var dataVersion = AnvilFormatSerializer.dataVersionToWorldVersion(
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
    final var optionalHeightMaps = compound.getCompoundTag("Heightmaps");
    final CompoundTag heightMapsCompound;
    if (worldVersion >= 4) {
      heightMapsCompound = optionalHeightMaps.orElse(Tag.createCompound());
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
      NibbleArray dataArray = null;
      ListTag paletteTag = null;
      long[] blockStatesArray = null;
      CompoundTag blockStatesTag = null;
      CompoundTag biomeTag = null;
      if (worldVersion < 4) {
        dataArray = new NibbleArray(compoundTag.getByteArray("Data").orElseThrow());
      } else if (worldVersion < 8) {
        paletteTag = compoundTag.getListTag("Palette").orElse(null);
        blockStatesArray = compoundTag.getLongArray("BlockStates").orElse(null);
        if (
          paletteTag == null ||
          blockStatesArray == null ||
          AnvilFormatSerializer.isEmpty(blockStatesArray)
        ) {
          continue;
        }
      } else {
        final var blockStatesOptional = compoundTag.getCompoundTag("block_states");
        final var biomesOptional = compoundTag.getCompoundTag("biomes");
        if (blockStatesOptional.isEmpty() && biomesOptional.isEmpty()) {
          continue;
        }
        blockStatesTag = blockStatesOptional.orElseThrow();
        biomeTag = biomesOptional.orElseThrow();
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
        RealmFormatChunkSectionV1
          .builder()
          .blockDataV1_8(new BlockDataV1_8(dataArray))
          .blockDataV1_14(new BlockDataV1_14(paletteTag, blockStatesArray))
          .blockDataV1_18(new BlockDataV1_18(biomeTag, blockStatesTag))
          .blockLight(blockLightArray)
          .skyLight(skyLightArray)
          .build();
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
    final var dataVersion = AnvilFormatSerializer.dataVersionToWorldVersion(
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
  private AnvilFormatLevelData readLevelData(@NotNull final File file) throws IOException {
    final var reader = Tag.createGZIPReader(new FileInputStream(file));
    final var tag = reader.readCompoundTag();
    final var dataTag = tag
      .getCompoundTag("")
      .orElseThrow(() -> new IllegalStateException("This file is not a proper level.dat file!"))
      .getCompoundTag("Data")
      .orElseThrow(() -> new IllegalStateException("This file is not a proper level.dat file!"));
    final var worldVersionOptional = dataTag.getInteger("DataVersion");
    final var worldVersion = AnvilFormatSerializer.dataVersionToWorldVersion(
      worldVersionOptional.orElse(-1)
    );
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
