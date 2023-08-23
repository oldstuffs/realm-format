package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.api.RealmFormatManager;
import io.github.portlek.realmformat.paper.config.RealmFormatConfig;
import java.util.Collection;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

final class RealmFormatLoaderMongo implements RealmFormatLoader, TerminableModule {

    @NotNull
    private final RealmFormatConfig config;

    @NotNull
    private final Logger logger;

    @NotNull
    private final RealmFormatManager manager;

    RealmFormatLoaderMongo(
        @NotNull final RealmFormatManager manager,
        @NotNull final Logger logger,
        @NotNull final RealmFormatConfig config
    ) {
        this.manager = manager;
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void delete(@NotNull final String worldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(@NotNull final String worldName) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Collection<String> list() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] load(@NotNull final String worldName, final boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean locked(@NotNull final String worldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(
        @NotNull final String worldName,
        final byte@NotNull[] serialized,
        final boolean lock
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unlock(@NotNull final String worldName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setup(@NotNull final TerminableConsumer consumer) {
        this.manager.registerLoader("mongo", this);
    }
}
