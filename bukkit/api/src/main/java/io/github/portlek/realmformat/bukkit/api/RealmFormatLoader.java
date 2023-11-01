package io.github.portlek.realmformat.bukkit.api;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public interface RealmFormatLoader {
    void delete(@NotNull String worldName);

    boolean exists(@NotNull String worldName);

    @NotNull
    @Unmodifiable
    Collection<String> list();

    byte[] load(@NotNull String worldName, boolean readOnly);

    boolean locked(@NotNull String worldName);

    void save(@NotNull String worldName, byte@NotNull[] serialized, boolean lock);

    void unlock(@NotNull String worldName);
}
