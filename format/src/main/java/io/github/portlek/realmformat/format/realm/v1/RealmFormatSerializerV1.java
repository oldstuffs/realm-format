package io.github.portlek.realmformat.format.realm.v1;

import io.github.portlek.realmformat.format.property.RealmPropertyMap;
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
    @NotNull final RealmPropertyMap properties
  ) throws IOException {
    final var worldVersion = input.readByte();
    final var chunkBytes = RealmFormatSerializerHelperV1.readCompressed(input);
    final var chunks = RealmFormatSerializerHelperV1.readChunks(chunkBytes, properties);
    final var entities = RealmFormatSerializerHelperV1.readCompressed(input);
    final var tileEntities = RealmFormatSerializerHelperV1.readCompressed(input);
    final var extra = RealmFormatSerializerHelperV1.readCompressed(input);
    final var entitiesCompound = RealmFormatSerializerHelperV1.readCompound(entities);
    if (entitiesCompound != null) {
      RealmFormatSerializerHelperV1.readEntities(chunks, entitiesCompound);
    }
    final var tileEntitiesCompound = RealmFormatSerializerHelperV1.readCompound(tileEntities);
    if (tileEntitiesCompound != null) {
      RealmFormatSerializerHelperV1.readTileEntities(chunks, tileEntitiesCompound);
    }
    final var extraCompound = RealmFormatSerializerHelperV1.readCompound(extra);
    final var newProperties = new RealmPropertyMap();
    if (extraCompound != null) {
      newProperties.merge(extraCompound.getCompoundTag("properties").orElse(Tag.createCompound()));
    }
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
  }
}
