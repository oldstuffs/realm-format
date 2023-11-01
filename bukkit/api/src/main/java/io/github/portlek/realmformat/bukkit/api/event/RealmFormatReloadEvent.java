package io.github.portlek.realmformat.bukkit.api.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class RealmFormatReloadEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    public RealmFormatReloadEvent() {
        super(!Bukkit.isPrimaryThread());
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return RealmFormatReloadEvent.HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return RealmFormatReloadEvent.getHandlerList();
    }
}
