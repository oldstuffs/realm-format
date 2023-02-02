package io.github.portlek.realmformat.paper.upgrader.v1_13;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockEntryDeserializer implements JsonDeserializer<DowngradeData.BlockEntry> {

  private static final Pattern PATTERN = Pattern.compile("([!A-Za-z1-9]+)=([A-Za-z1-9]+)");

  @Override
  public DowngradeData.BlockEntry deserialize(
    final JsonElement el,
    final Type type,
    final JsonDeserializationContext context
  ) throws JsonParseException {
    final JsonObject obj = el.getAsJsonObject();
    final List<DowngradeData.BlockProperty> properties;
    if (obj.has("properties")) {
      final JsonObject propertiesObj = obj.getAsJsonObject("properties");
      properties = new ArrayList<>();
      for (final Map.Entry<String, JsonElement> entry : propertiesObj.entrySet()) {
        final String conditionsString = entry.getKey();
        final Map<String, String> conditions = new HashMap<>();
        final Matcher matcher = BlockEntryDeserializer.PATTERN.matcher(conditionsString);
        while (matcher.find()) {
          final String property = matcher.group(1);
          final String value = matcher.group(2);
          conditions.put(property, value);
        }
        final JsonObject propertyObj = entry.getValue().getAsJsonObject();
        int id = -1;
        if (propertyObj.has("id")) {
          id = propertyObj.getAsJsonPrimitive("id").getAsInt();
        }
        int data = -1;
        DowngradeData.Operation operation = DowngradeData.Operation.REPLACE;
        if (propertyObj.has("data")) {
          final JsonPrimitive jsonData = propertyObj.getAsJsonPrimitive("data");
          if (jsonData.isNumber()) {
            data = jsonData.getAsInt();
          } else {
            final String opData = jsonData.getAsString();
            final String opString = opData.substring(0, 1);
            if ("|".equals(opString)) {
              operation = DowngradeData.Operation.OR;
            }
            data = Integer.parseInt(opData.substring(1));
          }
        }
        properties.add(new DowngradeData.BlockProperty(conditions, id, data, operation));
      }
    } else {
      properties = null;
    }
    final int id = obj.has("id") ? obj.getAsJsonPrimitive("id").getAsInt() : 0;
    final int data = obj.has("data") ? obj.getAsJsonPrimitive("data").getAsInt() : 0;
    final DowngradeData.TileEntityData tileEntityData = context.deserialize(
      obj.getAsJsonObject("tile_entity"),
      DowngradeData.TileEntityData.class
    );
    return new DowngradeData.BlockEntry(id, data, properties, tileEntityData);
  }
}
