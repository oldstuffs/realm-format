package io.github.portlek.realmformat.bukkit;

import io.github.portlek.realmformat.bukkit.config.RealmFormatConfig;
import io.github.portlek.realmformat.bukkit.config.RealmFormatMessages;
import io.github.portlek.realmformat.bukkit.internal.Cloud;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializers;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrades;
import io.github.portlek.realmformat.modifier.Modifier;
import io.github.portlek.realmformat.modifier.ModifierBackend;
import io.github.portlek.realmformat.bukkit.api.event.RealmFormatLoaderLoadEvent;
import io.github.portlek.realmformat.bukkit.api.internal.config.Configs;
import io.github.portlek.realmformat.bukkit.nms.NmsBackend;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.experimental.Delegate;
import org.bukkit.plugin.java.JavaPlugin;
import tr.com.infumia.event.bukkit.BukkitEventManager;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.versionmatched.VersionMatched;

public final class RealmFormatPlugin extends JavaPlugin implements TerminableConsumer, Terminable {

    private final NmsBackend nmsBackend = new VersionMatched<>(
        NmsBackendV1_19_R2.class,
        NmsBackendV1_19_R3.class
    )
        .of()
        .create()
        .orElseThrow(() ->
            new IllegalStateException(this.getServer().getVersion() + " not supported!")
        );

    private final ModifierBackend modifierBackend = new VersionMatched<>(
        ModifierBackendV1_19_R2.class,
        ModifierBackendV1_19_R3.class
    )
        .of(NmsBackend.class)
        .create(this.nmsBackend)
        .orElseThrow(() ->
            new IllegalStateException(this.getServer().getVersion() + " not supported!")
        );

    private final Path dataFolder = this.getDataFolder().toPath();

    private final RealmFormatConfig config = new RealmFormatConfig(
        Configs.yaml(this.dataFolder.resolve("config.yaml"))
    );

    private final RealmFormatMessages messages = new RealmFormatMessages(
        Configs.yaml(this.dataFolder.resolve("messages.yaml"))
    );

    private final RealmFormatManagerImpl manager = new RealmFormatManagerImpl(this.nmsBackend);

    @Delegate(types = { TerminableConsumer.class, Terminable.class })
    private final CompositeTerminable terminable = CompositeTerminable.simple();

    @Override
    public void onLoad() {
        Plugins.init(new BukkitEventManager(this));
        RealmFormatSerializers.initiate();
        RealmFormatWorldUpgrades.initiate();
        Modifier.initiateBackend(this.modifierBackend);
    }

    @Override
    public void onDisable() {
        this.closeUnchecked();
    }

    @Override
    public void onEnable() {
        this.manager.bindWith(this);
        this.reload();
        new RealmFormatModuleModule(this.dataFolder).bindModuleWith(this);
        new RealmFormatCommandModule(this, Cloud.annotation(Cloud.create(this)), this.messages)
            .bindModuleWith(this);
    }

    void reload() {
        this.config.reload();
        this.messages.reload();
        new RealmFormatLoaderFile(
            this.manager,
            Paths.get(System.getProperty("user.dir")).resolve(this.config.local())
        )
            .bindModuleWith(this);
        this.getServer().getPluginManager().callEvent(new RealmFormatLoaderLoadEvent());
    }
}
