package io.github.portlek.realmformat.paper.api.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RealmFormatLoaderLoadEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public RealmFormatLoaderLoadEvent() {
        super(!Bukkit.isPrimaryThread());
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
