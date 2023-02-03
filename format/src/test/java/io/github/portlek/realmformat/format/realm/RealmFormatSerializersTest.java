package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.property.RealmFormatPropertyMap;
import io.github.portlek.realmformat.format.realm.v1.RealmFormatWorldV1;
import io.github.shiruka.nbt.Tag;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class RealmFormatSerializersTest {

  @Test
  void test() throws IOException {
    final var original = RealmFormatWorldV1.builder()
      .extra(Tag.createCompound())
      .chunks(Collections.emptyMap())
      .properties(new RealmFormatPropertyMap(Tag.createCompound().setString("test", "test")))
      .worldVersion((byte) 1)
      .build();
    final var serialized = RealmFormatSerializers.serialize(original);
    final var deserialized = RealmFormatSerializers.deserialize(serialized, new RealmFormatPropertyMap(Tag.createCompound().setString("test", "test")));
    Assertions.assertEquals(original, deserialized);
  }
}
