package io.github.portlek.realmformat.paper.api.internal.module;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Plugin;
import tr.com.infumia.terminable.CompositeTerminable;
import tr.com.infumia.terminable.Terminable;
import tr.com.infumia.terminable.TerminableConsumer;

public abstract class Module extends Plugin implements TerminableConsumer, Terminable {

    @Delegate(types = { TerminableConsumer.class, Terminable.class })
    private final CompositeTerminable terminable = CompositeTerminable.simple();

    @NotNull
    protected final ModuleContext context;

    protected Module(@NotNull final ModuleContext context) {
        this.context = context;
    }

    @Override
    public final void start() {
        this.enable();
    }

    @Override
    public final void stop() {
        this.closeUnchecked();
    }

    @Override
    public final void delete() {}

    protected abstract void enable();
}
