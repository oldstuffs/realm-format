package io.github.portlek.realmformat.paper.loader;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.realm.RealmFormat;
import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import tr.com.infumia.terminable.TerminableConsumer;

public final class RealmFormatLoaderFile implements RealmFormatLoader {

    @NotNull
    private final Path folder;

    @NotNull
    private final Logger logger;

    private final Map<String, RandomAccessFile> worlds = new ConcurrentHashMap<>();

    public RealmFormatLoaderFile(@NotNull final Logger logger, @NotNull final Path folder) {
        this.logger = logger;
        this.folder = folder;
    }

    @Override
    @SneakyThrows
    public void delete(@NotNull final String worldName) {
        if (!this.exists(worldName)) {
            throw new IllegalStateException(
                String.format("World '%s' does not exists!", worldName)
            );
        }
        final RandomAccessFile file = this.randomAccessFile(worldName);
        this.logger.info("Trying to unlock '{}' world...", worldName);
        this.unlock(worldName);
        this.logger.info("World '{}' is successfully unlocked!", worldName);
        this.logger.info("Trying to delete '{}' world...", worldName);
        file.seek(0);
        file.setLength(0);
        file.write(null);
        file.close();
        this.worlds.remove(worldName);
        FileUtils.forceDelete(this.pathFor(worldName).toFile());
        this.logger.info("World '{}' is successfully deleted!", worldName);
    }

    @Override
    public boolean exists(@NotNull final String worldName) {
        return Files.exists(this.pathFor(worldName));
    }

    @NotNull
    @Override
    @SneakyThrows
    public Collection<String> list() {
        try (final Stream<Path> paths = Files.list(this.folder)) {
            return paths
                .map(Path::getFileName)
                .map(Path::toString)
                .map(s -> s.replace(RealmFormat.EXTENSION, ""))
                .collect(Collectors.toSet());
        }
    }

    @Override
    @SneakyThrows
    public byte@NotNull[] load(@NotNull final String worldName, final boolean readOnly) {
        if (!this.exists(worldName)) {
            throw new IllegalStateException(
                String.format("World '%s' does not exists!", worldName)
            );
        }
        final RandomAccessFile file = this.randomAccessFile(worldName);
        if (!readOnly && file.getChannel().isOpen()) {
            this.logger.info("World '{}' is unlocked by loading it.", worldName);
        }
        final long fileLength = file.length();
        if (fileLength > Integer.MAX_VALUE) {
            throw new IllegalStateException(
                String.format("World '%s' file is too big!", worldName)
            );
        }
        final byte[] bytes = new byte[(int) fileLength];
        file.seek(0);
        file.readFully(bytes);
        return bytes;
    }

    @Override
    @SneakyThrows
    public boolean locked(@NotNull final String worldName) {
        final RandomAccessFile file = this.randomAccessFile(worldName);
        if (!file.getChannel().isOpen()) {
            return true;
        }
        file.close();
        return false;
    }

    @Override
    @SneakyThrows
    public void save(
        @NotNull final String worldName,
        final byte@NotNull[] serialized,
        final boolean lock
    ) {
        final RandomAccessFile worldFile = this.randomAccessFile(worldName);
        worldFile.seek(0);
        worldFile.setLength(0);
        worldFile.write(serialized);
        if (lock) {
            worldFile.getChannel().tryLock();
        }
        worldFile.close();
    }

    @Override
    @SneakyThrows
    public void unlock(@NotNull final String worldName) {
        if (!this.exists(worldName)) {
            throw new IllegalStateException(
                String.format("World '%s' does not exists!", worldName)
            );
        }
        final RandomAccessFile removed = this.worlds.remove(worldName);
        if (removed == null) {
            return;
        }
        final FileChannel channel = removed.getChannel();
        if (channel.isOpen()) {
            removed.close();
        }
    }

    @Override
    @SneakyThrows
    public void setup(@NotNull final TerminableConsumer consumer) {
        Preconditions.checkState(
            Files.notExists(this.folder) || Files.isDirectory(this.folder),
            "This file '%s' is not a directory! Please delete or rename the file to start the RealmFormat plugin!",
            this.folder.toAbsolutePath()
        );
        if (Files.notExists(this.folder)) {
            Files.createDirectory(this.folder);
        }
        consumer.bind(() -> {
            this.worlds.forEach((fileName, file) -> {
                    try {
                        file.close();
                    } catch (final Exception e) {
                        this.logger.error(
                                "Unexpected error occurred while closing the file " + fileName,
                                e
                            );
                    }
                });
            this.worlds.clear();
        });
    }

    @NotNull
    private Path pathFor(@NotNull final String worldName) {
        return this.folder.resolve(worldName + RealmFormat.EXTENSION);
    }

    @NotNull
    private RandomAccessFile randomAccessFile(@NotNull final String worldName) {
        return this.worlds.computeIfAbsent(
                worldName,
                world -> {
                    try {
                        return new RandomAccessFile(this.pathFor(worldName).toFile(), "rw");
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }
}
