package io.github.portlek.realmformat.modules.mongo;

import io.github.portlek.realmformat.bukkit.api.event.RealmFormatLoaderLoadEvent;
import io.github.portlek.realmformat.bukkit.api.internal.config.Configs;
import io.github.portlek.realmformat.bukkit.api.internal.module.Module;
import io.github.portlek.realmformat.bukkit.api.internal.module.ModuleContext;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.event.bukkit.Events;

public final class MongoModule extends Module {

    public MongoModule(@NotNull final ModuleContext context) {
        super(context);
    }

    @Override
    protected void enable() {
        final RealmFormatMongoConfig config = new RealmFormatMongoConfig(
            Configs.yaml(this.context.dataFolder().resolve("mongo.yaml"))
        );
        config.reload();
        Events
            .subscribe(RealmFormatLoaderLoadEvent.class)
            .handler(event ->
                event
                    .manager()
                    .registerLoader(
                        "mongo",
                        new RealmFormatLoaderMongo(event.manager(), config.credential())
                    )
            )
            .bindWith(this);
    }
}
