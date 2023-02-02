package io.github.portlek.realmformat.paper.upgrader.v1_13;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class SetActionDeserializer implements JsonDeserializer<DowngradeData.TileSetAction> {

  @Override
  public DowngradeData.TileSetAction deserialize(
    final JsonElement el,
    final Type type,
    final JsonDeserializationContext context
  ) throws JsonParseException {
    final JsonObject obj = el.getAsJsonObject();
    final Map<String, DowngradeData.TileSetEntry> entries = new HashMap<>();
    for (final Map.Entry<String, JsonElement> entry : obj.entrySet()) {
      final String key = entry.getKey();
      final JsonElement entryEl = entry.getValue();
      entries.put(key, context.deserialize(entryEl, DowngradeData.TileSetEntry.class));
    }
    return new DowngradeData.TileSetAction(entries);
  }
}
