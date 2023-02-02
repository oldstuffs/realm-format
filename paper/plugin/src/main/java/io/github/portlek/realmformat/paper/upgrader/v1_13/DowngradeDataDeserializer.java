package io.github.portlek.realmformat.paper.upgrader.v1_13;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DowngradeDataDeserializer implements JsonDeserializer<DowngradeData> {

  @Override
  public DowngradeData deserialize(final JsonElement el, final Type type, final JsonDeserializationContext context)
    throws JsonParseException {
    final JsonObject obj = el.getAsJsonObject();
    final Map<String, DowngradeData.BlockEntry> blocks = new HashMap<>();
    for (final Map.Entry<String, JsonElement> entry : obj.entrySet()) {
      final String key = entry.getKey();
      final JsonElement blockEl = entry.getValue();
      blocks.put(key, context.deserialize(blockEl, DowngradeData.BlockEntry.class));
    }
    return new DowngradeData(blocks);
  }
}
