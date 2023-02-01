package io.github.portlek.realmformat.paper.nms.v1_18_R2;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import io.github.portlek.realmformat.format.loader.RealmLoader;
import io.github.portlek.realmformat.format.property.RealmProperties;
import io.github.portlek.realmformat.format.property.RealmPropertyMap;
import io.github.portlek.realmformat.format.realm.RealmChunk;
import io.github.portlek.realmformat.format.realm.RealmWorld;
import io.github.portlek.realmformat.paper.nms.RealmNmsBackend;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.ListTag;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelVersion;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.scoreboard.CraftScoreboardManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log4j2
public final class RealmNmsBackend1_18_R2 implements RealmNmsBackend {

  static LevelStorageSource CUSTOM_LEVEL_STORAGE;

  static boolean isPaperMC;

  @Getter
  private final byte worldVersion = 0x08;

  private RealmWorldServer defaultEndWorld;

  private RealmWorldServer defaultNetherWorld;

  private RealmWorldServer defaultWorld;

  private boolean injectFakeDimensions = false;

  static {
    try {
      final var path = Files
        .createTempDirectory("rf-" + UUID.randomUUID().toString().substring(0, 5))
        .toAbsolutePath();
      RealmNmsBackend1_18_R2.CUSTOM_LEVEL_STORAGE =
        new LevelStorageSource(path, path, DataFixers.getDataFixer());
      FileUtils.forceDeleteOnExit(path.toFile());
    } catch (final IOException ex) {
      throw new IllegalStateException("Couldn't create dummy file directory.", ex);
    }
  }

  public RealmNmsBackend1_18_R2(final boolean isPaper) {
    try {
      RealmNmsBackend1_18_R2.isPaperMC = isPaper;
      ModifierBackend1_18_R2.initialize(this);
    } catch (final NoClassDefFoundError ex) {
      RealmNmsBackend1_18_R2.log.error(
        "Failed to find ClassModifier classes. Are you sure you installed it correctly?",
        ex
      );
      Bukkit.getServer().shutdown();
    }
  }

  @NotNull
  private static RealmWorldServer createCustomWorld(
    @NotNull final RealmWorld world,
    @Nullable final ResourceKey<Level> dimensionOverride
  ) {
    final var nmsWorld = (RealmWorld1_18_R2) world;
    final var worldName = world.name();
    final var worldDataServer = RealmNmsBackend1_18_R2.createWorldData(world);
    final var environment = RealmNmsBackend1_18_R2.getEnvironment(world);
    final var dimension =
      switch (environment) {
        case NORMAL -> LevelStem.OVERWORLD;
        case NETHER -> LevelStem.NETHER;
        case THE_END -> LevelStem.END;
        default -> throw new IllegalArgumentException("Unknown dimension supplied");
      };
    final var registryMaterials = worldDataServer.worldGenSettings().dimensions();
    final var worldDimension = Objects.requireNonNull(
      registryMaterials.get(dimension),
      "Something went wrong!"
    );
    final var predefinedType = worldDimension.typeHolder().value();
    final var fixedTime =
      switch (environment) {
        case NORMAL -> OptionalLong.empty();
        case NETHER -> OptionalLong.of(18000L);
        case THE_END -> OptionalLong.of(6000L);
        case CUSTOM -> throw new UnsupportedOperationException();
      };
    final var light =
      switch (environment) {
        case NORMAL, THE_END -> 0;
        case NETHER -> 0.1;
        case CUSTOM -> throw new UnsupportedOperationException();
      };
    final var infiniburn =
      switch (environment) {
        case NORMAL -> BlockTags.INFINIBURN_OVERWORLD;
        case NETHER -> BlockTags.INFINIBURN_NETHER;
        case THE_END -> BlockTags.INFINIBURN_END;
        case CUSTOM -> throw new UnsupportedOperationException();
      };
    final var type = Holder.direct(
      DimensionType.create(
        fixedTime,
        predefinedType.hasSkyLight(),
        predefinedType.hasCeiling(),
        predefinedType.ultraWarm(),
        predefinedType.natural(),
        predefinedType.coordinateScale(),
        world.propertyMap().getValue(RealmProperties.DRAGON_BATTLE),
        predefinedType.piglinSafe(),
        predefinedType.bedWorks(),
        predefinedType.respawnAnchorWorks(),
        predefinedType.hasRaids(),
        predefinedType.minY(),
        predefinedType.height(),
        predefinedType.logicalHeight(),
        infiniburn,
        predefinedType.effectsLocation(),
        (float) light
      )
    );
    final var chunkGenerator = worldDimension.generator();
    final var worldKey = dimensionOverride == null
      ? ResourceKey.create(
        Registry.DIMENSION_REGISTRY,
        new ResourceLocation(worldName.toLowerCase(java.util.Locale.ENGLISH))
      )
      : dimensionOverride;
    final RealmWorldServer level;
    final var server = MinecraftServer.getServer().server;
    try {
      level =
        new RealmWorldServer(
          nmsWorld,
          worldDataServer,
          worldKey,
          dimension,
          type,
          chunkGenerator,
          environment,
          server.getGenerator(worldName),
          server.getBiomeProvider(worldName)
        );
      nmsWorld.handle(level);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
    level.ready(true);
    level.setSpawnSettings(
      world.propertyMap().getValue(RealmProperties.ALLOW_MONSTERS),
      world.propertyMap().getValue(RealmProperties.ALLOW_ANIMALS)
    );
    return level;
  }

  @NotNull
  private static PrimaryLevelData createWorldData(@NotNull final RealmWorld world) {
    final var worldName = world.name();
    final var extraData = world.extraData();
    final var extraTag = (net.minecraft.nbt.CompoundTag) Converter.convertTag(extraData);
    final var mcServer = MinecraftServer.getServer();
    final var serverProps = ((DedicatedServer) mcServer).getProperties();
    final PrimaryLevelData worldDataServer;
    if (extraTag.getTagType("LevelData") == Tag.TAG_COMPOUND) {
      final var levelData = extraTag.getCompound("LevelData");
      final var dataVersion = levelData.getTagType("DataVersion") == Tag.TAG_INT
        ? levelData.getInt("DataVersion")
        : -1;
      final var dynamic = mcServer
        .getFixerUpper()
        .update(
          DataFixTypes.LEVEL.getType(),
          new Dynamic<>(NbtOps.INSTANCE, levelData),
          dataVersion,
          SharedConstants.getCurrentVersion().getDataVersion().getVersion()
        );
      final var levelVersion = LevelVersion.parse(dynamic);
      final var worldSettings = LevelSettings.parse(dynamic, mcServer.datapackconfiguration);
      worldDataServer =
        PrimaryLevelData.parse(
          dynamic,
          mcServer.getFixerUpper(),
          dataVersion,
          null,
          worldSettings,
          levelVersion,
          serverProps.getWorldGenSettings(mcServer.registryHolder),
          Lifecycle.stable()
        );
    } else {
      final var gameRules = extraData.getCompoundTag("gamerules");
      final var rules = new GameRules();
      gameRules.ifPresent(compoundTag -> {
        final var compound = (net.minecraft.nbt.CompoundTag) Converter.convertTag(compoundTag);
        final var gameRuleKeys = CraftWorld.getGameRulesNMS();
        compound
          .getAllKeys()
          .forEach(gameRule -> {
            if (gameRuleKeys.containsKey(gameRule)) {
              final var gameRuleValue = rules.getRule(gameRuleKeys.get(gameRule));
              final var theValue = compound.getString(gameRule);
              gameRuleValue.deserialize(theValue);
              gameRuleValue.onChanged(null);
            }
          });
      });
      final var worldSettings = new LevelSettings(
        worldName,
        serverProps.gamemode,
        false,
        serverProps.difficulty,
        false,
        rules,
        mcServer.datapackconfiguration
      );
      worldDataServer =
        new PrimaryLevelData(
          worldSettings,
          serverProps.getWorldGenSettings(mcServer.registryHolder),
          Lifecycle.stable()
        );
    }
    worldDataServer.checkName(worldName);
    worldDataServer.setModdedInfo(
      mcServer.getServerModName(),
      mcServer.getModdedStatus().shouldReportAsModified()
    );
    worldDataServer.setInitialized(true);
    return worldDataServer;
  }

  @NotNull
  private static World.Environment getEnvironment(@NotNull final RealmWorld world) {
    return World.Environment.valueOf(
      world.propertyMap().getValue(RealmProperties.ENVIRONMENT).toUpperCase()
    );
  }

  private static void registerWorld(@NotNull final RealmWorldServer server) {
    final var mcServer = MinecraftServer.getServer();
    mcServer.initWorld(
      server,
      server.serverLevelData,
      mcServer.getWorldData(),
      server.serverLevelData.worldGenSettings()
    );
    mcServer.levels.put(server.dimension(), server);
  }

  @NotNull
  @Override
  public RealmWorld createRealmWorld(
    @NotNull final RealmLoader loader,
    @NotNull final String worldName,
    @NotNull final Map<Long, RealmChunk> chunks,
    @NotNull final CompoundTag extraCompound,
    @NotNull final ListTag mapList,
    final byte worldVersion,
    @NotNull final RealmPropertyMap worldPropertyMap,
    final boolean readOnly,
    final boolean lock,
    @NotNull final Map<Long, ListTag> entities
  ) {
    return new RealmWorld1_18_R2(
      worldVersion,
      loader,
      worldName,
      chunks,
      extraCompound,
      worldPropertyMap,
      readOnly,
      lock,
      entities
    );
  }

  @Override
  public void generateWorld(@NotNull final RealmWorld world) {
    final var worldName = world.name();
    Preconditions.checkNotNull(
      Bukkit.getWorld(worldName),
      "World %s already exists! Maybe it's an outdated RealmWorld object?",
      worldName
    );
    final var server = RealmNmsBackend1_18_R2.createCustomWorld(world, null);
    RealmNmsBackend1_18_R2.registerWorld(server);
  }

  @Nullable
  @Override
  public Object injectDefaultWorlds() {
    if (!this.injectFakeDimensions) {
      return null;
    }
    System.out.println(
      "INJECTING: " + this.defaultWorld + " " + this.defaultNetherWorld + " " + this.defaultEndWorld
    );
    final var server = MinecraftServer.getServer();
    server.server.scoreboardManager = new CraftScoreboardManager(server, server.getScoreboard());
    if (this.defaultWorld != null) {
      RealmNmsBackend1_18_R2.registerWorld(this.defaultWorld);
    }
    if (this.defaultNetherWorld != null) {
      RealmNmsBackend1_18_R2.registerWorld(this.defaultNetherWorld);
    }
    if (this.defaultEndWorld != null) {
      RealmNmsBackend1_18_R2.registerWorld(this.defaultEndWorld);
    }
    this.injectFakeDimensions = false;
    return new MappedRegistry<>(Registry.ACTIVITY_REGISTRY, Lifecycle.stable(), null);
  }

  @Nullable
  @Override
  public RealmWorld realmWorld(@NotNull final World world) {
    final var craftWorld = (CraftWorld) world;
    if (!(craftWorld.getHandle() instanceof RealmWorldServer worldServer)) {
      return null;
    }
    return worldServer.realmWorld();
  }

  @Override
  public void defaultWorlds(
    @Nullable final RealmWorld normalWorld,
    @Nullable final RealmWorld netherWorld,
    @Nullable final RealmWorld endWorld
  ) {
    try {
      final var server = MinecraftServer.getServer();
      final var dedicatedserverproperties = ((DedicatedServer) server).getProperties();
      final var worldsettings = new LevelSettings(
        dedicatedserverproperties.levelName,
        dedicatedserverproperties.gamemode,
        dedicatedserverproperties.hardcore,
        dedicatedserverproperties.difficulty,
        false,
        new GameRules(),
        server.datapackconfiguration
      );
      final var generatorsettings = dedicatedserverproperties.getWorldGenSettings(
        server.registryAccess()
      );
      final var data = new PrimaryLevelData(worldsettings, generatorsettings, Lifecycle.stable());
      //noinspection JavaReflectionMemberAccess
      final var field = MinecraftServer.class.getDeclaredField("p");
      field.setAccessible(true);
      field.set(server, data);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    if (normalWorld != null) {
      normalWorld
        .propertyMap()
        .setValue(RealmProperties.ENVIRONMENT, World.Environment.NORMAL.toString().toLowerCase());
      this.defaultWorld = RealmNmsBackend1_18_R2.createCustomWorld(normalWorld, Level.OVERWORLD);
      this.injectFakeDimensions = true;
    }
    if (netherWorld != null) {
      netherWorld
        .propertyMap()
        .setValue(RealmProperties.ENVIRONMENT, World.Environment.NETHER.toString().toLowerCase());
      this.defaultNetherWorld = RealmNmsBackend1_18_R2.createCustomWorld(netherWorld, Level.NETHER);
      this.injectFakeDimensions = true;
    }
    if (endWorld != null) {
      endWorld
        .propertyMap()
        .setValue(RealmProperties.ENVIRONMENT, World.Environment.THE_END.toString().toLowerCase());
      this.defaultEndWorld = RealmNmsBackend1_18_R2.createCustomWorld(endWorld, Level.END);
      this.injectFakeDimensions = true;
    }
  }
}
