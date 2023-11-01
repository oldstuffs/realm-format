package io.github.portlek.realmformat.format.realm.upgrader.v1_9;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.github.portlek.realmformat.format.realm.RealmFormatChunk;
import io.github.portlek.realmformat.format.realm.RealmFormatWorld;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrade;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RealmFormatWorldUpgradeV1_9 implements RealmFormatWorldUpgrade {

    @NotNull
    private static String fixJson(@Nullable final String value) {
        if (value == null || value.equalsIgnoreCase("null") || value.isEmpty()) {
            return "{\"text\":\"\"}";
        }
        try {
            JsonParser.parseString(value);
        } catch (final JsonSyntaxException e) {
            final JsonObject json = new JsonObject();
            json.addProperty("text", value);
            return json.toString();
        }
        return value;
    }

    @NotNull
    @Override
    public RealmFormatWorld apply(@NotNull final RealmFormatWorld t) {
        for (final RealmFormatChunk chunk : t.chunks().values()) {
            for (final Tag entityTag : chunk.tileEntities()) {
                final CompoundTag compound = entityTag.asCompound();
                final String type = compound.getString("id").orElseThrow();
                if (type.equals("Sign")) {
                    for (int i = 1; i < 5; i++) {
                        final String id = "Text" + i;
                        compound.setString(
                            id,
                            RealmFormatWorldUpgradeV1_9.fixJson(compound.getString(id).orElse(null))
                        );
                    }
                }
            }
        }
        return t;
    }
}
