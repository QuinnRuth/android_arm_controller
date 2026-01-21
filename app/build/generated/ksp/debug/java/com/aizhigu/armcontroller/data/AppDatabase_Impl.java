package com.aizhigu.armcontroller.data;

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
  private volatile ActionDao _actionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `action_projects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `remoteSlotId` INTEGER, `createdAt` INTEGER NOT NULL, `modifiedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `action_frames` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectId` INTEGER NOT NULL, `sequenceId` INTEGER NOT NULL, `duration` INTEGER NOT NULL, `servo1` INTEGER NOT NULL, `servo2` INTEGER NOT NULL, `servo3` INTEGER NOT NULL, `servo4` INTEGER NOT NULL, `servo5` INTEGER NOT NULL, `servo6` INTEGER NOT NULL, `soundId` INTEGER, FOREIGN KEY(`projectId`) REFERENCES `action_projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '95e7a9a3d34eaf6e28ed8fe2e09e7f11')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `action_projects`");
        db.execSQL("DROP TABLE IF EXISTS `action_frames`");
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
        final HashMap<String, TableInfo.Column> _columnsActionProjects = new HashMap<String, TableInfo.Column>(5);
        _columnsActionProjects.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionProjects.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionProjects.put("remoteSlotId", new TableInfo.Column("remoteSlotId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionProjects.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionProjects.put("modifiedAt", new TableInfo.Column("modifiedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysActionProjects = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesActionProjects = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoActionProjects = new TableInfo("action_projects", _columnsActionProjects, _foreignKeysActionProjects, _indicesActionProjects);
        final TableInfo _existingActionProjects = TableInfo.read(db, "action_projects");
        if (!_infoActionProjects.equals(_existingActionProjects)) {
          return new RoomOpenHelper.ValidationResult(false, "action_projects(com.aizhigu.armcontroller.data.ActionProjectEntity).\n"
                  + " Expected:\n" + _infoActionProjects + "\n"
                  + " Found:\n" + _existingActionProjects);
        }
        final HashMap<String, TableInfo.Column> _columnsActionFrames = new HashMap<String, TableInfo.Column>(11);
        _columnsActionFrames.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("projectId", new TableInfo.Column("projectId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("sequenceId", new TableInfo.Column("sequenceId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("duration", new TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("servo1", new TableInfo.Column("servo1", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("servo2", new TableInfo.Column("servo2", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("servo3", new TableInfo.Column("servo3", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("servo4", new TableInfo.Column("servo4", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("servo5", new TableInfo.Column("servo5", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("servo6", new TableInfo.Column("servo6", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsActionFrames.put("soundId", new TableInfo.Column("soundId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysActionFrames = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysActionFrames.add(new TableInfo.ForeignKey("action_projects", "CASCADE", "NO ACTION", Arrays.asList("projectId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesActionFrames = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoActionFrames = new TableInfo("action_frames", _columnsActionFrames, _foreignKeysActionFrames, _indicesActionFrames);
        final TableInfo _existingActionFrames = TableInfo.read(db, "action_frames");
        if (!_infoActionFrames.equals(_existingActionFrames)) {
          return new RoomOpenHelper.ValidationResult(false, "action_frames(com.aizhigu.armcontroller.data.ActionFrameEntity).\n"
                  + " Expected:\n" + _infoActionFrames + "\n"
                  + " Found:\n" + _existingActionFrames);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "95e7a9a3d34eaf6e28ed8fe2e09e7f11", "c2e3ef270b6f2730978940a0f36e2f87");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "action_projects","action_frames");
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
      _db.execSQL("DELETE FROM `action_projects`");
      _db.execSQL("DELETE FROM `action_frames`");
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
    _typeConvertersMap.put(ActionDao.class, ActionDao_Impl.getRequiredConverters());
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
  public ActionDao actionDao() {
    if (_actionDao != null) {
      return _actionDao;
    } else {
      synchronized(this) {
        if(_actionDao == null) {
          _actionDao = new ActionDao_Impl(this);
        }
        return _actionDao;
      }
    }
  }
}
