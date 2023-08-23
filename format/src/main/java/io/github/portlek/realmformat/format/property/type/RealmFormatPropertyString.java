package io.github.portlek.realmformat.format.property.type;

import io.github.portlek.realmformat.format.property.RealmFormatProperty;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RealmFormatPropertyString extends RealmFormatProperty<String> {

    public RealmFormatPropertyString(
        @NotNull final String nbtName,
        @Nullable final String defaultValue,
        @Nullable final Predicate<String> validator
    ) {
        super(nbtName, defaultValue, validator);
    }

    public RealmFormatPropertyString(
        @NotNull final String nbtName,
        @Nullable final String defaultValue
    ) {
        this(nbtName, defaultValue, null);
    }

    public RealmFormatPropertyString(@NotNull final String nbtName) {
        this(nbtName, null, null);
    }

    @Nullable
    @Override
    protected String readValue(@NotNull final Tag tag) {
        if (tag.isString()) {
            return this.defaultValue();
        }
        return tag.asString().value();
    }

    @Override
    protected void writeValue(@NotNull final CompoundTag compound, @NotNull final String value) {
        compound.setString(this.nbtName(), value);
    }
}
