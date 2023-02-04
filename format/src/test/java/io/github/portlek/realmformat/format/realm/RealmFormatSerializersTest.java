package io.github.portlek.realmformat.format.realm;

import io.github.portlek.realmformat.format.anvil.AnvilFormatSerializer;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class RealmFormatSerializersTest {

  @Test
  void test() throws Exception {
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_8"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_9"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_10"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_11"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_12"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_13"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_14"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_15"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_16"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_17"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_18"));
    AnvilFormatSerializer.deserialize(new File("src/test/resources/worldv1_19"));
  }
}
