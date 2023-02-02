package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.exception.CorruptedWorldException;
import io.github.portlek.realmformat.format.exception.NewerFormatException;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import java.io.DataInputStream;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public interface RealmWorldReader {
  @NotNull
  RealmWorld read(
    byte version,
    @NotNull RealmLoader loader,
    @NotNull String worldName,
    @NotNull DataInputStream dataStream,
    @NotNull RealmPropertyMap propertyMap,
    boolean readOnly
  ) throws IOException, CorruptedWorldException, NewerFormatException;
}
