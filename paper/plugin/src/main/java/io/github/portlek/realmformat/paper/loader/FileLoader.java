package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.format.exception.UnknownWorldException;
import io.github.portlek.realmformat.format.exception.WorldInUseException;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

@Log4j2
public final class FileLoader implements RealmLoader {

  private static final FilenameFilter WORLD_FILE_FILTER = (dir, name) -> name.endsWith(".realm");

  private final File worldDir;

  private final Map<String, RandomAccessFile> worldFiles = Collections.synchronizedMap(
    new HashMap<>()
  );

  public FileLoader(@NotNull final File worldDirectory) {
    this.worldDir = worldDirectory;
    if (worldDirectory.exists() && !worldDirectory.isDirectory()) {
      FileLoader.log.error(
        "A file named '" +
          worldDirectory.getName() +
          "' has been deleted, as this is the name used for the worlds directory."
      );
      worldDirectory.delete();
    }
    worldDirectory.mkdirs();
  }

  @Override
  public void deleteWorld(@NotNull final String worldName) throws UnknownWorldException {
    if (!this.worldExists(worldName)) {
      throw new UnknownWorldException(worldName);
    }
    try (final RandomAccessFile randomAccessFile = this.worldFiles.get(worldName)) {
      System.out.println("Deleting world.. " + worldName + ".");
      this.unlockWorld(worldName);
      FileUtils.forceDelete(new File(this.worldDir, worldName + ".realm"));
      if (randomAccessFile != null) {
        System.out.print("Attempting to delete worldData " + worldName + ".");
        randomAccessFile.seek(0);
        randomAccessFile.setLength(0);
        randomAccessFile.write(null);
        randomAccessFile.close();
        this.worldFiles.remove(worldName);
      }
      System.out.println("World.. " + worldName + " deleted.");
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean isWorldLocked(@NotNull final String worldName) throws IOException {
    RandomAccessFile file = this.worldFiles.get(worldName);
    if (file == null) {
      file = new RandomAccessFile(new File(this.worldDir, worldName + ".realm"), "rw");
    }
    if (file.getChannel().isOpen()) {
      file.close();
    } else {
      return true;
    }
    return false;
  }

  @NotNull
  @Override
  public List<String> listWorlds() throws NotDirectoryException {
    final String[] worlds = this.worldDir.list(FileLoader.WORLD_FILE_FILTER);
    if (worlds == null) {
      throw new NotDirectoryException(this.worldDir.getPath());
    }
    return Arrays
      .stream(worlds)
      .map(c -> c.substring(0, c.length() - 6))
      .collect(Collectors.toList());
  }

  @Override
  public byte @NotNull [] loadWorld(@NotNull final String worldName, final boolean readOnly)
    throws UnknownWorldException, IOException, WorldInUseException {
    if (!this.worldExists(worldName)) {
      throw new UnknownWorldException(worldName);
    }
    @Cleanup final RandomAccessFile file = new RandomAccessFile(
      new File(this.worldDir, worldName + ".realm"),
      "rw"
    );
    if (!readOnly) {
      if (file.getChannel().isOpen()) {
        System.out.print("World is unlocked");
      }
    }
    if (file.length() > Integer.MAX_VALUE) {
      throw new IndexOutOfBoundsException("World is too big!");
    }
    final byte[] serializedWorld = new byte[(int) file.length()];
    file.seek(0);
    file.readFully(serializedWorld);
    return serializedWorld;
  }

  @Override
  public void saveWorld(
    @NotNull final String worldName,
    final byte @NotNull [] serializedWorld,
    final boolean lock
  ) throws IOException {
    RandomAccessFile worldFile = this.worldFiles.get(worldName);
    final boolean tempFile = worldFile == null;
    if (tempFile) {
      worldFile = new RandomAccessFile(new File(this.worldDir, worldName + ".realm"), "rw");
    }
    worldFile.seek(0);
    worldFile.setLength(0);
    worldFile.write(serializedWorld);
    if (lock) {
      final FileChannel channel = worldFile.getChannel();
      try {
        channel.tryLock();
      } catch (final OverlappingFileLockException ignored) {
      }
    }
    worldFile.close();
  }

  @Override
  public void unlockWorld(@NotNull final String worldName)
    throws UnknownWorldException, IOException {
    if (!this.worldExists(worldName)) {
      throw new UnknownWorldException(worldName);
    }
    final RandomAccessFile file = this.worldFiles.remove(worldName);
    if (file != null) {
      final FileChannel channel = file.getChannel();
      if (channel.isOpen()) {
        file.close();
      }
    }
  }

  @Override
  public boolean worldExists(@NotNull final String worldName) {
    return new File(this.worldDir, worldName + ".realm").exists();
  }
}
