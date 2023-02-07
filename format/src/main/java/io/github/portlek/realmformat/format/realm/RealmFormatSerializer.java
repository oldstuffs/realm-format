package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.misc.InputStreamExtension;
import io.github.portlek.realmformat.format.misc.OutputStreamExtension;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * An interface that represents serializers for {@link RealmFormatWorld}.
 */
public interface RealmFormatSerializer {
  /**
   * Deserializes the given input into {@link RealmFormatWorld}.
   *
   * @param input The input to deserialize into an object.
   * @param properties The properties to change deserialization behavior.
   *
   * @return Deserialized {@link RealmFormatWorld}.
   *
   * @throws IOException If something goes wrong when reading from input.
   */
  @NotNull
  RealmFormatWorld deserialize(
    @NotNull InputStreamExtension input,
    @NotNull RealmFormatPropertyMap properties
  ) throws IOException;

  /**
   * Serializes the given world into output.
   *
   * @param output The output to write.
   * @param world The world to serialize.
   *
   * @throws IOException If something goes wrong when writing the world into the output.
   */
  void serialize(@NotNull OutputStreamExtension output, @NotNull RealmFormatWorld world)
    throws IOException;
}
