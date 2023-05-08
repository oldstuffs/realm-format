package io.github.portlek.realmformat.paper;

import io.github.portlek.realmformat.paper.internal.misc.Services;
import org.bukkit.plugin.java.JavaPlugin;

public final class RealmFormatBoostrap extends JavaPlugin {

  @Override
  public void onLoad() {
    RealmFormatPlugin.initialize(this);
  }

  @Override
  public void onDisable() {
    Services.load(RealmFormatPlugin.class).onDisable();
  }

  @Override
  public void onEnable() {
    Services.load(RealmFormatPlugin.class).onEnable();
  }
}
