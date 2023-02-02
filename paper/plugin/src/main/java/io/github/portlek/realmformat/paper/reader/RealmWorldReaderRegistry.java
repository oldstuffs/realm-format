package io.github.portlek.realmformat.paper.reader;

import io.github.portlek.realmformat.format.exception.CorruptedWorldException;
import io.github.portlek.realmformat.format.exception.NewerFormatException;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.misc.RealmConstants;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.format.realm.RealmWorldReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class RealmWorldReaderRegistry {

  private final Map<Byte, RealmWorldReader> FORMATS = new HashMap<>();

  static {
    RealmWorldReaderRegistry.register(new RealmWorldFormatv1_9(), 1, 2, 3, 4, 5, 6, 7, 8, 9);
  }

  @NotNull
  public RealmWorld read(@NotNull final RealmLoader loader, @NotNull final String worldName, final byte @NotNull [] serializedWorld, @NotNull final RealmPropertyMap propertyMap, final boolean readOnly) throws IOException, CorruptedWorldException, NewerFormatException {
    @Cleanup final var dataStream = new DataInputStream(new ByteArrayInputStream(serializedWorld));
    final var fileHeader = new byte[RealmConstants.HEADER.length];
    dataStream.read(fileHeader);
    CorruptedWorldException.check(Arrays.equals(RealmConstants.HEADER, fileHeader), worldName);
    final var version = dataStream.readByte();
    NewerFormatException.check(RealmConstants.VERSION >= version, version);
    final var reader = RealmWorldReaderRegistry.FORMATS.get(version);
    return reader.read(version, loader, worldName, dataStream, propertyMap, readOnly);
  }

  private void register(@NotNull final RealmWorldReader format, final int... bytes) {
    for (final var value : bytes) {
      RealmWorldReaderRegistry.FORMATS.put((byte) value, format);
    }
  }
}
