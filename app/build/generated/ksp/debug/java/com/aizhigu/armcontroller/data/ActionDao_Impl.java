package com.aizhigu.armcontroller.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ActionDao_Impl implements ActionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ActionProjectEntity> __insertionAdapterOfActionProjectEntity;

  private final EntityInsertionAdapter<ActionFrameEntity> __insertionAdapterOfActionFrameEntity;

  private final EntityDeletionOrUpdateAdapter<ActionProjectEntity> __deletionAdapterOfActionProjectEntity;

  private final EntityDeletionOrUpdateAdapter<ActionFrameEntity> __deletionAdapterOfActionFrameEntity;

  private final EntityDeletionOrUpdateAdapter<ActionProjectEntity> __updateAdapterOfActionProjectEntity;

  private final EntityDeletionOrUpdateAdapter<ActionFrameEntity> __updateAdapterOfActionFrameEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProjectById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteFrameBySequenceId;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllFramesForProject;

  public ActionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfActionProjectEntity = new EntityInsertionAdapter<ActionProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `action_projects` (`id`,`name`,`remoteSlotId`,`createdAt`,`modifiedAt`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ActionProjectEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getRemoteSlotId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getRemoteSlotId());
        }
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindLong(5, entity.getModifiedAt());
      }
    };
    this.__insertionAdapterOfActionFrameEntity = new EntityInsertionAdapter<ActionFrameEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `action_frames` (`id`,`projectId`,`sequenceId`,`duration`,`servo1`,`servo2`,`servo3`,`servo4`,`servo5`,`servo6`,`soundId`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ActionFrameEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getProjectId());
        statement.bindLong(3, entity.getSequenceId());
        statement.bindLong(4, entity.getDuration());
        statement.bindLong(5, entity.getServo1());
        statement.bindLong(6, entity.getServo2());
        statement.bindLong(7, entity.getServo3());
        statement.bindLong(8, entity.getServo4());
        statement.bindLong(9, entity.getServo5());
        statement.bindLong(10, entity.getServo6());
        if (entity.getSoundId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getSoundId());
        }
      }
    };
    this.__deletionAdapterOfActionProjectEntity = new EntityDeletionOrUpdateAdapter<ActionProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `action_projects` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ActionProjectEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfActionFrameEntity = new EntityDeletionOrUpdateAdapter<ActionFrameEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `action_frames` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ActionFrameEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfActionProjectEntity = new EntityDeletionOrUpdateAdapter<ActionProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `action_projects` SET `id` = ?,`name` = ?,`remoteSlotId` = ?,`createdAt` = ?,`modifiedAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ActionProjectEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        if (entity.getRemoteSlotId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getRemoteSlotId());
        }
        statement.bindLong(4, entity.getCreatedAt());
        statement.bindLong(5, entity.getModifiedAt());
        statement.bindLong(6, entity.getId());
      }
    };
    this.__updateAdapterOfActionFrameEntity = new EntityDeletionOrUpdateAdapter<ActionFrameEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `action_frames` SET `id` = ?,`projectId` = ?,`sequenceId` = ?,`duration` = ?,`servo1` = ?,`servo2` = ?,`servo3` = ?,`servo4` = ?,`servo5` = ?,`servo6` = ?,`soundId` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ActionFrameEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getProjectId());
        statement.bindLong(3, entity.getSequenceId());
        statement.bindLong(4, entity.getDuration());
        statement.bindLong(5, entity.getServo1());
        statement.bindLong(6, entity.getServo2());
        statement.bindLong(7, entity.getServo3());
        statement.bindLong(8, entity.getServo4());
        statement.bindLong(9, entity.getServo5());
        statement.bindLong(10, entity.getServo6());
        if (entity.getSoundId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getSoundId());
        }
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteProjectById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM action_projects WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteFrameBySequenceId = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM action_frames WHERE projectId = ? AND sequenceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllFramesForProject = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM action_frames WHERE projectId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertProject(final ActionProjectEntity project,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfActionProjectEntity.insertAndReturnId(project);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFrame(final ActionFrameEntity frame,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfActionFrameEntity.insertAndReturnId(frame);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertFrames(final List<ActionFrameEntity> frames,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfActionFrameEntity.insert(frames);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProject(final ActionProjectEntity project,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfActionProjectEntity.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFrame(final ActionFrameEntity frame,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfActionFrameEntity.handle(frame);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProject(final ActionProjectEntity project,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfActionProjectEntity.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFrame(final ActionFrameEntity frame,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfActionFrameEntity.handle(frame);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertProjectWithFrames(final ActionProjectEntity project,
      final List<ActionFrameEntity> frames, final Continuation<? super Long> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ActionDao.DefaultImpls.insertProjectWithFrames(ActionDao_Impl.this, project, frames, __cont), $completion);
  }

  @Override
  public Object getProjectWithFrames(final long projectId,
      final Continuation<? super ActionProject> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ActionDao.DefaultImpls.getProjectWithFrames(ActionDao_Impl.this, projectId, __cont), $completion);
  }

  @Override
  public Object getAllProjectsWithFrames(
      final Continuation<? super List<ActionProject>> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ActionDao.DefaultImpls.getAllProjectsWithFrames(ActionDao_Impl.this, __cont), $completion);
  }

  @Override
  public Object updateProjectWithFrames(final ActionProject project,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> ActionDao.DefaultImpls.updateProjectWithFrames(ActionDao_Impl.this, project, __cont), $completion);
  }

  @Override
  public Object deleteProjectById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProjectById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteProjectById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteFrameBySequenceId(final long projectId, final int sequenceId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteFrameBySequenceId.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, projectId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, sequenceId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteFrameBySequenceId.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllFramesForProject(final long projectId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllFramesForProject.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, projectId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllFramesForProject.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ActionProjectEntity>> getAllProjects() {
    final String _sql = "SELECT * FROM action_projects ORDER BY modifiedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"action_projects"}, new Callable<List<ActionProjectEntity>>() {
      @Override
      @NonNull
      public List<ActionProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRemoteSlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteSlotId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "modifiedAt");
          final List<ActionProjectEntity> _result = new ArrayList<ActionProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ActionProjectEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Integer _tmpRemoteSlotId;
            if (_cursor.isNull(_cursorIndexOfRemoteSlotId)) {
              _tmpRemoteSlotId = null;
            } else {
              _tmpRemoteSlotId = _cursor.getInt(_cursorIndexOfRemoteSlotId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpModifiedAt;
            _tmpModifiedAt = _cursor.getLong(_cursorIndexOfModifiedAt);
            _item = new ActionProjectEntity(_tmpId,_tmpName,_tmpRemoteSlotId,_tmpCreatedAt,_tmpModifiedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getProjectById(final long id,
      final Continuation<? super ActionProjectEntity> $completion) {
    final String _sql = "SELECT * FROM action_projects WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ActionProjectEntity>() {
      @Override
      @Nullable
      public ActionProjectEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRemoteSlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteSlotId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "modifiedAt");
          final ActionProjectEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Integer _tmpRemoteSlotId;
            if (_cursor.isNull(_cursorIndexOfRemoteSlotId)) {
              _tmpRemoteSlotId = null;
            } else {
              _tmpRemoteSlotId = _cursor.getInt(_cursorIndexOfRemoteSlotId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpModifiedAt;
            _tmpModifiedAt = _cursor.getLong(_cursorIndexOfModifiedAt);
            _result = new ActionProjectEntity(_tmpId,_tmpName,_tmpRemoteSlotId,_tmpCreatedAt,_tmpModifiedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getFramesByProjectId(final long projectId,
      final Continuation<? super List<ActionFrameEntity>> $completion) {
    final String _sql = "SELECT * FROM action_frames WHERE projectId = ? ORDER BY sequenceId ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, projectId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ActionFrameEntity>>() {
      @Override
      @NonNull
      public List<ActionFrameEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "projectId");
          final int _cursorIndexOfSequenceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sequenceId");
          final int _cursorIndexOfDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "duration");
          final int _cursorIndexOfServo1 = CursorUtil.getColumnIndexOrThrow(_cursor, "servo1");
          final int _cursorIndexOfServo2 = CursorUtil.getColumnIndexOrThrow(_cursor, "servo2");
          final int _cursorIndexOfServo3 = CursorUtil.getColumnIndexOrThrow(_cursor, "servo3");
          final int _cursorIndexOfServo4 = CursorUtil.getColumnIndexOrThrow(_cursor, "servo4");
          final int _cursorIndexOfServo5 = CursorUtil.getColumnIndexOrThrow(_cursor, "servo5");
          final int _cursorIndexOfServo6 = CursorUtil.getColumnIndexOrThrow(_cursor, "servo6");
          final int _cursorIndexOfSoundId = CursorUtil.getColumnIndexOrThrow(_cursor, "soundId");
          final List<ActionFrameEntity> _result = new ArrayList<ActionFrameEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ActionFrameEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProjectId;
            _tmpProjectId = _cursor.getLong(_cursorIndexOfProjectId);
            final int _tmpSequenceId;
            _tmpSequenceId = _cursor.getInt(_cursorIndexOfSequenceId);
            final int _tmpDuration;
            _tmpDuration = _cursor.getInt(_cursorIndexOfDuration);
            final int _tmpServo1;
            _tmpServo1 = _cursor.getInt(_cursorIndexOfServo1);
            final int _tmpServo2;
            _tmpServo2 = _cursor.getInt(_cursorIndexOfServo2);
            final int _tmpServo3;
            _tmpServo3 = _cursor.getInt(_cursorIndexOfServo3);
            final int _tmpServo4;
            _tmpServo4 = _cursor.getInt(_cursorIndexOfServo4);
            final int _tmpServo5;
            _tmpServo5 = _cursor.getInt(_cursorIndexOfServo5);
            final int _tmpServo6;
            _tmpServo6 = _cursor.getInt(_cursorIndexOfServo6);
            final Integer _tmpSoundId;
            if (_cursor.isNull(_cursorIndexOfSoundId)) {
              _tmpSoundId = null;
            } else {
              _tmpSoundId = _cursor.getInt(_cursorIndexOfSoundId);
            }
            _item = new ActionFrameEntity(_tmpId,_tmpProjectId,_tmpSequenceId,_tmpDuration,_tmpServo1,_tmpServo2,_tmpServo3,_tmpServo4,_tmpServo5,_tmpServo6,_tmpSoundId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllProjectsSync(
      final Continuation<? super List<ActionProjectEntity>> $completion) {
    final String _sql = "SELECT * FROM action_projects ORDER BY modifiedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ActionProjectEntity>>() {
      @Override
      @NonNull
      public List<ActionProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRemoteSlotId = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteSlotId");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfModifiedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "modifiedAt");
          final List<ActionProjectEntity> _result = new ArrayList<ActionProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ActionProjectEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final Integer _tmpRemoteSlotId;
            if (_cursor.isNull(_cursorIndexOfRemoteSlotId)) {
              _tmpRemoteSlotId = null;
            } else {
              _tmpRemoteSlotId = _cursor.getInt(_cursorIndexOfRemoteSlotId);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpModifiedAt;
            _tmpModifiedAt = _cursor.getLong(_cursorIndexOfModifiedAt);
            _item = new ActionProjectEntity(_tmpId,_tmpName,_tmpRemoteSlotId,_tmpCreatedAt,_tmpModifiedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
