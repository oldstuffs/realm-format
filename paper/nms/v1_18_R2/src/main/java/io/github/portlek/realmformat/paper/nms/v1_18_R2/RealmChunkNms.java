package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import io.github.portlek.realmformat.format.realm.RealmChunk;
import io.github.portlek.realmformat.format.realm.RealmChunkSection;
import io.github.portlek.realmformat.format.realm.impl.RealmChunkSectionImpl;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RealmChunkNms implements RealmChunk {

  @Setter
  @Getter
  private LevelChunk chunk;

  @Nullable
  private RealmChunk realmChunk;

  RealmChunkNms(@Nullable final RealmChunk realmChunk, @NotNull final LevelChunk chunk) {
    this.chunk = chunk;
    this.realmChunk = realmChunk;
  }

  @Override
  public int@NotNull[] biomes() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public ListTag entities() {
    final var entities = io.github.shiruka.nbt.Tag.createList();
    final var entityManager = this.chunk.level.entityManager;
    for (final var entity : entityManager.getEntityGetter().getAll()) {
      final var chunkPos = this.chunk.getPos();
      final var entityPos = entity.chunkPosition();
      if (chunkPos.x == entityPos.x && chunkPos.z == entityPos.z) {
        final var entityNbt = new net.minecraft.nbt.CompoundTag();
        if (entity.save(entityNbt)) {
          entities.add(Converter.convertTag(entityNbt));
        }
      }
    }
    return entities;
  }

  @NotNull
  @Override
  public CompoundTag heightMaps() {
    final var heightMaps = io.github.shiruka.nbt.Tag.createCompound();
    final var test = io.github.shiruka.nbt.Tag.createLongArray(1L, 2L, 3L);
    for (final var entry : this.chunk.heightmaps.entrySet()) {
      if (!entry.getKey().keepAfterWorldgen()) {
        continue;
      }
      final var type = entry.getKey();
      final var map = entry.getValue();
      heightMaps.set(type.name(), io.github.shiruka.nbt.Tag.createLongArray(map.getRawData()));
    }
    return heightMaps;
  }

  @Override
  public int maxSection() {
    return this.chunk.getMaxSection();
  }

  @Override
  public int minSection() {
    return this.chunk.getMinSection();
  }

  @Override
  public RealmChunkSection@NotNull[] sections() {
    final var sections = new RealmChunkSection[this.chunk.getMaxSection() -
    this.chunk.getMinSection() +
    1];
    final var lightEngine = this.chunk.getLevel().getChunkSource().getLightEngine();
    final var biomeRegistry =
      this.chunk.getLevel().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
    //noinspection deprecation
    final var codec = PalettedContainer.codec(
      biomeRegistry.asHolderIdMap(),
      biomeRegistry.holderByNameCodec(),
      PalettedContainer.Strategy.SECTION_BIOMES,
      biomeRegistry.getHolderOrThrow(Biomes.PLAINS)
    );
    for (var sectionId = 0; sectionId < this.chunk.getSections().length; sectionId++) {
      final var section = this.chunk.getSections()[sectionId];
      final var blockLightArray = Converter.convertArray(
        lightEngine
          .getLayerListener(LightLayer.BLOCK)
          .getDataLayerData(SectionPos.of(this.chunk.getPos(), sectionId))
      );
      final var skyLightArray = Converter.convertArray(
        lightEngine
          .getLayerListener(LightLayer.SKY)
          .getDataLayerData(SectionPos.of(this.chunk.getPos(), sectionId))
      );
      final var blockStateData = ChunkSerializer.BLOCK_STATE_CODEC
        .encodeStart(NbtOps.INSTANCE, section.getStates())
        .getOrThrow(false, System.err::println);
      final var biomeData = codec
        .encodeStart(NbtOps.INSTANCE, section.getBiomes())
        .getOrThrow(false, System.err::println);
      final var blockStateTag = Converter.convertTag(blockStateData).asCompound();
      final var biomeTag = Converter.convertTag(biomeData).asCompound();
      sections[sectionId] =
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
    return sections;
  }

  @Nullable
  @Override
  public ListTag tileEntities() {
    if (this.shouldDefaultBackToRealmChunk()) {
      return this.realmChunk.tileEntities();
    }
    final var tileEntities = io.github.shiruka.nbt.Tag.createList();
    for (final var entity : this.chunk.blockEntities.values()) {
      final var entityNbt = entity.saveWithFullMetadata();
      tileEntities.add(Converter.convertTag(entityNbt));
    }
    return tileEntities;
  }

  @Override
  public int x() {
    return this.chunk.getPos().x;
  }

  @Override
  public int z() {
    return this.chunk.getPos().z;
  }

  void dirtyRealm() {
    this.realmChunk = null;
  }

  private boolean shouldDefaultBackToRealmChunk() {
    return this.realmChunk != null && !this.chunk.loaded;
  }
}
