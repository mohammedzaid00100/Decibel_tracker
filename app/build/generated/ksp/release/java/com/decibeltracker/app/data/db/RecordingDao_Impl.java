package com.decibeltracker.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.decibeltracker.app.data.model.RecordingStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.IllegalArgumentException;
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
public final class RecordingDao_Impl implements RecordingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RecordingEntity> __insertionAdapterOfRecordingEntity;

  private final EntityDeletionOrUpdateAdapter<RecordingEntity> __deletionAdapterOfRecordingEntity;

  private final EntityDeletionOrUpdateAdapter<RecordingEntity> __updateAdapterOfRecordingEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecordingById;

  private final SharedSQLiteStatement __preparedStmtOfRenameRecording;

  private final SharedSQLiteStatement __preparedStmtOfCompleteRecording;

  public RecordingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRecordingEntity = new EntityInsertionAdapter<RecordingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `recordings` (`id`,`title`,`startTimeEpochMillis`,`endTimeEpochMillis`,`durationMillis`,`status`,`minimumLevel`,`maximumLevel`,`averageLevel`,`sampleCount`,`measurementUnit`,`calibrationOffset`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordingEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getStartTimeEpochMillis());
        if (entity.getEndTimeEpochMillis() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getEndTimeEpochMillis());
        }
        statement.bindLong(5, entity.getDurationMillis());
        statement.bindString(6, __RecordingStatus_enumToString(entity.getStatus()));
        if (entity.getMinimumLevel() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getMinimumLevel());
        }
        if (entity.getMaximumLevel() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getMaximumLevel());
        }
        if (entity.getAverageLevel() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getAverageLevel());
        }
        statement.bindLong(10, entity.getSampleCount());
        statement.bindString(11, entity.getMeasurementUnit());
        statement.bindDouble(12, entity.getCalibrationOffset());
      }
    };
    this.__deletionAdapterOfRecordingEntity = new EntityDeletionOrUpdateAdapter<RecordingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `recordings` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordingEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfRecordingEntity = new EntityDeletionOrUpdateAdapter<RecordingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `recordings` SET `id` = ?,`title` = ?,`startTimeEpochMillis` = ?,`endTimeEpochMillis` = ?,`durationMillis` = ?,`status` = ?,`minimumLevel` = ?,`maximumLevel` = ?,`averageLevel` = ?,`sampleCount` = ?,`measurementUnit` = ?,`calibrationOffset` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RecordingEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getStartTimeEpochMillis());
        if (entity.getEndTimeEpochMillis() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getEndTimeEpochMillis());
        }
        statement.bindLong(5, entity.getDurationMillis());
        statement.bindString(6, __RecordingStatus_enumToString(entity.getStatus()));
        if (entity.getMinimumLevel() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getMinimumLevel());
        }
        if (entity.getMaximumLevel() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getMaximumLevel());
        }
        if (entity.getAverageLevel() == null) {
          statement.bindNull(9);
        } else {
          statement.bindDouble(9, entity.getAverageLevel());
        }
        statement.bindLong(10, entity.getSampleCount());
        statement.bindString(11, entity.getMeasurementUnit());
        statement.bindDouble(12, entity.getCalibrationOffset());
        statement.bindLong(13, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteRecordingById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM recordings WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfRenameRecording = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recordings SET title = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfCompleteRecording = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE recordings SET status = ?, endTimeEpochMillis = ?, durationMillis = ?, minimumLevel = ?, maximumLevel = ?, averageLevel = ?, sampleCount = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecording(final RecordingEntity recording,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRecordingEntity.insertAndReturnId(recording);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecording(final RecordingEntity recording,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfRecordingEntity.handle(recording);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRecording(final RecordingEntity recording,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfRecordingEntity.handle(recording);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecordingById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRecordingById.acquire();
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
          __preparedStmtOfDeleteRecordingById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object renameRecording(final long id, final String newTitle,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRenameRecording.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, newTitle);
        _argIndex = 2;
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
          __preparedStmtOfRenameRecording.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object completeRecording(final long id, final RecordingStatus status, final long endTime,
      final long duration, final Float minLevel, final Float maxLevel, final Float avgLevel,
      final long sampleCount, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCompleteRecording.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __RecordingStatus_enumToString(status));
        _argIndex = 2;
        _stmt.bindLong(_argIndex, endTime);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, duration);
        _argIndex = 4;
        if (minLevel == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindDouble(_argIndex, minLevel);
        }
        _argIndex = 5;
        if (maxLevel == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindDouble(_argIndex, maxLevel);
        }
        _argIndex = 6;
        if (avgLevel == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindDouble(_argIndex, avgLevel);
        }
        _argIndex = 7;
        _stmt.bindLong(_argIndex, sampleCount);
        _argIndex = 8;
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
          __preparedStmtOfCompleteRecording.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<RecordingEntity>> getAllRecordingsFlow() {
    final String _sql = "SELECT * FROM recordings ORDER BY startTimeEpochMillis DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recordings"}, new Callable<List<RecordingEntity>>() {
      @Override
      @NonNull
      public List<RecordingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeEpochMillis");
          final int _cursorIndexOfEndTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeEpochMillis");
          final int _cursorIndexOfDurationMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMillis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMinimumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumLevel");
          final int _cursorIndexOfMaximumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "maximumLevel");
          final int _cursorIndexOfAverageLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "averageLevel");
          final int _cursorIndexOfSampleCount = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleCount");
          final int _cursorIndexOfMeasurementUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "measurementUnit");
          final int _cursorIndexOfCalibrationOffset = CursorUtil.getColumnIndexOrThrow(_cursor, "calibrationOffset");
          final List<RecordingEntity> _result = new ArrayList<RecordingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordingEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTimeEpochMillis;
            _tmpStartTimeEpochMillis = _cursor.getLong(_cursorIndexOfStartTimeEpochMillis);
            final Long _tmpEndTimeEpochMillis;
            if (_cursor.isNull(_cursorIndexOfEndTimeEpochMillis)) {
              _tmpEndTimeEpochMillis = null;
            } else {
              _tmpEndTimeEpochMillis = _cursor.getLong(_cursorIndexOfEndTimeEpochMillis);
            }
            final long _tmpDurationMillis;
            _tmpDurationMillis = _cursor.getLong(_cursorIndexOfDurationMillis);
            final RecordingStatus _tmpStatus;
            _tmpStatus = __RecordingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Float _tmpMinimumLevel;
            if (_cursor.isNull(_cursorIndexOfMinimumLevel)) {
              _tmpMinimumLevel = null;
            } else {
              _tmpMinimumLevel = _cursor.getFloat(_cursorIndexOfMinimumLevel);
            }
            final Float _tmpMaximumLevel;
            if (_cursor.isNull(_cursorIndexOfMaximumLevel)) {
              _tmpMaximumLevel = null;
            } else {
              _tmpMaximumLevel = _cursor.getFloat(_cursorIndexOfMaximumLevel);
            }
            final Float _tmpAverageLevel;
            if (_cursor.isNull(_cursorIndexOfAverageLevel)) {
              _tmpAverageLevel = null;
            } else {
              _tmpAverageLevel = _cursor.getFloat(_cursorIndexOfAverageLevel);
            }
            final long _tmpSampleCount;
            _tmpSampleCount = _cursor.getLong(_cursorIndexOfSampleCount);
            final String _tmpMeasurementUnit;
            _tmpMeasurementUnit = _cursor.getString(_cursorIndexOfMeasurementUnit);
            final float _tmpCalibrationOffset;
            _tmpCalibrationOffset = _cursor.getFloat(_cursorIndexOfCalibrationOffset);
            _item = new RecordingEntity(_tmpId,_tmpTitle,_tmpStartTimeEpochMillis,_tmpEndTimeEpochMillis,_tmpDurationMillis,_tmpStatus,_tmpMinimumLevel,_tmpMaximumLevel,_tmpAverageLevel,_tmpSampleCount,_tmpMeasurementUnit,_tmpCalibrationOffset);
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
  public Object getRecordingById(final long id,
      final Continuation<? super RecordingEntity> $completion) {
    final String _sql = "SELECT * FROM recordings WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RecordingEntity>() {
      @Override
      @Nullable
      public RecordingEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeEpochMillis");
          final int _cursorIndexOfEndTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeEpochMillis");
          final int _cursorIndexOfDurationMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMillis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMinimumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumLevel");
          final int _cursorIndexOfMaximumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "maximumLevel");
          final int _cursorIndexOfAverageLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "averageLevel");
          final int _cursorIndexOfSampleCount = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleCount");
          final int _cursorIndexOfMeasurementUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "measurementUnit");
          final int _cursorIndexOfCalibrationOffset = CursorUtil.getColumnIndexOrThrow(_cursor, "calibrationOffset");
          final RecordingEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTimeEpochMillis;
            _tmpStartTimeEpochMillis = _cursor.getLong(_cursorIndexOfStartTimeEpochMillis);
            final Long _tmpEndTimeEpochMillis;
            if (_cursor.isNull(_cursorIndexOfEndTimeEpochMillis)) {
              _tmpEndTimeEpochMillis = null;
            } else {
              _tmpEndTimeEpochMillis = _cursor.getLong(_cursorIndexOfEndTimeEpochMillis);
            }
            final long _tmpDurationMillis;
            _tmpDurationMillis = _cursor.getLong(_cursorIndexOfDurationMillis);
            final RecordingStatus _tmpStatus;
            _tmpStatus = __RecordingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Float _tmpMinimumLevel;
            if (_cursor.isNull(_cursorIndexOfMinimumLevel)) {
              _tmpMinimumLevel = null;
            } else {
              _tmpMinimumLevel = _cursor.getFloat(_cursorIndexOfMinimumLevel);
            }
            final Float _tmpMaximumLevel;
            if (_cursor.isNull(_cursorIndexOfMaximumLevel)) {
              _tmpMaximumLevel = null;
            } else {
              _tmpMaximumLevel = _cursor.getFloat(_cursorIndexOfMaximumLevel);
            }
            final Float _tmpAverageLevel;
            if (_cursor.isNull(_cursorIndexOfAverageLevel)) {
              _tmpAverageLevel = null;
            } else {
              _tmpAverageLevel = _cursor.getFloat(_cursorIndexOfAverageLevel);
            }
            final long _tmpSampleCount;
            _tmpSampleCount = _cursor.getLong(_cursorIndexOfSampleCount);
            final String _tmpMeasurementUnit;
            _tmpMeasurementUnit = _cursor.getString(_cursorIndexOfMeasurementUnit);
            final float _tmpCalibrationOffset;
            _tmpCalibrationOffset = _cursor.getFloat(_cursorIndexOfCalibrationOffset);
            _result = new RecordingEntity(_tmpId,_tmpTitle,_tmpStartTimeEpochMillis,_tmpEndTimeEpochMillis,_tmpDurationMillis,_tmpStatus,_tmpMinimumLevel,_tmpMaximumLevel,_tmpAverageLevel,_tmpSampleCount,_tmpMeasurementUnit,_tmpCalibrationOffset);
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
  public Flow<RecordingEntity> getRecordingByIdFlow(final long id) {
    final String _sql = "SELECT * FROM recordings WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"recordings"}, new Callable<RecordingEntity>() {
      @Override
      @Nullable
      public RecordingEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeEpochMillis");
          final int _cursorIndexOfEndTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeEpochMillis");
          final int _cursorIndexOfDurationMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMillis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMinimumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumLevel");
          final int _cursorIndexOfMaximumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "maximumLevel");
          final int _cursorIndexOfAverageLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "averageLevel");
          final int _cursorIndexOfSampleCount = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleCount");
          final int _cursorIndexOfMeasurementUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "measurementUnit");
          final int _cursorIndexOfCalibrationOffset = CursorUtil.getColumnIndexOrThrow(_cursor, "calibrationOffset");
          final RecordingEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTimeEpochMillis;
            _tmpStartTimeEpochMillis = _cursor.getLong(_cursorIndexOfStartTimeEpochMillis);
            final Long _tmpEndTimeEpochMillis;
            if (_cursor.isNull(_cursorIndexOfEndTimeEpochMillis)) {
              _tmpEndTimeEpochMillis = null;
            } else {
              _tmpEndTimeEpochMillis = _cursor.getLong(_cursorIndexOfEndTimeEpochMillis);
            }
            final long _tmpDurationMillis;
            _tmpDurationMillis = _cursor.getLong(_cursorIndexOfDurationMillis);
            final RecordingStatus _tmpStatus;
            _tmpStatus = __RecordingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Float _tmpMinimumLevel;
            if (_cursor.isNull(_cursorIndexOfMinimumLevel)) {
              _tmpMinimumLevel = null;
            } else {
              _tmpMinimumLevel = _cursor.getFloat(_cursorIndexOfMinimumLevel);
            }
            final Float _tmpMaximumLevel;
            if (_cursor.isNull(_cursorIndexOfMaximumLevel)) {
              _tmpMaximumLevel = null;
            } else {
              _tmpMaximumLevel = _cursor.getFloat(_cursorIndexOfMaximumLevel);
            }
            final Float _tmpAverageLevel;
            if (_cursor.isNull(_cursorIndexOfAverageLevel)) {
              _tmpAverageLevel = null;
            } else {
              _tmpAverageLevel = _cursor.getFloat(_cursorIndexOfAverageLevel);
            }
            final long _tmpSampleCount;
            _tmpSampleCount = _cursor.getLong(_cursorIndexOfSampleCount);
            final String _tmpMeasurementUnit;
            _tmpMeasurementUnit = _cursor.getString(_cursorIndexOfMeasurementUnit);
            final float _tmpCalibrationOffset;
            _tmpCalibrationOffset = _cursor.getFloat(_cursorIndexOfCalibrationOffset);
            _result = new RecordingEntity(_tmpId,_tmpTitle,_tmpStartTimeEpochMillis,_tmpEndTimeEpochMillis,_tmpDurationMillis,_tmpStatus,_tmpMinimumLevel,_tmpMaximumLevel,_tmpAverageLevel,_tmpSampleCount,_tmpMeasurementUnit,_tmpCalibrationOffset);
          } else {
            _result = null;
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
  public Object getIncompleteRecordings(
      final Continuation<? super List<RecordingEntity>> $completion) {
    final String _sql = "SELECT * FROM recordings WHERE status = 'RECORDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RecordingEntity>>() {
      @Override
      @NonNull
      public List<RecordingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeEpochMillis");
          final int _cursorIndexOfEndTimeEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeEpochMillis");
          final int _cursorIndexOfDurationMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMillis");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfMinimumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "minimumLevel");
          final int _cursorIndexOfMaximumLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "maximumLevel");
          final int _cursorIndexOfAverageLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "averageLevel");
          final int _cursorIndexOfSampleCount = CursorUtil.getColumnIndexOrThrow(_cursor, "sampleCount");
          final int _cursorIndexOfMeasurementUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "measurementUnit");
          final int _cursorIndexOfCalibrationOffset = CursorUtil.getColumnIndexOrThrow(_cursor, "calibrationOffset");
          final List<RecordingEntity> _result = new ArrayList<RecordingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RecordingEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final long _tmpStartTimeEpochMillis;
            _tmpStartTimeEpochMillis = _cursor.getLong(_cursorIndexOfStartTimeEpochMillis);
            final Long _tmpEndTimeEpochMillis;
            if (_cursor.isNull(_cursorIndexOfEndTimeEpochMillis)) {
              _tmpEndTimeEpochMillis = null;
            } else {
              _tmpEndTimeEpochMillis = _cursor.getLong(_cursorIndexOfEndTimeEpochMillis);
            }
            final long _tmpDurationMillis;
            _tmpDurationMillis = _cursor.getLong(_cursorIndexOfDurationMillis);
            final RecordingStatus _tmpStatus;
            _tmpStatus = __RecordingStatus_stringToEnum(_cursor.getString(_cursorIndexOfStatus));
            final Float _tmpMinimumLevel;
            if (_cursor.isNull(_cursorIndexOfMinimumLevel)) {
              _tmpMinimumLevel = null;
            } else {
              _tmpMinimumLevel = _cursor.getFloat(_cursorIndexOfMinimumLevel);
            }
            final Float _tmpMaximumLevel;
            if (_cursor.isNull(_cursorIndexOfMaximumLevel)) {
              _tmpMaximumLevel = null;
            } else {
              _tmpMaximumLevel = _cursor.getFloat(_cursorIndexOfMaximumLevel);
            }
            final Float _tmpAverageLevel;
            if (_cursor.isNull(_cursorIndexOfAverageLevel)) {
              _tmpAverageLevel = null;
            } else {
              _tmpAverageLevel = _cursor.getFloat(_cursorIndexOfAverageLevel);
            }
            final long _tmpSampleCount;
            _tmpSampleCount = _cursor.getLong(_cursorIndexOfSampleCount);
            final String _tmpMeasurementUnit;
            _tmpMeasurementUnit = _cursor.getString(_cursorIndexOfMeasurementUnit);
            final float _tmpCalibrationOffset;
            _tmpCalibrationOffset = _cursor.getFloat(_cursorIndexOfCalibrationOffset);
            _item = new RecordingEntity(_tmpId,_tmpTitle,_tmpStartTimeEpochMillis,_tmpEndTimeEpochMillis,_tmpDurationMillis,_tmpStatus,_tmpMinimumLevel,_tmpMaximumLevel,_tmpAverageLevel,_tmpSampleCount,_tmpMeasurementUnit,_tmpCalibrationOffset);
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

  private String __RecordingStatus_enumToString(@NonNull final RecordingStatus _value) {
    switch (_value) {
      case RECORDING: return "RECORDING";
      case COMPLETED: return "COMPLETED";
      case RECOVERED: return "RECOVERED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private RecordingStatus __RecordingStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "RECORDING": return RecordingStatus.RECORDING;
      case "COMPLETED": return RecordingStatus.COMPLETED;
      case "RECOVERED": return RecordingStatus.RECOVERED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
