package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.format.realm.RealmFormatSerializers;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrades;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.bukkit.BukkitEventManager;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.terminable.TerminableConsumer;

public final class RealmFormatBoostrap
    extends JavaPlugin
    implements TerminableConsumer, Terminable {

    private final CompositeTerminable terminable = CompositeTerminable.simple();

    @Override
    public <T extends AutoCloseable> @NotNull T bind(@NotNull final T terminable) {
        return this.terminable.bind(terminable);
    }

    @Override
    public void close() throws Exception {
        this.terminable.close();
    }

    @Override
    public void onLoad() {
        Plugins.init(new BukkitEventManager(this));
        RealmFormatSerializers.initiate();
        RealmFormatWorldUpgrades.initiate();
    }

    @Override
    public void onDisable() {
        this.closeUnchecked();
    }

    @Override
    public void onEnable() {}
}
