package io.github.portlek.realmformat.format.property;

import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.Tag;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString(onlyExplicitlyIncluded = true)
public abstract class RealmFormatProperty<T> {

    @Nullable
    @ToString.Include
    private final T defaultValue;

    @NotNull
    @ToString.Include
    private final String nbtName;

    @Nullable
    private final Predicate<@NotNull T> validator;

    protected RealmFormatProperty(
        @NotNull final String nbtName,
        @Nullable final T defaultValue,
        @Nullable final Predicate<T> validator
    ) {
        this.nbtName = nbtName;
        if (validator != null && defaultValue != null && !validator.test(defaultValue)) {
            throw new IllegalArgumentException(
                "Invalid default value for property %s! %s".formatted(nbtName, defaultValue)
            );
        }
        this.defaultValue = defaultValue;
        this.validator = validator;
    }

    @Nullable
    protected abstract T readValue(@NotNull Tag tag);

    protected abstract void writeValue(@NotNull CompoundTag compound, @NotNull T value);
}
