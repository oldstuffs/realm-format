package io.github.portlek.realmformat.bukkit.nms;

import io.github.portlek.realmformat.bukkit.api.RealmFormatLoader;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RealmFormatWorldNms extends RealmFormatWorld {
    @Nullable
    RealmFormatChunk chunkAt(int x, int z);

    @NotNull
    RealmFormatWorldNms clone(@NotNull String worldName) throws Exception;

    @NotNull
    RealmFormatWorldNms clone(@NotNull String worldName, @Nullable RealmFormatLoader loader)
        throws Exception;

    @NotNull
    RealmFormatWorldNms clone(
        @NotNull String worldName,
        @Nullable RealmFormatLoader loader,
        boolean lock
    ) throws Exception;

    @NotNull
    RealmFormatWorldNms loader(@NotNull RealmFormatLoader loader);

    @NotNull
    RealmFormatLoader loader();

    boolean locked();

    @NotNull
    String name();

    boolean readOnly();

    @NotNull
    CompletableFuture<byte[]> serialize() throws Exception;

    void updateChunk(@NotNull RealmFormatChunk chunk);

    @NotNull
    RealmFormatWorldNms worldVersion(byte worldVersion);
}
