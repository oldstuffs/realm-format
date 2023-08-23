package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatSerializerV1;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class RealmFormatSerializers {

    private final Map<Byte, RealmFormatSerializer> SERIALIZERS = Map.of(
        (byte) 1,
        RealmFormatSerializerV1.INSTANCE
    );

    @NotNull
    @SneakyThrows
    public RealmFormatWorld deserialize(
        final byte@NotNull[] serialized,
        @NotNull final RealmFormatPropertyMap properties
    ) {
        @Cleanup
        final DataInputStream input = new DataInputStream(new ByteArrayInputStream(serialized));
        final byte[] header = new byte[RealmFormat.HEADER.length];
        input.read(header);
        if (!Arrays.equals(header, RealmFormat.HEADER)) {
            throw new IllegalArgumentException(
                "Serialized data does NOT starts with the realm format's header!"
            );
        }
        final byte version = input.readByte();
        final RealmFormatSerializer serializer = Objects.requireNonNull(
            RealmFormatSerializers.SERIALIZERS.get(version),
            "This version '%s' is NOT supported!".formatted(version)
        );
        return serializer.deserialize(input, properties);
    }

    public void initiate() {
        // ignored
    }

    @SneakyThrows
    public byte@NotNull[] serialize(@NotNull final RealmFormatWorld world) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        @Cleanup
        final DataOutputStream output = new DataOutputStream(stream);
        final RealmFormatSerializer serializer = Objects.requireNonNull(
            RealmFormatSerializers.SERIALIZERS.get(world.version()),
            "This version '%s' is NOT supported!".formatted(world.version())
        );
        output.write(RealmFormat.HEADER);
        output.writeByte(RealmFormat.VERSION);
        serializer.serialize(output, world);
        return stream.toByteArray();
    }
}
