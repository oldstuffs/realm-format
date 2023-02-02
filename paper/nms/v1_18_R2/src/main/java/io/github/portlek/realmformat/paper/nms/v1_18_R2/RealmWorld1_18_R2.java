package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.misc.ChunkSerialization;
import io.github.portlek.realmformat.format.old.realm.RealmChunk;
import io.github.portlek.realmformat.format.old.realm.RealmWorld;
import io.github.portlek.realmformat.format.old.realm.impl.RealmWorldBase;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import io.github.shiruka.nbt.Tag;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Cleanup;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tr.com.infumia.task.Promise;
import tr.com.infumia.task.Schedulers;

@Log4j2
public final class RealmWorld1_18_R2 extends RealmWorldBase {

  private static final InternalPlugin INTERNAL_PLUGIN = new InternalPlugin();

  @Setter
  private RealmWorldServer handle;

  RealmWorld1_18_R2(
    final byte version,
    @NotNull final RealmLoader loader,
    @NotNull final String name,
    @NotNull final Map<Long, RealmChunk> chunks,
    @NotNull final CompoundTag extraData,
    @NotNull final RealmPropertyMap propertyMap,
    final boolean readOnly,
    final boolean locked,
    @NotNull final Map<Long, ListTag> entities
  ) {
    super(version, loader, name, chunks, extraData, propertyMap, readOnly, locked, entities);
  }

  @NotNull
  @Override
  protected RealmWorld deepClone(
    @NotNull final String worldName,
    @Nullable final RealmLoader loader,
    final boolean lock
  ) {
    final var extraData = Tag.createCompound();
    this.extraData().all().forEach(extraData::set);
    return new RealmWorld1_18_R2(
      this.version(),
      loader == null ? this.loader() : loader,
      worldName,
      new Long2ObjectOpenHashMap<>(this.chunks()),
      extraData,
      this.propertyMap(),
      loader == null,
      lock,
      this.entities()
    );
  }

  @NotNull
  @Override
  protected Promise<@NotNull ChunkSerialization> serializeChunks(
    @NotNull final List<RealmChunk> chunks,
    final byte worldVersion
  ) throws IOException {
    final var tasks = new ArrayList<Runnable>(chunks.size() + 1);
    @Cleanup
    final var outByteStream = new ByteArrayOutputStream(16384);
    @Cleanup
    final var outStream = new DataOutputStream(outByteStream);
    final var tileEntities = Tag.createList();
    final var entities = Tag.createList();
    tasks.add(() -> {
      if (this.handle != null) {
        RealmWorld1_18_R2.log.debug("Saving entities");
        this.handle.entityManager.saveAll();
        this.entities().forEach((key, value) -> value.forEach(entities::add));
      }
    });
    for (final var chunk : chunks) {
      tasks.add(() -> {
        try {
          chunk.tileEntities().forEach(tileEntities::add);
          final var heightMaps = RealmWorldBase.serializeCompoundTag(chunk.heightMaps());
          outStream.writeInt(heightMaps.length);
          outStream.write(heightMaps);
          final var sections = chunk.sections();
          outStream.writeInt(chunk.minSection());
          outStream.writeInt(chunk.maxSection());
          outStream.writeInt(
            Math.toIntExact(Arrays.stream(sections).filter(Objects::nonNull).count())
          );
          for (var i = 0; i < sections.length; i++) {
            final var section = sections[i];
            if (section == null) {
              continue;
            }
            outStream.writeInt(i);
            final var blockLight = section.blockLight();
            final var hasBlockLight = blockLight != null;
            outStream.writeBoolean(hasBlockLight);
            if (hasBlockLight) {
              outStream.write(blockLight.backing());
            }
            final var serializedBlockStates = RealmWorldBase.serializeCompoundTag(
              section.blockStatesTag()
            );
            outStream.writeInt(serializedBlockStates.length);
            outStream.write(serializedBlockStates);
            final var serializedBiomes = RealmWorldBase.serializeCompoundTag(section.biomeTag());
            outStream.writeInt(serializedBiomes.length);
            outStream.write(serializedBiomes);
            final var skyLight = section.skyLight();
            final var hasSkyLight = skyLight != null;
            outStream.writeBoolean(hasSkyLight);
            if (hasSkyLight) {
              outStream.write(skyLight.backing());
            }
          }
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      });
    }
    if (Bukkit.isStopping()) {
      if (!Bukkit.isPrimaryThread()) {
        throw new UnsupportedOperationException(
          "Cannot save the world while the server is stopping async!"
        );
      }
      for (final var task : tasks) {
        task.run();
      }
      return Promise.completed(
        new ChunkSerialization(outByteStream.toByteArray(), tileEntities, entities)
      );
    } else {
      final var promise = Promise.<ChunkSerialization>empty();
      final var taskIterator = tasks.iterator();
      if (Bukkit.isPrimaryThread()) {
        var timeSaved = 0L;
        final var capturedTime = System.currentTimeMillis();
        while (
          taskIterator.hasNext() &&
          (timeSaved < 200 || Bukkit.isStopping() || Bukkit.isPrimaryThread())
        ) {
          taskIterator.next().run();
          timeSaved += System.currentTimeMillis() - capturedTime;
        }
        if (!taskIterator.hasNext()) {
          promise.supply(
            new ChunkSerialization(outByteStream.toByteArray(), tileEntities, entities)
          );
        }
      }
      if (!promise.isDone()) {
        Schedulers
          .sync()
          .runRepeating(
            task -> {
              var timeSaved = 0L;
              final var capturedTime = System.currentTimeMillis();
              while (
                taskIterator.hasNext() &&
                (timeSaved < 200 || Bukkit.isStopping() || Bukkit.isPrimaryThread())
              ) {
                taskIterator.next().run();
                timeSaved += System.currentTimeMillis() - capturedTime;
              }
              if (!taskIterator.hasNext()) {
                promise.supply(
                  new ChunkSerialization(outByteStream.toByteArray(), tileEntities, entities)
                );
                try {
                  task.stop();
                } catch (final Exception ignored) {}
              }
            },
            0L,
            1L
          );
      }
      return promise;
    }
  }
}
