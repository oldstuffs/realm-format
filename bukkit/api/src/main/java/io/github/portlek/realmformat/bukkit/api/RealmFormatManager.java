package io.github.portlek.realmformat.bukkit.api;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import java.io.File;
import java.util.Collection;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public interface RealmFormatManager {
    @NotNull
    Optional<RealmFormatWorld> createEmptyWorld(
        @NotNull RealmFormatLoader loader,
        @NotNull String worldName,
        boolean readOnly,
        @NotNull RealmFormatPropertyMap properties
    );

    void generateWorld(@NotNull RealmFormatWorld world);

    void importAnvilWorld(
        @NotNull File worldDirectory,
        @NotNull String worldName,
        @NotNull RealmFormatLoader loader
    );

    @NotNull
    Optional<RealmFormatWorld> loadWorld(
        @NotNull RealmFormatLoader loader,
        @NotNull String worldName,
        boolean readOnly,
        @NotNull RealmFormatPropertyMap properties
    );

    @NotNull
    @UnmodifiableView
    Collection<RealmFormatWorld> loadedWorlds();

    @NotNull
    Optional<RealmFormatLoader> loader(@NotNull String type);

    void migrateWorld(
        @NotNull String worldName,
        @NotNull RealmFormatLoader oldLoader,
        @NotNull RealmFormatLoader newLoader
    );

    void registerLoader(@NotNull String type, @NotNull RealmFormatLoader loader);

    @NotNull
    Optional<RealmFormatWorld> world(@NotNull String worldName);
}
