package com.example.xangh_final2025;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ActivitiesDataAccess {

    public static final String TABLE_NAME = "activities";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date_time";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    //public static final String COLUMN_FOREIGN_KEY = "category_id";
    public static final String CREATE_TABLE = String.format("CREATE TABLE %s (%s id INTEGER PRIMARY KEY AUTOINCREMENT, %s title TEXT NOT NULL, %s description TEXT, %s date_time TEXT, %s status TEXT, %s category_id INTEGER,  FOREIGN KEY (%s) REFERENCES Category(id)",
        TABLE_NAME, COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_STATUS, COLUMN_CATEGORY_ID
    );





    private Context context;
    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;

    public ActivitiesDataAccess(Context context){
        this.context = context;
        this.dbHelper = new MySQLiteHelper(context);
        this.database = this.dbHelper.getWritableDatabase();
    }
}
