package io.github.portlek.realmformat.paper.loader;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.realm.RealmFormat;
import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.api.RealmFormatManager;
import io.github.portlek.realmformat.paper.file.RealmFormatConfig;
import io.github.portlek.realmformat.paper.internal.misc.Services;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;

@Log4j2
public final class RealmFormatLoaderFile implements RealmFormatLoader {

  private final RealmFormatConfig config = Services.load(RealmFormatConfig.class);

  @NotNull
  private final Path directory = new File(this.config.fileLoaderPath()).toPath();

  private final RealmFormatManager manager = Services.load(RealmFormatManager.class);

  private final Map<String, RandomAccessFile> worlds = new ConcurrentHashMap<>();

  @Override
  @SneakyThrows
  public void delete(@NotNull final String worldName) {
    Preconditions.checkState(this.exists(worldName), "World '%s' does not exists!", worldName);
    final var file = this.randomAccessFile(worldName);
    RealmFormatLoaderFile.log.info("Trying to unlock '{}' world...", worldName);
    this.unlock(worldName);
    RealmFormatLoaderFile.log.info("World '{}' is successfully unlocked!", worldName);
    RealmFormatLoaderFile.log.info("Trying to delete '{}' world...", worldName);
    file.seek(0);
    file.setLength(0);
    file.write(null);
    file.close();
    this.worlds.remove(worldName);
    FileUtils.forceDelete(this.pathFor(worldName).toFile());
    RealmFormatLoaderFile.log.info("World '{}' is successfully deleted!", worldName);
  }

  @Override
  public boolean exists(@NotNull final String worldName) {
    return Files.exists(this.pathFor(worldName));
  }

  @NotNull
  @Override
  @SneakyThrows
  public List<String> list() {
    return Files
      .list(this.directory)
      .map(Path::getFileName)
      .map(Path::toString)
      .map(s -> s.replace(RealmFormat.EXTENSION, ""))
      .toList();
  }

  @Override
  @SneakyThrows
  public byte@NotNull[] load(@NotNull final String worldName, final boolean readOnly) {
    Preconditions.checkState(this.exists(worldName), "World '%s' does not exists!", worldName);
    final var file = this.randomAccessFile(worldName);
    if (!readOnly && file.getChannel().isOpen()) {
      RealmFormatLoaderFile.log.info("World '{}' is unlocked by loading it.", worldName);
    }
    final var fileLength = file.length();
    Preconditions.checkState(
      fileLength <= Integer.MAX_VALUE,
      "World '%s' file is too big!",
      worldName
    );
    final var bytes = new byte[(int) fileLength];
    file.seek(0);
    file.readFully(bytes);
    return bytes;
  }

  @Override
  @SneakyThrows
  public boolean locked(@NotNull final String worldName) {
    final var file = this.randomAccessFile(worldName);
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
    final var worldFile = this.randomAccessFile(worldName);
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
    Preconditions.checkState(this.exists(worldName), "World '%s' does not exists!", worldName);
    final var removed = this.worlds.remove(worldName);
    if (removed == null) {
      return;
    }
    final var channel = removed.getChannel();
    if (channel.isOpen()) {
      removed.close();
    }
  }

  @Override
  @SneakyThrows
  public void setup(@NotNull final TerminableConsumer consumer) {
    this.manager.registerLoader("file", this);
    Preconditions.checkState(
      Files.notExists(this.directory) || Files.isDirectory(this.directory),
      "This file '%s' is not a directory! Please delete or rename the file to start the RealmFormat plugin!",
      this.directory.toAbsolutePath()
    );
    if (Files.notExists(this.directory)) {
      Files.createDirectory(this.directory);
    }
  }

  @NotNull
  private Path pathFor(@NotNull final String worldName) {
    return this.directory.resolve(worldName + RealmFormat.EXTENSION);
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
