package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.anvil.AnvilFormatSerializer;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import java.io.File;
import java.util.List;
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
      final var world = AnvilFormatSerializer.deserialize(new File("src/test/resources/" + worldFolder));
      final var serialized = RealmFormatSerializers.serialize(world);
      RealmFormatSerializers.deserialize(serialized, new RealmFormatPropertyMap());
    }
  }
}
