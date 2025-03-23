package com.example.xangh_final2025.database_helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.xangh_final2025.data_access.ActivitiesDataAccess;
import com.example.xangh_final2025.data_access.CategoryDataAccess;

public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TAG = "MySQLiteHelper";
    private static final String DATA_BASE_NAME = "activities_tracker.sqlite";
    private static final int DATA_BASE_VERSION = 1;

    public MySQLiteHelper(Context context){
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String activitiesDb = ActivitiesDataAccess.CREATE_TABLE;
        String categoryDb = CategoryDataAccess.CREATE_TABLE;
        Log.d(TAG, "Creating DataBase");
        db.execSQL(activitiesDb);
        db.execSQL(categoryDb);
        Log.d(TAG, ActivitiesDataAccess.CREATE_TABLE);
        Log.d(TAG, CategoryDataAccess.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.d(TAG,"Data Base Updated To Version: " + DATA_BASE_VERSION);
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesDataAccess.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryDataAccess.TABLE_NAME);
    }

    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CategoryDataAccess.TABLE_NAME, CategoryDataAccess.COLUMN_ID +  " = ?",
                new String[]{String.valueOf(categoryId)}) > 0;
    }

    public void deleteDatabase(Context context) {
        context.deleteDatabase(DATA_BASE_NAME);
    }
}
