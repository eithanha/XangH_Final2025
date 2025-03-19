package com.example.xangh_final2025.data_access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.xangh_final2025.database_helper.MySQLiteHelper;
import com.example.xangh_final2025.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDataAccess {
    private static final String TAG = "CategoryDataAccess";

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

    
    public Category insertCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, category.getName());

        try {
            long id = database.insert(TABLE_NAME, null, values);
            if (id != -1) {
                category.setId((int) id);
                return category;
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error inserting category: " + category.getName(), e);
        }
        return null;
    }

    
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_NAME + 
                      " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_NAME + " ASC";

        try (Cursor c = database.rawQuery(query, null)) {
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    int id = c.getInt(c.getColumnIndexOrThrow(COLUMN_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME));
                    categories.add(new Category(id, name));
                    c.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving all categories", e);
        }

        return categories;
    }

    
    public Category getCategoryById(int id) {
        Category category = null;
        String query = "SELECT " + COLUMN_ID + ", " + COLUMN_NAME + 
                      " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";

        try (Cursor c = database.rawQuery(query, new String[]{String.valueOf(id)})) {
            if (c.moveToFirst()) {
                String name = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME));
                category = new Category(id, name);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving category with ID: " + id, e);
        }

        return category;
    }

    
    public Category updateCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, category.getName());

        try {
            int rowsUpdated = database.update(TABLE_NAME, values, 
                COLUMN_ID + "=?", 
                new String[]{String.valueOf(category.getId())});
            
            if (rowsUpdated > 0) {
                return category;
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating category: " + category.getId(), e);
        }
        return null;
    }

    
    public boolean deleteCategory(int id) {
        try {
            int rowsDeleted = database.delete(TABLE_NAME, COLUMN_ID + "=?", 
                new String[]{String.valueOf(id)});
            return rowsDeleted > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting category: " + id, e);
            return false;
        }
    }

    
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
