package io.github.portlek.realmformat.paper.internal;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Misc {

    public boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (final ClassNotFoundException ex) {
            return false;
        }
    }

    @NotNull
    public String colorize(@NotNull final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
