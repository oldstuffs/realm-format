package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializer;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.shiruka.nbt.Tag;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RealmFormatSerializerV1 implements RealmFormatSerializer {

  public static final RealmFormatSerializerV1 INSTANCE = new RealmFormatSerializerV1();

  @NotNull
  @Override
  public RealmFormatWorld deserialize(
    @NotNull final DataInputStream input,
    @NotNull final RealmFormatPropertyMap properties
  ) throws IOException {
    final var worldVersion = input.readByte();
    final var chunks = RealmFormatSerializerHelperV1.readChunks(input, properties, worldVersion);
    final var extraCompound = RealmFormatSerializerHelperV1.readCompressedCompound(input);
    final var newProperties = new RealmFormatPropertyMap();
    newProperties.merge(extraCompound.getCompoundTag("properties").orElse(Tag.createCompound()));
    newProperties.merge(properties);
    return RealmFormatWorldV1
      .builder()
      .worldVersion(worldVersion)
      .chunks(chunks)
      .extra(extraCompound)
      .properties(newProperties)
      .build();
  }

  @Override
  public void serialize(
    @NotNull final DataOutputStream output,
    @NotNull final RealmFormatWorld world
  ) throws IOException {
    output.writeByte(world.worldVersion());
    RealmFormatSerializerHelperV1.writeChunks(output, world.properties(), world.chunks().values(), world.worldVersion());
    final var extra = world.extra();
    final var properties = new RealmFormatPropertyMap(
      extra.getCompoundTag("properties").orElse(Tag.createCompound())
    );
    properties.merge(world.properties());
    extra.set("properties", properties.tag());
    RealmFormatSerializerHelperV1.writeCompressedCompound(output, extra);
  }
}
