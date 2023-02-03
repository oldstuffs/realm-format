package io.github.portlek.realmformat.format.anvil;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.property.RealmFormatProperties;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatWorldV1;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    final var version = levelData.version();
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
      chunks.putAll(
        AnvilFormatSerializer
          .loadChunks(regionFile, version)
          .stream()
          .collect(
            Collectors.toMap(
              chunk -> new RealmFormatChunkPosition(chunk.x(), chunk.z()),
              Function.identity()
            )
          )
      );
    }
    final var entityFiles = entitiesDirectory.listFiles((dir, name) -> name.endsWith(".mca"));
    if (entityFiles != null) {
      for (final var entityFile : entityFiles) {
        AnvilFormatSerializer.loadEntities(entityFile, version, chunks);
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
      .worldVersion(version)
      .chunks(chunks)
      .properties(properties)
      .extra(extra)
      .build();
  }

  @NotNull
  private Collection<RealmFormatChunk> loadChunks(@NotNull final File file, final int version)
    throws IOException {
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
    final var chunks = new HashSet<RealmFormatChunk>();
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
      final var global = reader.readCompoundTag();
      var levelDataTag = Tag.createCompound().set("Level", global);
      if (global.containsKey("Level")) {
        levelDataTag = global.getCompoundTag("Level").orElse(Tag.createCompound());
      }
      final var chunk = AnvilFormatSerializer.readChunk(levelDataTag, version);
      if (chunk != null) {
        chunks.add(chunk);
      }
    }
    return chunks;
  }

  private void loadEntities(
    @NotNull final File file,
    final int version,
    @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunkMap
  ) throws IOException {}

  @Nullable
  private RealmFormatChunk readChunk(@NotNull final CompoundTag compound, final int version) {
    return null;
  }

  @NotNull
  private AnvilFormatLevelData readLevelData(@NotNull final File file) throws IOException {
    final var reader = Tag.createReader(new FileInputStream(file));
    final var tag = reader.readCompoundTag();
    final var dataTag = tag
      .getCompoundTag("Data")
      .orElseThrow(() -> new IllegalStateException("This file is not a proper level.dat file!"));
    final var worldVersion = dataTag
      .getInteger("DataVersion")
      .map(dataVersion -> {
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
        } else if (dataVersion <= 3120) {
          return (byte) 8;
        }
        throw new UnsupportedOperationException("Unsupported world version: " + dataVersion);
      })
      .orElse((byte) 1);
    final var spawnX = dataTag.getInteger("SpawnX").orElse(0);
    final var spawnY = dataTag.getInteger("SpawnY").orElse(255);
    final var spawnZ = dataTag.getInteger("SpawnZ").orElse(0);
    final var gameRules = new HashMap<String, String>();
    final var rules = dataTag.getCompoundTag("GameRules").orElse(Tag.createCompound());
    rules.all().forEach((key, value) -> gameRules.put(key, value.asString().value()));
    return new AnvilFormatLevelData(worldVersion, gameRules, spawnX, spawnY, spawnZ);
  }
}
