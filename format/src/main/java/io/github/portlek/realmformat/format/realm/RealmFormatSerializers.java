package io.github.portlek.realmformat.format.realm;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatSerializerV1;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class that contains serialization methods for {@link RealmFormatWorld}.
 * <p>
 * Run {@link #initiate()} method before using other methods.
 */
@UtilityClass
public class RealmFormatSerializers {

  /**
   * Realm format version -> serializer.
   */
  private final Byte2ObjectMap<RealmFormatSerializer> SERIALIZERS = new Byte2ObjectOpenHashMap<>();

  static {
    RealmFormatSerializers.SERIALIZERS.put((byte) 1, RealmFormatSerializerV1.INSTANCE);
  }

  /**
   * Deserializes the given bytes into {@link RealmFormatWorld}.
   *
   * @param serialized The serialized bytes to deserialize into {@link RealmFormatWorld}.
   * @param properties The properties to change deserialization behavior.
   *
   * @return Deserialized {@link RealmFormatWorld}.
   */
  @NotNull
  public RealmFormatWorld deserialize(
    final byte@NotNull[] serialized,
    @NotNull final RealmFormatPropertyMap properties
  ) {
    try (final var input = new DataInputStream(new ByteArrayInputStream(serialized))) {
      final var header = new byte[RealmFormat.HEADER.length];
      input.read(header);
      Preconditions.checkArgument(
        Arrays.equals(header, RealmFormat.HEADER),
        "Serialized data does NOT starts with the realm format's header!"
      );
      final var version = input.readByte();
      final var serializer = Preconditions.checkNotNull(
        RealmFormatSerializers.SERIALIZERS.get(version),
        "This version '%s' is NOT supported!",
        version
      );
      return serializer.deserialize(input, properties);
    } catch (final Exception e) {
      throw new RuntimeException("Something went wrong when deserializing a world!", e);
    }
  }

  /**
   * ignored.
   */
  public void initiate() {
    // ignored
  }

  /**
   * Serializes the given world into bytes.
   *
   * @param world The world to serialize.
   *
   * @return Serialized bytes.
   */
  public byte@NotNull[] serialize(@NotNull final RealmFormatWorld world) {
    final var stream = new ByteArrayOutputStream();
    try (final var output = new DataOutputStream(stream)) {
      final var serializer = Preconditions.checkNotNull(
        RealmFormatSerializers.SERIALIZERS.get(world.version()),
        "This version '%s' is NOT supported!",
        world.version()
      );
      output.write(RealmFormat.HEADER);
      output.writeByte(RealmFormat.VERSION);
      serializer.serialize(output, world);
    } catch (final Exception e) {
      throw new RuntimeException("Something went wrong when serializing a world!", e);
    }
    return stream.toByteArray();
  }
}
