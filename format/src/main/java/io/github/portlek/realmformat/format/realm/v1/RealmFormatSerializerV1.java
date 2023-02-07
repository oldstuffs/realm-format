package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializer;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.v1.misc.InputStreamExtensionV1;
import io.github.portlek.realmformat.format.realm.v1.misc.OutputStreamExtensionV1;
import io.github.shiruka.nbt.Tag;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.Cleanup;
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
    @Cleanup
    final var versionedInput = new InputStreamExtensionV1(input, properties, worldVersion);
    final var chunks = versionedInput.readChunksWithPosition();
    versionedInput.readEntitiesInto(chunks);
    versionedInput.readTileEntitiesInto(chunks);
    final var extraCompound = versionedInput.readCompressedCompound();
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
    final var versionedOutput = new OutputStreamExtensionV1(
      output,
      world.properties(),
      world.worldVersion()
    );
    versionedOutput.writeByte(world.worldVersion());
    versionedOutput.writeChunks(world.chunks().values());
    versionedOutput.writeEntities(world.chunks().values());
    versionedOutput.writeTileEntities(world.chunks().values());
    final var extra = world.extra();
    final var properties = new RealmFormatPropertyMap(
      extra.getCompoundTag("properties").orElse(Tag.createCompound())
    );
    properties.merge(world.properties());
    extra.set("properties", properties.tag());
    versionedOutput.writeCompressedCompound(extra);
  }
}
