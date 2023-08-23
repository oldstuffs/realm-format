package io.github.portlek.realmformat.paper;

import com.google.common.base.Preconditions;
import io.github.portlek.realmformat.modifier.Modifier;
import io.github.portlek.realmformat.paper.api.RealmFormatManager;
import io.github.portlek.realmformat.paper.internal.Cloud;
import io.github.portlek.realmformat.paper.module.RealmFormatCommandModule;
import io.github.portlek.realmformat.paper.nms.NmsBackend;
import java.io.File;
import java.nio.file.Path;
import jdk.vm.ci.services.Services;

public final class RealmFormatPlugin {

    static void initialize(@NotNull final RealmFormatBoostrap boostrap) {
        Services.provide(RealmFormatBoostrap.class, boostrap);
        Services.provide(
            Path.class,
            Services.provide(File.class, boostrap.getDataFolder()).toPath()
        );
        final var nmsBackend = Services.provide(
            NmsBackend.class,
            RealmFormatPlugin.NMS_BACKEND.of().create().orElseThrow()
        );
        Modifier.initiateBackend(
            RealmFormatPlugin.MODIFIER_BACKEND.of(NmsBackend.class).create(nmsBackend).orElseThrow()
        );
        Services.provide(RealmFormatManager.class, new RealmFormatManagerImpl());
        RealmFormatPlugin.INITIALIZED.set(true);
    }

    void onEnable() {
        Preconditions.checkState(
            RealmFormatPlugin.INITIALIZED.get(),
            "RealmFormat plugin cannot be initialized properly, please check the logs!"
        );
        Services.provide(Cloud.KEY, Cloud.create(Services.load(RealmFormatBoostrap.class)));
        this.reload();
        Services.load(RealmFormatCommandModule.class).bindModuleWith(this.terminable);
    }
}
