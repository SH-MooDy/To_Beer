package com.cookandroid.to_beer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        // 향후 마이그레이션 로직 필요 시 수정
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO);
        onCreate(db);
    }
}
