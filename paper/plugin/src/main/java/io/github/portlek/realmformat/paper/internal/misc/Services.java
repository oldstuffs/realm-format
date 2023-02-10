package io.github.portlek.realmformat.paper.internal.misc;

import com.google.common.reflect.TypeToken;
import io.github.portlek.realmformat.paper.RealmFormatBoostrap;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
@SuppressWarnings("unchecked")
public class Services {

  @Nullable
  private Plugin plugin;

  @NotNull
  public <T> Optional<T> get(@NotNull final TypeToken<T> type) {
    return Optional.ofNullable(Bukkit.getServicesManager().load(type.getRawType())).map(o -> (T) o);
  }

  @NotNull
  public <T> Optional<T> get(@NotNull final Class<T> type) {
    return Services.get(TypeToken.of(type));
  }

  @NotNull
  public <T> T load(@NotNull final TypeToken<T> type) {
    return Services.get(type).orElseThrow();
  }

  @NotNull
  public <T> T load(@NotNull final Class<T> type) {
    return Services.load(TypeToken.of(type));
  }

  @NotNull
  public <T> T provide(@NotNull final TypeToken<T> type, @NotNull final T instance) {
    return Services
      .get(type)
      .orElseGet(() -> {
        Bukkit
          .getServicesManager()
          .register(type.getRawType(), instance, Services.plugin(), ServicePriority.Normal);
        return instance;
      });
  }

  @NotNull
  public <T> T provide(@NotNull final Class<T> type, @NotNull final T instance) {
    return Services.provide(TypeToken.of(type), instance);
  }

  @NotNull
  private Plugin plugin() {
    if (Services.plugin == null) {
      Services.plugin = JavaPlugin.getPlugin(RealmFormatBoostrap.class);
    }
    return Services.plugin;
  }
}
