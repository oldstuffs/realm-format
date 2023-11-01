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
import io.github.shiruka.nbt.stream.NBTInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        final Path levelPath = worldDirectory.resolve("level.dat");
        final Path regionPath = worldDirectory.resolve("region");
        final Path entitiesPath = worldDirectory.resolve("entities");
        final AnvilFormatLevelData levelData = AnvilFormatSerializer.readLevelData(levelPath);
        final byte worldVersion = levelData.version();
        if (!Files.exists(regionPath) || !Files.isDirectory(regionPath)) {
            throw new IllegalArgumentException(
                "'region' directory not found or it's not a directory!"
            );
        }
        final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks = new HashMap<>();
        try (final Stream<Path> regionPathsStream = Files.list(regionPath)) {
            final List<Path> regionPaths = regionPathsStream
                .filter(name -> name.toString().endsWith(".mca"))
                .collect(Collectors.toList());
            for (final Path path : regionPaths) {
                chunks.putAll(AnvilFormatSerializer.loadChunks(path, worldVersion));
            }
        }
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Chunks not found!");
        }
        if (Files.exists(entitiesPath)) {
            try (final Stream<Path> entityPathsStream = Files.list(entitiesPath)) {
                final List<Path> entityPaths = entityPathsStream
                    .filter(name -> name.toString().endsWith(".mca"))
                    .collect(Collectors.toList());
                for (final Path path : entityPaths) {
                    AnvilFormatSerializer.loadEntities(path, worldVersion, chunks);
                }
            }
        }
        final CompoundTag extra = Tag.createCompound();
        final RealmFormatPropertyMap properties = new RealmFormatPropertyMap();
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
        for (final byte b : array) {
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
        final byte[] regionByteArray = Files.readAllBytes(path);
        @Cleanup
        final DataInputStream inputStream = new DataInputStream(
            new ByteArrayInputStream(regionByteArray)
        );
        final ArrayList<AnvilFormatChunkEntry> chunkEntries = new ArrayList<AnvilFormatChunkEntry>(
            1024
        );
        for (int i = 0; i < 1024; i++) {
            final int entry = inputStream.readInt();
            final int chunkOffset = entry >>> 8;
            final int chunkSize = entry & 15;
            if (entry != 0) {
                final AnvilFormatChunkEntry chunkEntry = new AnvilFormatChunkEntry(
                    chunkOffset * AnvilFormatSerializer.SECTOR_SIZE,
                    chunkSize * AnvilFormatSerializer.SECTOR_SIZE
                );
                chunkEntries.add(chunkEntry);
            }
        }
        final HashMap<RealmFormatChunkPosition, RealmFormatChunk> chunks = new HashMap<
            RealmFormatChunkPosition,
            RealmFormatChunk
        >();
        for (final AnvilFormatChunkEntry entry : chunkEntries) {
            @Cleanup
            final DataInputStream headerStream = new DataInputStream(
                new ByteArrayInputStream(regionByteArray, entry.offset(), entry.paddedSize())
            );
            final int chunkSize = headerStream.readInt() - 1;
            final byte compressionScheme = headerStream.readByte();
            final DataInputStream chunkStream = new DataInputStream(
                new ByteArrayInputStream(regionByteArray, entry.offset() + 5, chunkSize)
            );
            final InflaterInputStream decompressorStream = compressionScheme == 1
                ? new GZIPInputStream(chunkStream)
                : new InflaterInputStream(chunkStream);
            final CompoundTag tag;
            try (final NBTInputStream reader = Tag.createReader(decompressorStream)) {
                tag = reader.readCompoundTag();
            }
            CompoundTag global = tag
                .getCompoundTag("")
                .orElseThrow(() -> new IllegalStateException("Global tab not found!"));
            final Optional<CompoundTag> innerLevel = global.getCompoundTag("Level");
            if (innerLevel.isPresent()) {
                global = innerLevel.get();
            }
            final RealmFormatChunk chunk = AnvilFormatSerializer.readChunk(global, worldVersion);
            if (chunk != null) {
                chunks.put(
                    RealmFormatChunkPosition.builder().x(chunk.x()).z(chunk.z()).build(),
                    chunk
                );
            }
        }
        return chunks;
    }

    private void loadEntities(
        @NotNull final Path path,
        final int version,
        @NotNull final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks
    ) throws IOException {
        final byte[] regionByteArray = Files.readAllBytes(path);
        @Cleanup
        final DataInputStream input = new DataInputStream(
            new ByteArrayInputStream(regionByteArray)
        );
        final ArrayList<AnvilFormatChunkEntry> chunkEntries = new ArrayList<AnvilFormatChunkEntry>(
            1024
        );
        for (int i = 0; i < 1024; i++) {
            final int entry = input.readInt();
            if (entry != 0) {
                final int chunkOffset = entry >>> 8;
                final int chunkSize = entry & 15;
                chunkEntries.add(
                    new AnvilFormatChunkEntry(
                        chunkOffset * AnvilFormatSerializer.SECTOR_SIZE,
                        chunkSize * AnvilFormatSerializer.SECTOR_SIZE
                    )
                );
            }
        }
        for (final AnvilFormatChunkEntry entry : chunkEntries) {
            @Cleanup
            final DataInputStream headerStream = new DataInputStream(
                new ByteArrayInputStream(regionByteArray, entry.offset(), entry.paddedSize())
            );
            final int chunkSize = headerStream.readInt() - 1;
            final int compressionScheme = headerStream.readByte();
            final DataInputStream chunkStream = new DataInputStream(
                new ByteArrayInputStream(regionByteArray, entry.offset() + 5, chunkSize)
            );
            final InflaterInputStream decompressorStream = compressionScheme == 1
                ? new GZIPInputStream(chunkStream)
                : new InflaterInputStream(chunkStream);
            @Cleanup
            final NBTInputStream nbtStream = Tag.createReader(decompressorStream);
            final CompoundTag globalCompound = nbtStream
                .readCompoundTag()
                .getCompoundTag("")
                .orElseThrow(() -> new IllegalStateException("Global tag not found!"));
            AnvilFormatSerializer.readEntityChunk(globalCompound, version, chunks);
        }
    }

    @Nullable
    private RealmFormatChunk readChunk(
        @NotNull final CompoundTag compound,
        final int worldVersion
    ) {
        final int chunkX = compound
            .getInteger("xPos")
            .orElseThrow(() -> new IllegalStateException("xPos integer tag not found!"));
        final int chunkZ = compound
            .getInteger("zPos")
            .orElseThrow(() -> new IllegalStateException("zPos integer tag not found!"));
        if (worldVersion >= 8) {
            final byte dataVersion = RealmFormat.dataVersionToWorldVersion(
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
        final Optional<String> status = compound.getString("Status");
        if (
            status.isPresent() &&
            !status.get().equals("postprocessed") &&
            !status.get().startsWith("full")
        ) {
            return null;
        }
        final int[] biomes;
        final Tag biomesTag = compound.get("Biomes").orElse(Tag.createEnd());
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
            final int[] heightMap = compound.getIntArray("HeightMap").orElseGet(() -> new int[256]);
            heightMapsCompound =
            Tag.createCompound().set("heightMap", Tag.createIntArray(heightMap));
        }
        final ListTag tileEntities;
        final ListTag entities;
        final ListTag sections;
        int minSection = 0;
        int maxSection = 16;
        if (worldVersion < 8) {
            tileEntities = compound.getListTag("TileEntities").orElse(Tag.createList());
            entities = compound.getListTag("Entities").orElse(Tag.createList());
            sections =
            compound
                .getListTag("Sections")
                .orElseThrow(() -> new IllegalStateException("Sections list tag not found!"));
        } else {
            tileEntities = compound.getListTag("block_entities").orElse(Tag.createList());
            entities = compound.getListTag("entities").orElse(Tag.createList());
            sections =
            compound
                .getListTag("sections")
                .orElseThrow(() -> new IllegalStateException("sections list tag not found!"));
            final Tag yPos = compound
                .get("yPos")
                .orElseThrow(() -> new IllegalStateException("yPos tag not found!"));
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
        final RealmFormatChunkSection[] sectionArray = new RealmFormatChunkSection[maxSection -
        minSection];
        for (final Tag section : sections) {
            final CompoundTag compoundTag = section.asCompound();
            final byte index = compoundTag.getByte("Y").orElseThrow();
            if (worldVersion < 7 && index < 0) {
                continue;
            }
            final RealmFormatChunkSectionV1.RealmFormatChunkSectionV1Builder builder =
                RealmFormatChunkSectionV1.builder();
            if (worldVersion < 4) {
                final byte[] data = compoundTag
                    .getByteArray("Data")
                    .orElseThrow(() -> new IllegalStateException("Data byte tag not found!"));
                if (AnvilFormatSerializer.isEmpty(data)) {
                    continue;
                }
                builder.blockDataV1_8(BlockDataV1_8.builder().data(new NibbleArray(data)).build());
            } else if (worldVersion < 8) {
                final ListTag palette = compoundTag.getListTag("Palette").orElse(null);
                final long[] blockStates = compoundTag.getLongArray("BlockStates").orElse(null);
                if (
                    palette == null ||
                    blockStates == null ||
                    AnvilFormatSerializer.isEmpty(blockStates)
                ) {
                    continue;
                }
                builder.blockDataV1_14(new BlockDataV1_14(palette, blockStates));
            } else {
                final Optional<CompoundTag> blockStatesOptional = compoundTag.getCompoundTag(
                    "block_states"
                );
                final Optional<CompoundTag> biomesOptional = compoundTag.getCompoundTag("biomes");
                if (!blockStatesOptional.isPresent() || !biomesOptional.isPresent()) {
                    continue;
                }
                builder.blockDataV1_18(
                    BlockDataV1_18
                        .builder()
                        .blockStates(
                            blockStatesOptional.orElseThrow(() ->
                                new IllegalStateException("block_states tag not found!")
                            )
                        )
                        .biomes(
                            biomesOptional.orElseThrow(() ->
                                new IllegalStateException("biomes tag not found!")
                            )
                        )
                        .build()
                );
            }
            final NibbleArray blockLightArray = compoundTag
                .getByteArray("BlockLight")
                .map(NibbleArray::new)
                .orElse(null);
            final NibbleArray skyLightArray = compoundTag
                .getByteArray("SkyLight")
                .map(NibbleArray::new)
                .orElse(null);
            sectionArray[index - minSection] =
            builder.blockLight(blockLightArray).skyLight(skyLightArray).build();
        }
        for (final RealmFormatChunkSection section : sectionArray) {
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
        final int[] position = compound
            .getIntArray("Position")
            .orElseThrow(() -> new IllegalStateException("Position int array tag not found!"));
        final int chunkX = position[0];
        final int chunkZ = position[1];
        final byte dataVersion = RealmFormat.dataVersionToWorldVersion(
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
        final RealmFormatChunk chunk = chunks.get(
            RealmFormatChunkPosition.builder().x(chunkX).z(chunkZ).build()
        );
        if (chunk == null) {
            System.out.printf("Lost entity chunk data at: %s %s%n", chunkX, chunkZ);
        } else {
            for (final Tag tag : compound
                .getListTag("Entities")
                .orElseThrow(() -> new IllegalStateException("Entities list tag not found!"))) {
                chunk.entities().add(tag);
            }
        }
    }

    @NotNull
    private AnvilFormatLevelData readLevelData(@NotNull final Path path) throws IOException {
        final CompoundTag tag;
        try (final NBTInputStream reader = Tag.createGZIPReader(Files.newInputStream(path))) {
            tag = reader.readCompoundTag();
        }
        final CompoundTag dataTag = tag
            .getCompoundTag("")
            .orElseThrow(() ->
                new IllegalStateException("This file is not a proper level.dat file!")
            )
            .getCompoundTag("Data")
            .orElseThrow(() ->
                new IllegalStateException("This file is not a proper level.dat file!")
            );
        final OptionalInt worldVersionOptional = dataTag.getInteger("DataVersion");
        final byte worldVersion = RealmFormat.dataVersionToWorldVersion(
            worldVersionOptional.orElse(-1)
        );
        final int spawnX = dataTag.getInteger("SpawnX").orElse(0);
        final int spawnY = dataTag.getInteger("SpawnY").orElse(255);
        final int spawnZ = dataTag.getInteger("SpawnZ").orElse(0);
        final HashMap<String, String> gameRules = new HashMap<String, String>();
        final CompoundTag rules = dataTag.getCompoundTag("GameRules").orElse(Tag.createCompound());
        rules.all().forEach((key, value) -> gameRules.put(key, value.asString().value()));
        return new AnvilFormatLevelData(worldVersion, gameRules, spawnX, spawnY, spawnZ);
    }

    private int@NotNull[] toIntArray(final byte@NotNull[] buf) {
        final ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.BIG_ENDIAN);
        final int[] ret = new int[buf.length / 4];
        buffer.asIntBuffer().get(ret);
        return ret;
    }
}
