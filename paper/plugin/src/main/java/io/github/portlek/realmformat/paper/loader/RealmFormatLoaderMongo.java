package io.github.portlek.realmformat.paper.loader;

import io.github.portlek.realmformat.paper.api.RealmFormatLoader;
import io.github.portlek.realmformat.paper.internal.config.MongoCredential;
import java.util.Collection;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.terminable.TerminableConsumer;
import tr.com.infumia.terminable.TerminableModule;

public final class RealmFormatLoaderMongo implements RealmFormatLoader, TerminableModule {

    @NotNull
    private final MongoCredential credential;

    @NotNull
    private final Logger logger;

    public RealmFormatLoaderMongo(
        @NotNull final MongoCredential credential,
        @NotNull final Logger logger
    ) {
        this.credential = credential;
        this.logger = logger;
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
    public void setup(@NotNull final TerminableConsumer consumer) {}
}
