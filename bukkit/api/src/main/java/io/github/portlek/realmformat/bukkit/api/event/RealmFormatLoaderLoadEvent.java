package io.github.portlek.realmformat.bukkit.api.event;

import io.github.portlek.realmformat.bukkit.api.RealmFormatManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public final class RealmFormatLoaderLoadEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @NotNull
    private final RealmFormatManager manager;

    public RealmFormatLoaderLoadEvent(@NotNull final RealmFormatManager manager) {
        super(!Bukkit.isPrimaryThread());
        this.manager = manager;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return RealmFormatLoaderLoadEvent.HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return RealmFormatLoaderLoadEvent.getHandlerList();
    }
}
