package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.anvil.AnvilFormatSerializer;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class RealmFormatSerializersTest {

  private static final List<String> WORLD_FOLDERS = List.of(
    "worldv1_8",
    "worldv1_9",
    "worldv1_10",
    "worldv1_11",
    "worldv1_12",
    "worldv1_13",
    "worldv1_14",
    "worldv1_15",
    "worldv1_16",
    "worldv1_17",
    "worldv1_18",
    "worldv1_19"
  );

  @Test
  void test() throws Exception {
    for (final var worldFolder : RealmFormatSerializersTest.WORLD_FOLDERS) {
      final var imported = AnvilFormatSerializer.deserialize(
        new File("src/test/resources/" + worldFolder)
      );
      final var serialized = RealmFormatSerializers.serialize(imported);
      final var deserialized = RealmFormatSerializers.deserialize(
        serialized,
        new RealmFormatPropertyMap()
      );
      Assertions.assertEquals(imported.worldVersion(), deserialized.worldVersion());
      Assertions.assertEquals(imported.version(), deserialized.version());
      Assertions.assertEquals(imported.extra(), deserialized.extra());
      Assertions.assertEquals(imported.properties(), deserialized.properties());
      imported
        .chunks()
        .forEach((position, chunk) -> {
          final var actual = deserialized.chunks().get(position);
          Assertions.assertEquals(chunk.x(), actual.x());
          Assertions.assertEquals(chunk.z(), actual.z());
          Assertions.assertEquals(chunk.maxSection(), actual.maxSection());
          Assertions.assertEquals(chunk.minSection(), actual.minSection());
          Assertions.assertArrayEquals(chunk.biomes(), actual.biomes());
          Assertions.assertEquals(chunk.heightMaps(), actual.heightMaps());
          Assertions.assertEquals(chunk.tileEntities(), actual.tileEntities());
          Assertions.assertEquals(chunk.entities(), actual.entities());
          Assertions.assertArrayEquals(chunk.sections(), actual.sections());
        });
    }
  }
}
