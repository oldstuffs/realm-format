package io.github.portlek.realmformat.paper;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.bukkit.BukkitCommandManager;
import io.github.portlek.realmformat.format.realm.RealmFormatSerializers;
import io.github.portlek.realmformat.format.realm.upgrader.RealmFormatWorldUpgrades;
import io.github.portlek.realmformat.modifier.Modifier;
import io.github.portlek.realmformat.modifier.ModifierBackend;
import io.github.portlek.realmformat.paper.config.RealmFormatConfig;
import io.github.portlek.realmformat.paper.config.RealmFormatMessages;
import io.github.portlek.realmformat.paper.internal.Cloud;
import io.github.portlek.realmformat.paper.internal.config.Configs;
import io.github.portlek.realmformat.paper.module.RealmFormatCommandModule;
import io.github.portlek.realmformat.paper.nms.NmsBackend;
import java.nio.file.Path;
import lombok.experimental.Delegate;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import tr.com.infumia.event.bukkit.BukkitEventManager;
import tr.com.infumia.event.common.Plugins;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.versionmatched.VersionMatched;

public final class RealmFormatPlugin extends JavaPlugin implements TerminableConsumer, Terminable {

    private final Path dataFolder = this.getDataFolder().toPath();

    private final RealmFormatConfig config = new RealmFormatConfig(
        Configs.yaml(this.dataFolder.resolve("config.yaml"))
    );

    private final RealmFormatMessages messages = new RealmFormatMessages(
        Configs.yaml(this.dataFolder.resolve("messages.yaml"))
    );

    @Delegate(types = { TerminableConsumer.class, Terminable.class })
    private final CompositeTerminable terminable = CompositeTerminable.simple();

    @Override
    public void onLoad() {
        Plugins.init(new BukkitEventManager(this));
        RealmFormatSerializers.initiate();
        RealmFormatWorldUpgrades.initiate();
        final VersionMatched<ModifierBackend> modifierBackend = new VersionMatched<>();
        final VersionMatched<NmsBackend> nmsBackend = new VersionMatched<>();
        Modifier.initiateBackend(
            modifierBackend
                .of(NmsBackend.class)
                .create(
                    nmsBackend
                        .of()
                        .create()
                        .orElseThrow(() ->
                            new IllegalStateException(nmsBackend.version() + " not supported!")
                        )
                )
                .orElseThrow(() ->
                    new IllegalStateException(modifierBackend.version() + " not supported!")
                )
        );
    }

    @Override
    public void onDisable() {
        this.closeUnchecked();
    }

    @Override
    public void onEnable() {
        final BukkitCommandManager<CommandSender> commandManager = Cloud.create(this);
        final AnnotationParser<CommandSender> annotationParser = Cloud.annotation(commandManager);
        new RealmFormatCommandModule(this, annotationParser, this.messages).bindModuleWith(this);
    }

    public void reload() {
        this.config.reload();
        this.messages.reload();
    }
}
