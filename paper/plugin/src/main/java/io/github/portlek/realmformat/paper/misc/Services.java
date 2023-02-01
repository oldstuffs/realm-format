package io.github.portlek.realmformat.paper.misc;

import com.google.common.reflect.TypeToken;
import io.github.portlek.realmformat.paper.RealmBoostrap;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@UtilityClass
@SuppressWarnings("unchecked")
public class Services {

  @NotNull
  public <T> Optional<T> get(@NotNull final Class<T> serviceType) {
    return Services.get(TypeToken.of(serviceType));
  }

  @NotNull
  public <T> Optional<T> get(@NotNull final TypeToken<T> serviceType) {
    return Optional.ofNullable(
      Bukkit.getServicesManager().load((Class<T>) serviceType.getRawType())
    );
  }

  @NotNull
  public <T> T load(@NotNull final Class<T> serviceType) {
    return Services.load(TypeToken.of(serviceType));
  }

  @NotNull
  public <T> T load(@NotNull final TypeToken<T> serviceType) {
    return Services
      .get(serviceType)
      .orElseThrow(() -> {
        throw new IllegalStateException(
          "No registration present for service '%s'".formatted(serviceType.getRawType().getName())
        );
      });
  }

  @NotNull
  public <T> T provide(@NotNull final Class<T> serviceType, @NotNull final T instance) {
    return Services.provide(TypeToken.of(serviceType), instance);
  }

  @NotNull
  public <T> T provide(@NotNull final TypeToken<T> serviceType, @NotNull final T instance) {
    Bukkit
      .getServicesManager()
      .register(
        serviceType.getRawType(),
        instance,
        JavaPlugin.getPlugin(RealmBoostrap.class),
        ServicePriority.Normal
      );
    return instance;
  }
}
