package io.github.portlek.realmformat.paper.api.internal.config;

import java.nio.file.Path;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@UtilityClass
public class Configs {

    @NotNull
    public YamlConfigurationLoader yaml(@NotNull final Path path) {
        return YamlConfigurationLoader
            .builder()
            .path(path)
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
            .defaultOptions(options ->
                options.serializers(builder ->
                    builder.registerAnnotatedObjects(
                        ObjectMapper
                            .factoryBuilder()
                            .addNodeResolver(NodeResolver.onlyWithSetting())
                            .build()
                    )
                )
            )
            .build();
    }

    @SneakyThrows
    <T extends Config> void reload(@NotNull final T instance) {
        final ConfigurationLoader<?> loader = instance.loader();
        final ConfigurationNode node = loader.load();
        final ObjectMapper.Factory factory = (ObjectMapper.Factory) Objects.requireNonNull(
            node.options().serializers().get(instance.getClass())
        );
        //noinspection unchecked
        final ObjectMapper.Mutable<T> mutable = (ObjectMapper.Mutable<T>) factory.get(
            instance.getClass()
        );
        mutable.load(instance, node);
        loader.save(node);
    }

    @SneakyThrows
    <T extends Config> void save(@NotNull final T instance) {
        final ConfigurationLoader<?> loader = instance.loader();
        loader.save(loader.createNode().set(instance.getClass(), instance));
    }
}
