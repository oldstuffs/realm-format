package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatChunkPosition;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializer;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.v1.misc.InputStreamExtensionV1;
import io.github.portlek.realmformat.format.realm.v1.misc.OutputStreamExtensionV1;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
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
    final byte worldVersion = input.readByte();
    @Cleanup
    final InputStreamExtensionV1 versionedInput = new InputStreamExtensionV1(input, worldVersion);
    final Map<RealmFormatChunkPosition, RealmFormatChunk> chunks =
      versionedInput.readCompressedChunks();
    versionedInput.readEntities(chunks);
    versionedInput.readTileEntities(chunks);
    final CompoundTag extraCompound = versionedInput.readCompressedCompound();
    final RealmFormatPropertyMap newProperties = new RealmFormatPropertyMap();
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
    final OutputStreamExtensionV1 versionedOutput = new OutputStreamExtensionV1(
      output,
      world.worldVersion()
    );
    versionedOutput.writeByte(world.worldVersion());
    versionedOutput.writeCompressedChunks(world.chunks().values());
    versionedOutput.writeEntities(world.chunks().values());
    versionedOutput.writeTileEntities(world.chunks().values());
    final CompoundTag extra = world.extra();
    final RealmFormatPropertyMap properties = new RealmFormatPropertyMap(
      extra.getCompoundTag("properties").orElse(Tag.createCompound())
    );
    properties.merge(world.properties());
    extra.set("properties", properties.tag());
    versionedOutput.writeCompressedCompound(extra);
  }
}
