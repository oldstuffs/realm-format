package io.github.portlek.realmformat.paper;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializers;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrades;
import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.api.RealmFormatManager;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

final class RealmFormatManagerImpl implements RealmFormatManager {

    private final Map<String, RealmFormatWorld> loadedWorlds = new ConcurrentHashMap<>();

    private final Map<String, RealmFormatLoader> loaders = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public Optional<RealmFormatWorld> createEmptyWorld(
        @NotNull final RealmFormatLoader loader,
        @NotNull final String worldName,
        final boolean readOnly,
        @NotNull final RealmFormatPropertyMap properties
    ) {
        return Optional.empty();
    }

    @Override
    public void generateWorld(@NotNull final RealmFormatWorld world) {}

    @Override
    public void importAnvilWorld(
        @NotNull final File worldDirectory,
        @NotNull final String worldName,
        @NotNull final RealmFormatLoader loader
    ) {}

    @NotNull
    @Override
    public Optional<RealmFormatWorld> loadWorld(
        @NotNull final RealmFormatLoader loader,
        @NotNull final String worldName,
        final boolean readOnly,
        @NotNull final RealmFormatPropertyMap properties
    ) {
        final long start = System.currentTimeMillis();
        RealmFormatManagerImpl.log.info("Loading world {}.", worldName);
        final byte[] serializedWorld = loader.load(worldName, readOnly);
        final RealmFormatWorld world;
        try {
            world = RealmFormatSerializers.deserialize(serializedWorld, properties);
            Preconditions.checkState(
                world.worldVersion() <= this.backend.worldVersion(),
                "World's world version: %s, Server's world version: %s"
            );
            if (world.worldVersion() < this.backend.worldVersion()) {
                RealmFormatWorldUpgrades.apply(world, this.backend.worldVersion());
            }
        } catch (final Exception e) {
            if (!readOnly) {
                loader.unlock(worldName);
            }
            throw e;
        }
        RealmFormatManagerImpl.log.info(
            "World {} loaded in {}ms.",
            worldName,
            System.currentTimeMillis() - start
        );
        this.loadedWorlds.put(worldName, world);
        return Optional.of(world);
    }

    @NotNull
    @Override
    @UnmodifiableView
    public Collection<RealmFormatWorld> loadedWorlds() {
        return Collections.unmodifiableCollection(this.loadedWorlds.values());
    }

    @NotNull
    @Override
    public Optional<RealmFormatLoader> loader(@NotNull final String type) {
        return Optional.ofNullable(this.loaders.get(type));
    }

    @Override
    public void migrateWorld(
        @NotNull final String worldName,
        @NotNull final RealmFormatLoader oldLoader,
        @NotNull final RealmFormatLoader newLoader
    ) {}

    @Override
    public void registerLoader(
        @NotNull final String type,
        final @NotNull RealmFormatLoader loader
    ) {
        this.loaders.put(type, loader);
    }

    @NotNull
    @Override
    public Optional<RealmFormatWorld> world(@NotNull final String worldName) {
        return Optional.empty();
    }
}
