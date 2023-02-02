package io.github.portlek.realmformat.paper.upgrader.v1_9;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.portlek.realmformat.paper.upgrader.Upgrade;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.primitive.StringTag;

public class WorldUpgrade1_9 implements Upgrade {

  private static final JsonParser PARSER = new JsonParser();

  private static String fixJson(final String value) {
    if (value == null || value.equalsIgnoreCase("null") || value.isEmpty()) {
      return "{\"text\":\"\"}";
    }
    try {
      WorldUpgrade1_9.PARSER.parse(value);
    } catch (final JsonSyntaxException ex) {
      final JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("text", value);
      return jsonObject.toString();
    }
    return value;
  }

  @Override
  public void downgrade(final CraftSlimeWorld world) {
    // No need to downgrade as JSON signs are compatible with 1.8
  }

  @Override
  public void upgrade(final CraftSlimeWorld world) {
    // In 1.9, all signs must be formatted using JSON
    for (final SlimeChunk chunk : world.getChunks().values()) {
      for (final CompoundTag entityTag : chunk.getTileEntities()) {
        final String type = entityTag.getAsStringTag("id").get().getValue();
        if (type.equals("Sign")) {
          final CompoundMap map = entityTag.getValue();
          for (int i = 1; i < 5; i++) {
            final String id = "Text" + i;
            map.put(
              id,
              new StringTag(
                id,
                WorldUpgrade1_9.fixJson(entityTag.getAsStringTag(id).map(StringTag::getValue).orElse(null))
              )
            );
          }
        }
      }
    }
  }
}
