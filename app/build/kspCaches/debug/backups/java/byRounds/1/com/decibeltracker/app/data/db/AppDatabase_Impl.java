package com.decibeltracker.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile RecordingDao _recordingDao;

  private volatile MeasurementSampleDao _measurementSampleDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `recordings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `startTimeEpochMillis` INTEGER NOT NULL, `endTimeEpochMillis` INTEGER, `durationMillis` INTEGER NOT NULL, `status` TEXT NOT NULL, `minimumLevel` REAL, `maximumLevel` REAL, `averageLevel` REAL, `sampleCount` INTEGER NOT NULL, `measurementUnit` TEXT NOT NULL, `calibrationOffset` REAL NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `measurement_samples` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `recordingId` INTEGER NOT NULL, `elapsedMillis` INTEGER NOT NULL, `timestampEpochMillis` INTEGER NOT NULL, `levelValue` REAL NOT NULL, FOREIGN KEY(`recordingId`) REFERENCES `recordings`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_measurement_samples_recordingId` ON `measurement_samples` (`recordingId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_measurement_samples_recordingId_elapsedMillis` ON `measurement_samples` (`recordingId`, `elapsedMillis`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'da7af27043ac52701b01697fd2b2a513')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `recordings`");
        db.execSQL("DROP TABLE IF EXISTS `measurement_samples`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsRecordings = new HashMap<String, TableInfo.Column>(12);
        _columnsRecordings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("startTimeEpochMillis", new TableInfo.Column("startTimeEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("endTimeEpochMillis", new TableInfo.Column("endTimeEpochMillis", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("durationMillis", new TableInfo.Column("durationMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("minimumLevel", new TableInfo.Column("minimumLevel", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("maximumLevel", new TableInfo.Column("maximumLevel", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("averageLevel", new TableInfo.Column("averageLevel", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("sampleCount", new TableInfo.Column("sampleCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("measurementUnit", new TableInfo.Column("measurementUnit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRecordings.put("calibrationOffset", new TableInfo.Column("calibrationOffset", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRecordings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRecordings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoRecordings = new TableInfo("recordings", _columnsRecordings, _foreignKeysRecordings, _indicesRecordings);
        final TableInfo _existingRecordings = TableInfo.read(db, "recordings");
        if (!_infoRecordings.equals(_existingRecordings)) {
          return new RoomOpenHelper.ValidationResult(false, "recordings(com.decibeltracker.app.data.db.RecordingEntity).\n"
                  + " Expected:\n" + _infoRecordings + "\n"
                  + " Found:\n" + _existingRecordings);
        }
        final HashMap<String, TableInfo.Column> _columnsMeasurementSamples = new HashMap<String, TableInfo.Column>(5);
        _columnsMeasurementSamples.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeasurementSamples.put("recordingId", new TableInfo.Column("recordingId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeasurementSamples.put("elapsedMillis", new TableInfo.Column("elapsedMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeasurementSamples.put("timestampEpochMillis", new TableInfo.Column("timestampEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMeasurementSamples.put("levelValue", new TableInfo.Column("levelValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMeasurementSamples = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMeasurementSamples.add(new TableInfo.ForeignKey("recordings", "CASCADE", "NO ACTION", Arrays.asList("recordingId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMeasurementSamples = new HashSet<TableInfo.Index>(2);
        _indicesMeasurementSamples.add(new TableInfo.Index("index_measurement_samples_recordingId", false, Arrays.asList("recordingId"), Arrays.asList("ASC")));
        _indicesMeasurementSamples.add(new TableInfo.Index("index_measurement_samples_recordingId_elapsedMillis", false, Arrays.asList("recordingId", "elapsedMillis"), Arrays.asList("ASC", "ASC")));
        final TableInfo _infoMeasurementSamples = new TableInfo("measurement_samples", _columnsMeasurementSamples, _foreignKeysMeasurementSamples, _indicesMeasurementSamples);
        final TableInfo _existingMeasurementSamples = TableInfo.read(db, "measurement_samples");
        if (!_infoMeasurementSamples.equals(_existingMeasurementSamples)) {
          return new RoomOpenHelper.ValidationResult(false, "measurement_samples(com.decibeltracker.app.data.db.MeasurementSampleEntity).\n"
                  + " Expected:\n" + _infoMeasurementSamples + "\n"
                  + " Found:\n" + _existingMeasurementSamples);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "da7af27043ac52701b01697fd2b2a513", "2b99c516cc8fa304e6bdb7cc34a10120");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "recordings","measurement_samples");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `recordings`");
      _db.execSQL("DELETE FROM `measurement_samples`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RecordingDao.class, RecordingDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MeasurementSampleDao.class, MeasurementSampleDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RecordingDao recordingDao() {
    if (_recordingDao != null) {
      return _recordingDao;
    } else {
      synchronized(this) {
        if(_recordingDao == null) {
          _recordingDao = new RecordingDao_Impl(this);
        }
        return _recordingDao;
      }
    }
  }

  @Override
  public MeasurementSampleDao sampleDao() {
    if (_measurementSampleDao != null) {
      return _measurementSampleDao;
    } else {
      synchronized(this) {
        if(_measurementSampleDao == null) {
          _measurementSampleDao = new MeasurementSampleDao_Impl(this);
        }
        return _measurementSampleDao;
      }
    }
  }
}
