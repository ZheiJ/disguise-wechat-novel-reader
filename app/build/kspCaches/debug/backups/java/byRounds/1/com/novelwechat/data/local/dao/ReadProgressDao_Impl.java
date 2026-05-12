package com.novelwechat.data.local.dao;

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
import com.novelwechat.data.local.entity.ReadProgress;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReadProgressDao_Impl implements ReadProgressDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ReadProgress> __insertionAdapterOfReadProgress;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProgress;

  public ReadProgressDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfReadProgress = new EntityInsertionAdapter<ReadProgress>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `read_progress` (`bookId`,`chapterIndex`,`sentenceIndex`,`scrollOffset`,`lastReadTime`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ReadProgress entity) {
        statement.bindLong(1, entity.getBookId());
        statement.bindLong(2, entity.getChapterIndex());
        statement.bindLong(3, entity.getSentenceIndex());
        statement.bindLong(4, entity.getScrollOffset());
        statement.bindLong(5, entity.getLastReadTime());
      }
    };
    this.__preparedStmtOfDeleteProgress = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM read_progress WHERE bookId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object saveProgress(final ReadProgress progress,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfReadProgress.insert(progress);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProgress(final long bookId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProgress.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, bookId);
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
          __preparedStmtOfDeleteProgress.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getProgress(final long bookId,
      final Continuation<? super ReadProgress> $completion) {
    final String _sql = "SELECT * FROM read_progress WHERE bookId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, bookId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ReadProgress>() {
      @Override
      @Nullable
      public ReadProgress call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfBookId = CursorUtil.getColumnIndexOrThrow(_cursor, "bookId");
          final int _cursorIndexOfChapterIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "chapterIndex");
          final int _cursorIndexOfSentenceIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "sentenceIndex");
          final int _cursorIndexOfScrollOffset = CursorUtil.getColumnIndexOrThrow(_cursor, "scrollOffset");
          final int _cursorIndexOfLastReadTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastReadTime");
          final ReadProgress _result;
          if (_cursor.moveToFirst()) {
            final long _tmpBookId;
            _tmpBookId = _cursor.getLong(_cursorIndexOfBookId);
            final int _tmpChapterIndex;
            _tmpChapterIndex = _cursor.getInt(_cursorIndexOfChapterIndex);
            final int _tmpSentenceIndex;
            _tmpSentenceIndex = _cursor.getInt(_cursorIndexOfSentenceIndex);
            final int _tmpScrollOffset;
            _tmpScrollOffset = _cursor.getInt(_cursorIndexOfScrollOffset);
            final long _tmpLastReadTime;
            _tmpLastReadTime = _cursor.getLong(_cursorIndexOfLastReadTime);
            _result = new ReadProgress(_tmpBookId,_tmpChapterIndex,_tmpSentenceIndex,_tmpScrollOffset,_tmpLastReadTime);
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
