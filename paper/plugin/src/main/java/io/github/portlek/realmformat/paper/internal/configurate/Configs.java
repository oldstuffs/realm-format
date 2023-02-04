package io.github.portlek.realmformat.paper.internal.configurate;

import io.github.portlek.realmformat.paper.internal.configurate.serializer.NonItalicFallbackComponentSerializer;
import java.nio.file.Path;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@SuppressWarnings("unchecked")
@UtilityClass
public class Configs {

  private final TypeSerializerCollection COLLECTION = TypeSerializerCollection
    .builder()
    .registerAll(
      ConfigurateComponentSerializer
        .builder()
        .outputStringComponents(true)
        .scalarSerializer(NonItalicFallbackComponentSerializer.INSTANCE)
        .build()
        .serializers()
    )
    .build();

  @NotNull
  public YamlConfigurationLoader yaml(@NotNull final Path path) {
    return YamlConfigurationLoader
      .builder()
      .path(path)
      .indent(2)
      .nodeStyle(NodeStyle.BLOCK)
      .defaultOptions(options ->
        options.serializers(builder ->
          builder
            .registerAnnotatedObjects(
              ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build()
            )
            .registerAll(Configs.COLLECTION)
        )
      )
      .build();
  }

  @SneakyThrows
  <T extends Config> void reload(@NotNull final T instance) {
    final var loader = instance.loader();
    final var node = loader.load();
    final var factory = (ObjectMapper.Factory) Objects.requireNonNull(
      node.options().serializers().get(instance.getClass())
    );
    final var mutable = (ObjectMapper.Mutable<T>) factory.get(instance.getClass());
    mutable.load(instance, node);
    loader.save(node);
  }

  @SneakyThrows
  <T extends Config> void save(@NotNull final T instance) {
    final var loader = instance.loader();
    loader.save(loader.createNode().set(instance.getClass(), instance));
  }
}
