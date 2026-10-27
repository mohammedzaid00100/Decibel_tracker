package com.decibeltracker.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
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
public final class MeasurementSampleDao_Impl implements MeasurementSampleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MeasurementSampleEntity> __insertionAdapterOfMeasurementSampleEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSamplesForRecording;

  public MeasurementSampleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMeasurementSampleEntity = new EntityInsertionAdapter<MeasurementSampleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `measurement_samples` (`id`,`recordingId`,`elapsedMillis`,`timestampEpochMillis`,`levelValue`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MeasurementSampleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRecordingId());
        statement.bindLong(3, entity.getElapsedMillis());
        statement.bindLong(4, entity.getTimestampEpochMillis());
        statement.bindDouble(5, entity.getLevelValue());
      }
    };
    this.__preparedStmtOfDeleteSamplesForRecording = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM measurement_samples WHERE recordingId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBatch(final List<MeasurementSampleEntity> samples,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMeasurementSampleEntity.insert(samples);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSamplesForRecording(final long recordingId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSamplesForRecording.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, recordingId);
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
          __preparedStmtOfDeleteSamplesForRecording.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getSamplesForRecording(final long recordingId,
      final Continuation<? super List<MeasurementSampleEntity>> $completion) {
    final String _sql = "SELECT * FROM measurement_samples WHERE recordingId = ? ORDER BY elapsedMillis ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MeasurementSampleEntity>>() {
      @Override
      @NonNull
      public List<MeasurementSampleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfElapsedMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "elapsedMillis");
          final int _cursorIndexOfTimestampEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampEpochMillis");
          final int _cursorIndexOfLevelValue = CursorUtil.getColumnIndexOrThrow(_cursor, "levelValue");
          final List<MeasurementSampleEntity> _result = new ArrayList<MeasurementSampleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MeasurementSampleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordingId;
            _tmpRecordingId = _cursor.getLong(_cursorIndexOfRecordingId);
            final long _tmpElapsedMillis;
            _tmpElapsedMillis = _cursor.getLong(_cursorIndexOfElapsedMillis);
            final long _tmpTimestampEpochMillis;
            _tmpTimestampEpochMillis = _cursor.getLong(_cursorIndexOfTimestampEpochMillis);
            final float _tmpLevelValue;
            _tmpLevelValue = _cursor.getFloat(_cursorIndexOfLevelValue);
            _item = new MeasurementSampleEntity(_tmpId,_tmpRecordingId,_tmpElapsedMillis,_tmpTimestampEpochMillis,_tmpLevelValue);
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
  public Flow<List<MeasurementSampleEntity>> getSamplesForRecordingFlow(final long recordingId) {
    final String _sql = "SELECT * FROM measurement_samples WHERE recordingId = ? ORDER BY elapsedMillis ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"measurement_samples"}, new Callable<List<MeasurementSampleEntity>>() {
      @Override
      @NonNull
      public List<MeasurementSampleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRecordingId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordingId");
          final int _cursorIndexOfElapsedMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "elapsedMillis");
          final int _cursorIndexOfTimestampEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampEpochMillis");
          final int _cursorIndexOfLevelValue = CursorUtil.getColumnIndexOrThrow(_cursor, "levelValue");
          final List<MeasurementSampleEntity> _result = new ArrayList<MeasurementSampleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MeasurementSampleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRecordingId;
            _tmpRecordingId = _cursor.getLong(_cursorIndexOfRecordingId);
            final long _tmpElapsedMillis;
            _tmpElapsedMillis = _cursor.getLong(_cursorIndexOfElapsedMillis);
            final long _tmpTimestampEpochMillis;
            _tmpTimestampEpochMillis = _cursor.getLong(_cursorIndexOfTimestampEpochMillis);
            final float _tmpLevelValue;
            _tmpLevelValue = _cursor.getFloat(_cursorIndexOfLevelValue);
            _item = new MeasurementSampleEntity(_tmpId,_tmpRecordingId,_tmpElapsedMillis,_tmpTimestampEpochMillis,_tmpLevelValue);
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
  public Object getSampleCount(final long recordingId,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT COUNT(*) FROM measurement_samples WHERE recordingId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final long _tmp;
            _tmp = _cursor.getLong(0);
            _result = _tmp;
          } else {
            _result = 0L;
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
  public Object getAllLevelsForRecording(final long recordingId,
      final Continuation<? super List<Float>> $completion) {
    final String _sql = "SELECT levelValue FROM measurement_samples WHERE recordingId = ? ORDER BY elapsedMillis ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Float>>() {
      @Override
      @NonNull
      public List<Float> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<Float> _result = new ArrayList<Float>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Float _item;
            _item = _cursor.getFloat(0);
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
  public Object getMinMaxStats(final long recordingId,
      final Continuation<? super MinMaxResult> $completion) {
    final String _sql = "SELECT MIN(levelValue) as minLevel, MAX(levelValue) as maxLevel, COUNT(*) as count FROM measurement_samples WHERE recordingId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, recordingId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MinMaxResult>() {
      @Override
      @Nullable
      public MinMaxResult call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMinLevel = 0;
          final int _cursorIndexOfMaxLevel = 1;
          final int _cursorIndexOfCount = 2;
          final MinMaxResult _result;
          if (_cursor.moveToFirst()) {
            final Float _tmpMinLevel;
            if (_cursor.isNull(_cursorIndexOfMinLevel)) {
              _tmpMinLevel = null;
            } else {
              _tmpMinLevel = _cursor.getFloat(_cursorIndexOfMinLevel);
            }
            final Float _tmpMaxLevel;
            if (_cursor.isNull(_cursorIndexOfMaxLevel)) {
              _tmpMaxLevel = null;
            } else {
              _tmpMaxLevel = _cursor.getFloat(_cursorIndexOfMaxLevel);
            }
            final long _tmpCount;
            _tmpCount = _cursor.getLong(_cursorIndexOfCount);
            _result = new MinMaxResult(_tmpMinLevel,_tmpMaxLevel,_tmpCount);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
