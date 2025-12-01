package com.cookandroid.to_beer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cookandroid.to_beer.model.TodoItem;

import java.util.ArrayList;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "todo.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_TODO = "TodoItem";
    public static final String COL_ID = "id";
    public static final String COL_DATE = "date";
    public static final String COL_TITLE = "title";
    public static final String COL_IS_COMPLETE = "is_complete";
    public static final String COL_WEIGHT = "weight";

    public TodoDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable =
                "CREATE TABLE " + TABLE_TODO + " (" +
                        COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_DATE + " TEXT, " +
                        COL_TITLE + " TEXT, " +
                        COL_IS_COMPLETE + " INTEGER DEFAULT 0, " +
                        COL_WEIGHT + " INTEGER DEFAULT 1" +
                        ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }

    // Todo 추가
    public long insertTodo(String date, String title, int weight) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_TITLE, title);
        values.put(COL_IS_COMPLETE, 0);
        values.put(COL_WEIGHT, weight);
        return db.insert(TABLE_TODO, null, values);
    }

    // 특정 날짜의 Todo 목록 조회
    public ArrayList<TodoItem> getTodosByDate(String date) {
        ArrayList<TodoItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TODO,
                null,
                COL_DATE + "=?",
                new String[]{date},
                null,
                null,
                COL_ID + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String d = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE));
                int isComplete = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_COMPLETE));
                int weight = cursor.getInt(cursor.getColumnIndexOrThrow(COL_WEIGHT));

                list.add(new TodoItem(id, d, title, isComplete, weight));
            }
            cursor.close();
        }
        return list;
    }

    // 완료 여부 업데이트
    public void updateTodoComplete(int id, boolean isComplete) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_COMPLETE, isComplete ? 1 : 0);
        db.update(TABLE_TODO, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // (선택) 삭제
    public void deleteTodo(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_TODO, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // 오늘 전체 weight 합계
    public int getTotalWeightForDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        int total = 0;

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_WEIGHT + ") FROM " + TABLE_TODO +
                        " WHERE " + COL_DATE + "=?",
                new String[]{date}
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
            cursor.close();
        }
        return total;
    }

    // 오늘 완료된 weight 합계
    public int getDoneWeightForDate(String date) {
        SQLiteDatabase db = getReadableDatabase();
        int done = 0;

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_WEIGHT + ") FROM " + TABLE_TODO +
                        " WHERE " + COL_DATE + "=? AND " + COL_IS_COMPLETE + "=1",
                new String[]{date}
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                done = cursor.getInt(0);
            }
            cursor.close();
        }
        return done;
    }
}
