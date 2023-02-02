package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    @NotNull DataInputStream input,
    @NotNull RealmPropertyMap properties
  ) throws IOException;

  /**
   * Serializes the given world into output.
   *
   * @param output The output to write.
   * @param world The world to serialize.
   *
   * @throws IOException If something goes wrong when writing the world into the output.
   */
  void serialize(@NotNull DataOutputStream output, @NotNull RealmFormatWorld world)
    throws IOException;
}
