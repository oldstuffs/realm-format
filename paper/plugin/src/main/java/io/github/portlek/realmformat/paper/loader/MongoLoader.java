package io.github.portlek.realmformat.paper.loader;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mongodb.MongoException;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import io.github.portlek.realmformat.format.exception.UnknownWorldException;
import io.github.portlek.realmformat.format.exception.WorldInUseException;
import io.github.portlek.realmformat.paper.misc.MongoCredential;
import io.github.portlek.realmformat.paper.misc.RealmConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import tr.com.infumia.task.Schedulers;

@Log4j2
@SuppressWarnings("rawtypes")
public final class MongoLoader extends UpdatableLoader {

  private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(
    2,
    new ThreadFactoryBuilder().setNameFormat("RF MongoDB Lock Pool Thread #%1$d").build()
  );

  private final MongoClient client;

  private final String collection;

  private final String database;

  private final Map<String, ScheduledFuture> lockedWorlds = new HashMap<>();

  public MongoLoader(@NotNull final MongoCredential credential) throws MongoException {
    this.database = credential.database();
    this.collection = credential.collection();
    this.client = MongoClients.create(credential.parseUri());
    final var mongoDatabase = this.client.getDatabase(this.database);
    final var mongoCollection = mongoDatabase.getCollection(this.collection);
    mongoCollection.createIndex(Indexes.ascending("name"), new IndexOptions().unique(true));
  }

  @Override
  public void deleteWorld(@NotNull final String worldName)
    throws IOException, UnknownWorldException {
    final var future = this.lockedWorlds.remove(worldName);
    if (future != null) {
      future.cancel(false);
    }
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var bucket = GridFSBuckets.create(mongoDatabase, this.collection);
      final var file = bucket.find(Filters.eq("filename", worldName)).first();
      if (file == null) {
        throw new UnknownWorldException(worldName);
      }
      bucket.delete(file.getObjectId());
      for (final var backupFile : bucket.find(Filters.eq("filename", worldName + "_backup"))) {
        bucket.delete(backupFile.getObjectId());
      }
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      mongoCollection.deleteOne(Filters.eq("name", worldName));
    } catch (final MongoException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public boolean isWorldLocked(@NotNull final String worldName)
    throws IOException, UnknownWorldException {
    if (this.lockedWorlds.containsKey(worldName)) {
      return true;
    }
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      final var worldDoc = mongoCollection.find(Filters.eq("name", worldName)).first();
      if (worldDoc == null) {
        throw new UnknownWorldException(worldName);
      }
      return (
        System.currentTimeMillis() - worldDoc.getLong("locked") <= RealmConstants.MAX_LOCK_TIME
      );
    } catch (final MongoException ex) {
      throw new IOException(ex);
    }
  }

  @NotNull
  @Override
  public List<String> listWorlds() throws IOException {
    final List<String> worldList = new ArrayList<>();
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      final var documents = mongoCollection.find().cursor();
      while (documents.hasNext()) {
        worldList.add(documents.next().getString("name"));
      }
    } catch (final MongoException ex) {
      throw new IOException(ex);
    }
    return worldList;
  }

  @Override
  public byte@NotNull[] loadWorld(@NotNull final String worldName, final boolean readOnly)
    throws UnknownWorldException, IOException, WorldInUseException {
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      final var worldDoc = mongoCollection.find(Filters.eq("name", worldName)).first();
      if (worldDoc == null) {
        throw new UnknownWorldException(worldName);
      }
      if (!readOnly) {
        final long lockedMillis = worldDoc.getLong("locked");
        if (System.currentTimeMillis() - lockedMillis <= RealmConstants.MAX_LOCK_TIME) {
          throw new WorldInUseException(worldName);
        }
        this.updateLock(worldName, true);
      }
      final var bucket = GridFSBuckets.create(mongoDatabase, this.collection);
      final var stream = new ByteArrayOutputStream();
      bucket.downloadToStream(worldName, stream);
      return stream.toByteArray();
    } catch (final MongoException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public void saveWorld(
    @NotNull final String worldName,
    final byte@NotNull[] serializedWorld,
    final boolean lock
  ) throws IOException {
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var bucket = GridFSBuckets.create(mongoDatabase, this.collection);
      final var oldFile = bucket.find(Filters.eq("filename", worldName)).first();
      bucket.uploadFromStream(worldName, new ByteArrayInputStream(serializedWorld));
      if (oldFile != null) {
        bucket.delete(oldFile.getObjectId());
      }
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      final var worldDoc = mongoCollection.find(Filters.eq("name", worldName)).first();
      final var lockMillis = lock ? System.currentTimeMillis() : 0L;
      if (worldDoc == null) {
        mongoCollection.insertOne(
          new Document().append("name", worldName).append("locked", lockMillis)
        );
      } else if (
        System.currentTimeMillis() - worldDoc.getLong("locked") > RealmConstants.MAX_LOCK_TIME &&
        lock
      ) {
        this.updateLock(worldName, true);
      }
    } catch (final MongoException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public void unlockWorld(@NotNull final String worldName)
    throws IOException, UnknownWorldException {
    final var future = this.lockedWorlds.remove(worldName);
    if (future != null) {
      future.cancel(false);
    }
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      final var result = mongoCollection.updateOne(
        Filters.eq("name", worldName),
        Updates.set("locked", 0L)
      );
      if (result.getMatchedCount() == 0) {
        throw new UnknownWorldException(worldName);
      }
    } catch (final MongoException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public boolean worldExists(@NotNull final String worldName) throws IOException {
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      final var worldDoc = mongoCollection.find(Filters.eq("name", worldName)).first();
      return worldDoc != null;
    } catch (final MongoException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public void update() {
    final var mongoDatabase = this.client.getDatabase(this.database);
    for (final var collectionName : mongoDatabase.listCollectionNames()) {
      if (
        collectionName.equals(this.collection + "_files.files") ||
        collectionName.equals(this.collection + "_files.chunks")
      ) {
        MongoLoader.log.info("Updating MongoDB database...");
        mongoDatabase
          .getCollection(this.collection + "_files.files")
          .renameCollection(new MongoNamespace(this.database, this.collection + ".files"));
        mongoDatabase
          .getCollection(this.collection + "_files.chunks")
          .renameCollection(new MongoNamespace(this.database, this.collection + ".chunks"));
        MongoLoader.log.info("MongoDB database updated!");
        break;
      }
    }
    final var mongoCollection = mongoDatabase.getCollection(this.collection);
    @Cleanup
    final var documents = mongoCollection
      .find(Filters.or(Filters.eq("locked", true), Filters.eq("locked", false)))
      .cursor();
    if (documents.hasNext()) {
      MongoLoader.log.error(
        "Your RF MongoDB database is outdated. The update process will start in 10 seconds."
      );
      MongoLoader.log.error(
        "Note that this update will make your database incompatible with older SWM versions."
      );
      MongoLoader.log.error(
        "Make sure no other servers with older SWM versions are using this database."
      );
      MongoLoader.log.error("Shut down the server to prevent your database from being updated.");
      Schedulers
        .sync()
        .runLater(
          () -> {
            while (documents.hasNext()) {
              final var name = documents.next().getString("name");
              mongoCollection.updateOne(Filters.eq("name", name), Updates.set("locked", 0L));
            }
          },
          Duration.ofSeconds(10L)
        );
    }
  }

  private void updateLock(final String worldName, final boolean forceSchedule) {
    try {
      final var mongoDatabase = this.client.getDatabase(this.database);
      final var mongoCollection = mongoDatabase.getCollection(this.collection);
      mongoCollection.updateOne(
        Filters.eq("name", worldName),
        Updates.set("locked", System.currentTimeMillis())
      );
    } catch (final MongoException ex) {
      MongoLoader.log.error("Failed to update the lock for world " + worldName + ":");
      ex.printStackTrace();
    }
    if (forceSchedule || this.lockedWorlds.containsKey(worldName)) {
      this.lockedWorlds.put(
          worldName,
          MongoLoader.SERVICE.schedule(
            () -> this.updateLock(worldName, false),
            RealmConstants.LOCK_INTERVAL,
            TimeUnit.MILLISECONDS
          )
        );
    }
  }
}
