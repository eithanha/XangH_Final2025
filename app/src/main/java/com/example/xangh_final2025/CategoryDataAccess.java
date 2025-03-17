package com.example.xangh_final2025;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class CategoryDataAccess {



    public static final String TABLE_NAME = "category";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";


    private Context context;
    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;

    public static final String CREATE_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL)",
        TABLE_NAME, COLUMN_ID, COLUMN_NAME
    );


    public CategoryDataAccess(Context context){
        this.context = context;
        this.dbHelper = new MySQLiteHelper(context);
        this.database = this.dbHelper.getWritableDatabase();
    }
}
