package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.portlek.realmformat.format.exception.UnknownWorldException;
import io.github.portlek.realmformat.format.misc.Misc;
import io.github.portlek.realmformat.format.property.RealmProperties;
import io.github.portlek.realmformat.format.realm.RealmChunk;
import io.github.shiruka.nbt.Tag;
import io.github.shiruka.nbt.array.LongArrayTag;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.ticks.LevelChunkTicks;
import org.bukkit.Bukkit;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.generator.BiomeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RealmWorldServer extends ServerLevel {

  private static final ExecutorService WORLD_SAVER_SERVICE = Executors.newFixedThreadPool(
    4,
    new ThreadFactoryBuilder().setNameFormat("RF Pool Thread #%1$d").build()
  );

  @Getter
  private final RealmWorld1_18_R2 realmWorld;

  private final Object saveLock = new Object();

  @Getter
  @Setter
  private boolean ready = false;

  RealmWorldServer(
    @NotNull final RealmWorld1_18_R2 world,
    final ServerLevelData iworlddataserver,
    final ResourceKey<net.minecraft.world.level.Level> resourcekey,
    final ResourceKey<LevelStem> dimensionKey,
    final Holder<DimensionType> holder,
    final ChunkGenerator chunkgenerator,
    final org.bukkit.World.Environment env,
    final org.bukkit.generator.ChunkGenerator gen,
    final BiomeProvider biomeProvider
  ) throws IOException {
    super(
      MinecraftServer.getServer(),
      MinecraftServer.getServer().executor,
      RealmNmsBackend1_18_R2.CUSTOM_LEVEL_STORAGE.createAccess(
        world.name() + UUID.randomUUID(),
        dimensionKey
      ),
      iworlddataserver,
      resourcekey,
      holder,
      MinecraftServer.getServer().progressListenerFactory.create(11),
      chunkgenerator,
      false,
      0,
      Collections.emptyList(),
      true,
      env,
      gen,
      biomeProvider
    );
    this.realmWorld = world;
    final var propertyMap = world.propertyMap();
    this.serverLevelData.setDifficulty(
        Difficulty.valueOf(propertyMap.getValue(RealmProperties.DIFFICULTY).toUpperCase())
      );
    this.serverLevelData.setSpawn(
        new BlockPos(
          propertyMap.getValue(RealmProperties.SPAWN_X),
          propertyMap.getValue(RealmProperties.SPAWN_Y),
          propertyMap.getValue(RealmProperties.SPAWN_Z)
        ),
        0
      );
    super.setSpawnSettings(
      propertyMap.getValue(RealmProperties.ALLOW_MONSTERS),
      propertyMap.getValue(RealmProperties.ALLOW_ANIMALS)
    );
    this.pvpMode = propertyMap.getValue(RealmProperties.PVP);
    this.keepSpawnInMemory = false;
  }

  @Override
  public void save(
    @Nullable final ProgressListener progressListener,
    final boolean flush,
    final boolean savingDisabled
  ) {
    if (this.realmWorld.readOnly()) {
      return;
    }
    Bukkit.getPluginManager().callEvent(new WorldSaveEvent(this.getWorld()));
    this.getChunkSource().save(flush);
    this.serverLevelData.setWorldBorder(this.getWorldBorder().createSettings());
    this.serverLevelData.setCustomBossEvents(
        MinecraftServer.getServer().getCustomBossEvents().save()
      );
    final var compound = new net.minecraft.nbt.CompoundTag();
    final var nbtTagCompound =
      this.serverLevelData.createTag(MinecraftServer.getServer().registryAccess(), compound);
    this.realmWorld.extraData().set("LevelData", Converter.convertTag(nbtTagCompound));
    if (MinecraftServer.getServer().isStopped()) {
      this.save();
      try {
        this.realmWorld.loader().unlockWorld(this.realmWorld.name());
      } catch (final IOException ex) {
        ex.printStackTrace();
      } catch (final UnknownWorldException ignored) {}
    } else {
      RealmWorldServer.WORLD_SAVER_SERVICE.execute(this::save);
    }
  }

  @Override
  public void unload(final LevelChunk chunk) {
    for (final var tileentity : chunk.getBlockEntities().values()) {
      if (tileentity instanceof net.minecraft.world.Container) {
        for (final var h : Lists.newArrayList(
          ((net.minecraft.world.Container) tileentity).getViewers()
        )) {
          ((org.bukkit.craftbukkit.v1_18_R2.entity.CraftHumanEntity) h).getHandle()
            .closeUnloadedInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.UNLOADED);
        }
      }
    }
    chunk.unregisterTickContainerFromLevel(this);
  }

  @NotNull
  CompletableFuture<ChunkEntities<Entity>> handleEntityLoad(@NotNull final ChunkPos pos) {
    var entities = this.realmWorld.entities().get(Misc.asLong(pos.x, pos.z));
    if (entities == null) {
      entities = Tag.createList();
    }
    return CompletableFuture.completedFuture(
      new ChunkEntities<>(
        pos,
        new ArrayList<>(
          EntityType
            .loadEntitiesRecursive(
              entities
                .stream()
                .map(tag -> (net.minecraft.nbt.CompoundTag) Converter.convertTag(tag))
                .collect(Collectors.toList()),
              this
            )
            .toList()
        )
      )
    );
  }

  void handleEntityUnLoad(@NotNull final ChunkEntities<Entity> entities) {
    final var pos = entities.getPos();
    final var entitiesSerialized = Tag.createList();
    entities
      .getEntities()
      .forEach(entity -> {
        final var tag = new net.minecraft.nbt.CompoundTag();
        if (entity.save(tag)) {
          entitiesSerialized.add(Converter.convertTag(tag));
        }
      });
    this.realmWorld.entities().put(Misc.asLong(pos.x, pos.z), entitiesSerialized);
  }

  @NotNull
  ImposterProtoChunk imposterChunk(final int x, final int z) {
    final var realmChunk = this.realmWorld.chunkAt(x, z);
    final LevelChunk chunk;
    if (realmChunk == null) {
      final var pos = new ChunkPos(x, z);
      final var blockLevelChunkTicks = new LevelChunkTicks<Block>();
      final var fluidLevelChunkTicks = new LevelChunkTicks<Fluid>();
      chunk =
        new LevelChunk(
          this,
          pos,
          UpgradeData.EMPTY,
          blockLevelChunkTicks,
          fluidLevelChunkTicks,
          0L,
          null,
          null,
          null
        );
      this.realmWorld.updateChunk(new RealmChunkNms(null, chunk));
    } else if (realmChunk instanceof RealmChunkNms) {
      chunk = ((RealmChunkNms) realmChunk).chunk();
    } else {
      final var jank = new AtomicReference<RealmChunkNms>();
      chunk = this.convertChunk(realmChunk, () -> jank.get().dirtyRealm());
      final var realmChunkNms = new RealmChunkNms(realmChunk, chunk);
      jank.set(realmChunkNms);
      this.realmWorld.updateChunk(realmChunkNms);
    }
    return new ImposterProtoChunk(chunk, false);
  }

  void saveChunk(@NotNull final LevelChunk chunk) {
    final var realmChunk = this.realmWorld.chunkAt(chunk.getPos().x, chunk.getPos().z);
    if (realmChunk instanceof RealmChunkNms) {
      ((RealmChunkNms) realmChunk).chunk(chunk);
    } else {
      this.realmWorld.updateChunk(new RealmChunkNms(realmChunk, chunk));
    }
  }

  @NotNull
  private LevelChunk convertChunk(@NotNull final RealmChunk chunk, final Runnable onUnload) {
    final var x = chunk.x();
    final var z = chunk.z();
    final var pos = new ChunkPos(x, z);
    final var sections = new LevelChunkSection[this.getSectionsCount()];
    Object[] blockNibbles = null;
    Object[] skyNibbles = null;
    if (RealmNmsBackend1_18_R2.isPaperMC) {
      blockNibbles =
        ca.spottedleaf.starlight.common.light.StarLightEngine.getFilledEmptyLight(this);
      skyNibbles = ca.spottedleaf.starlight.common.light.StarLightEngine.getFilledEmptyLight(this);
      this.getServer().scheduleOnMain(() -> this.getLightEngine().retainData(pos, true));
    }
    final var biomeRegistry = this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
    //noinspection deprecation
    final var codec = PalettedContainer.codec(
      biomeRegistry.asHolderIdMap(),
      biomeRegistry.holderByNameCodec(),
      PalettedContainer.Strategy.SECTION_BIOMES,
      biomeRegistry.getHolderOrThrow(Biomes.PLAINS)
    );
    for (var sectionId = 0; sectionId < chunk.sections().length; sectionId++) {
      final var realmSection = chunk.sections()[sectionId];
      if (realmSection != null) {
        BlockState[] presetBlockStates = null;
        if (RealmNmsBackend1_18_R2.isPaperMC) {
          final var blockLight = realmSection.blockLight();
          if (blockLight != null) {
            blockNibbles[sectionId] =
              new ca.spottedleaf.starlight.common.light.SWMRNibbleArray(blockLight.backing());
          }
          final var skyLight = realmSection.skyLight();
          if (skyLight != null) {
            skyNibbles[sectionId] =
              new ca.spottedleaf.starlight.common.light.SWMRNibbleArray(skyLight.backing());
          }
          presetBlockStates =
            this.chunkPacketBlockController.getPresetBlockStates(this, pos, sectionId << 4);
        }
        final PalettedContainer<BlockState> blockPalette;
        final var blockStatesTag = realmSection.blockStatesTag();
        if (blockStatesTag != null) {
          final var blockStateCodec = presetBlockStates == null
            ? ChunkSerializer.BLOCK_STATE_CODEC
            : PalettedContainer.codec(
              Block.BLOCK_STATE_REGISTRY,
              BlockState.CODEC,
              PalettedContainer.Strategy.SECTION_STATES,
              Blocks.AIR.defaultBlockState(),
              presetBlockStates
            );
          final var dataresult = blockStateCodec
            .parse(NbtOps.INSTANCE, Converter.convertTag(blockStatesTag))
            .promotePartial(s ->
              System.out.println("Recoverable error when parsing section " + x + "," + z + ": " + s)
            );
          blockPalette = dataresult.getOrThrow(false, System.err::println);
        } else {
          blockPalette =
            new PalettedContainer<>(
              Block.BLOCK_STATE_REGISTRY,
              Blocks.AIR.defaultBlockState(),
              PalettedContainer.Strategy.SECTION_STATES,
              presetBlockStates
            );
        }
        final PalettedContainer<Holder<Biome>> biomePalette;
        final var biomeTag = realmSection.biomeTag();
        if (biomeTag != null) {
          final var dataresult = codec
            .parse(NbtOps.INSTANCE, Converter.convertTag(biomeTag))
            .promotePartial(s ->
              System.out.println("Recoverable error when parsing section " + x + "," + z + ": " + s)
            );
          biomePalette = dataresult.getOrThrow(false, System.err::println);
        } else {
          //noinspection deprecation
          biomePalette =
            new PalettedContainer<>(
              biomeRegistry.asHolderIdMap(),
              biomeRegistry.getHolderOrThrow(Biomes.PLAINS),
              PalettedContainer.Strategy.SECTION_BIOMES
            );
        }
        final var section = new LevelChunkSection(sectionId << 4, blockPalette, biomePalette);
        sections[sectionId] = section;
      }
    }
    final LevelChunk.PostLoadProcessor loadEntities = nmsChunk -> {
      final var tileEntities = chunk.tileEntities();
      for (final var tag : tileEntities) {
        if (!tag.isCompound()) {
          continue;
        }
        final var compoundTag = tag.asCompound();
        final var type = compoundTag.getString("id");
        if (type.isPresent()) {
          final var blockPosition = new BlockPos(
            compoundTag.getInteger("x").get(),
            compoundTag.getInteger("y").get(),
            compoundTag.getInteger("z").get()
          );
          final var blockData = nmsChunk.getBlockState(blockPosition);
          final var entity = BlockEntity.loadStatic(
            blockPosition,
            blockData,
            (net.minecraft.nbt.CompoundTag) Converter.convertTag(tag)
          );
          if (entity != null) {
            nmsChunk.setBlockEntity(entity);
          }
        }
      }
    };
    final var blockLevelChunkTicks = new LevelChunkTicks<Block>();
    final var fluidLevelChunkTicks = new LevelChunkTicks<Fluid>();
    final var nmsChunk = new LevelChunk(
      this,
      pos,
      UpgradeData.EMPTY,
      blockLevelChunkTicks,
      fluidLevelChunkTicks,
      0L,
      sections,
      loadEntities,
      null
    ) {
      @Override
      public void unloadCallback() {
        super.unloadCallback();
        onUnload.run();
      }
    };
    final var heightMapTypes = nmsChunk.getStatus().heightmapsAfter();
    final var heightMaps = chunk.heightMaps();
    final var unsetHeightMaps = EnumSet.noneOf(Heightmap.Types.class);
    if (RealmNmsBackend1_18_R2.isPaperMC) {
      nmsChunk.setBlockNibbles((SWMRNibbleArray[]) blockNibbles);
      nmsChunk.setSkyNibbles((SWMRNibbleArray[]) skyNibbles);
    }
    for (final var type : heightMapTypes) {
      final var name = type.getSerializedName();
      final var heightMap = heightMaps
        .get(name)
        .filter(Tag::isLongArray)
        .map(Tag::asLongArray)
        .map(LongArrayTag::primitiveValue);
      if (heightMap.isPresent()) {
        nmsChunk.setHeightmap(type, heightMap.get());
      } else {
        unsetHeightMaps.add(type);
      }
    }
    if (!unsetHeightMaps.isEmpty()) {
      Heightmap.primeHeightmaps(nmsChunk, unsetHeightMaps);
    }
    return nmsChunk;
  }

  private void save() {
    synchronized (this.saveLock) {
      try {
        Bukkit.getLogger().log(Level.INFO, "Saving world " + this.realmWorld.name() + "...");
        final var start = System.currentTimeMillis();
        final var serializedWorld = this.realmWorld.serialize();
        final var saveStart = System.currentTimeMillis();
        this.realmWorld.loader().saveWorld(this.realmWorld.name(), serializedWorld, false);
        Bukkit
          .getLogger()
          .log(
            Level.INFO,
            "World " +
            this.realmWorld.name() +
            " serialized in " +
            (saveStart - start) +
            "ms and saved in " +
            (System.currentTimeMillis() - saveStart) +
            "ms."
          );
      } catch (final IOException | IllegalStateException ex) {
        ex.printStackTrace();
      }
    }
  }
}
