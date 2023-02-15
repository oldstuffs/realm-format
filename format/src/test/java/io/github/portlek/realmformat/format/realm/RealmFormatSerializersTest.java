package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.anvil.AnvilFormatSerializer;
import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class RealmFormatSerializersTest {

  @Test
  void test_1_10_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_10")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_11_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_11")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_12_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_12")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_13_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_13")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_14_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_14")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_15_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_15")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_16_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_16")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_17_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_17")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_18_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_18")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_19_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_19")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_8_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_8")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }

  @Test
  void test_1_9_serializations() throws Exception {
    final var imported = AnvilFormatSerializer.deserialize(
      new File("src/test/resources/" + "worldv1_9")
    );
    final var serialized = RealmFormatSerializers.serialize(imported);
    final var deserialized = RealmFormatSerializers.deserialize(
      serialized,
      new RealmFormatPropertyMap()
    );
    Assertions.assertEquals(imported, deserialized);
  }
}
