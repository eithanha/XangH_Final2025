package com.example.xangh_final2025.data_access;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.xangh_final2025.database_helper.MySQLiteHelper;
import com.example.xangh_final2025.models.Activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivitiesDataAccess {
    private static final String TAG = "ActivitiesDataAccess";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    public static final String TABLE_NAME = "activities";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    
    public static final String CREATE_TABLE = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT, %s TEXT, %s TEXT, %s INTEGER, FOREIGN KEY (%s) REFERENCES Category(id))",
        TABLE_NAME, COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_STATUS, COLUMN_CATEGORY_ID, COLUMN_CATEGORY_ID
    );

    private Context context;
    private MySQLiteHelper dbHelper;
    private SQLiteDatabase database;

    public ActivitiesDataAccess(Context context){
        this.context = context;
        this.dbHelper = new MySQLiteHelper(context);
        this.database = this.dbHelper.getWritableDatabase();
    }

    public Activities insertActivity(Activities activity) {
        if (activity == null) {
            Log.e(TAG, "Attempted to insert null activity");
            return null;
        }

        if (activity.getDate() == null) {
            Log.e(TAG, "Activity date is null for: " + activity.getTitle());
            return null;
        }

        SQLiteDatabase db = null;
        try {
            // Get a new database instance
            db = dbHelper.getWritableDatabase();
            
            // Begin transaction
            db.beginTransaction();
            
            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TITLE, activity.getTitle());
                values.put(COLUMN_DESCRIPTION, activity.getDescription());
                values.put(COLUMN_DATE, dateFormat.format(activity.getDate()));
                values.put(COLUMN_STATUS, activity.getStatus());
                values.put(COLUMN_CATEGORY_ID, activity.getCategoryId());

                Log.d(TAG, "Attempting to insert activity: " + activity.toString());

                // Insert the activity
                long id = db.insert(TABLE_NAME, null, values);
                Log.d(TAG, "Database insert returned ID: " + id);

                if (id > 0) {
                    // Query the inserted activity to verify
                    String[] columns = {COLUMN_ID, COLUMN_TITLE, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_STATUS, COLUMN_CATEGORY_ID};
                    String selection = COLUMN_ID + " = ?";
                    String[] selectionArgs = {String.valueOf(id)};
                    
                    Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
                    
                    if (cursor != null && cursor.moveToFirst()) {
                        Activities insertedActivity = new Activities(
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                            dateFormat.parse(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
                        );
                        cursor.close();
                        
                        // Mark transaction as successful
                        db.setTransactionSuccessful();
                        Log.d(TAG, "Successfully inserted and verified activity with ID: " + insertedActivity.getId());
                        return insertedActivity;
                    } else {
                        Log.e(TAG, "Failed to verify inserted activity with ID: " + id);
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to insert activity - insert returned invalid ID: " + id);
                }
            } finally {
                // End transaction
                if (db != null) {
                    db.endTransaction();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inserting activity: " + e.getMessage(), e);
        }
        return null;
    }

    
    public List<Activities> getAllActivities() {
        List<Activities> activities = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DATE + " DESC";

        try (Cursor c = database.rawQuery(query, null)) {
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    int id = c.getInt(c.getColumnIndexOrThrow(COLUMN_ID));
                    String title = c.getString(c.getColumnIndexOrThrow(COLUMN_TITLE));
                    String description = c.getString(c.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                    String dateStr = c.getString(c.getColumnIndexOrThrow(COLUMN_DATE));
                    String status = c.getString(c.getColumnIndexOrThrow(COLUMN_STATUS));
                    int categoryId = c.getInt(c.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));

                    Date date = null;
                    try {
                        date = dateFormat.parse(dateStr);
                    } catch (ParseException e) {
                        Log.e(TAG, "Error parsing date: " + dateStr, e);
                    }

                    Activities activity = new Activities(id, title, description, date, status, categoryId);
                    activities.add(activity);
                    c.moveToNext();
                }
                c.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving all activities", e);
        }

        return activities;
    }

    
    public Activities getActivityById(int id) {
        Activities activity = null;
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=" + id;

        try (Cursor cursor = database.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID));

                Date date = null;
                try {
                    date = dateFormat.parse(dateStr);
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing date: " + dateStr, e);
                }

                activity = new Activities(id, title, description, date, status, categoryId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving activity with ID: " + id, e);
        }

        return activity; 
    }

    
    public Activities updateActivity(Activities activity) {
        if (activity == null || activity.getId() <= 0) {
            Log.e(TAG, "Invalid activity or activity ID for update");
            return null;
        }

        // First verify the activity exists
        Activities existingActivity = getActivityById(activity.getId());
        if (existingActivity == null) {
            Log.e(TAG, "Activity not found in database: " + activity.getId());
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, activity.getTitle());
        values.put(COLUMN_DESCRIPTION, activity.getDescription());
        values.put(COLUMN_DATE, dateFormat.format(activity.getDate()));
        values.put(COLUMN_STATUS, activity.getStatus());
        values.put(COLUMN_CATEGORY_ID, activity.getCategoryId());

        try {
            String whereClause = COLUMN_ID + "=?";
            String[] whereArgs = new String[]{String.valueOf(activity.getId())};
            
            // Use a transaction to ensure atomicity
            database.beginTransaction();
            try {
                int rowsUpdated = database.update(TABLE_NAME, values, whereClause, whereArgs);
                
                if (rowsUpdated > 0) {
                    database.setTransactionSuccessful();
                    Log.d(TAG, "Activity updated successfully: " + activity.getId());
                    return activity;
                } else {
                    Log.e(TAG, "No rows updated for activity: " + activity.getId());
                }
            } finally {
                database.endTransaction();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating activity: " + activity.getId(), e);
        }
        return null;
    }

    
    public int deleteActivity(int id) {
        try {
            return database.delete(TABLE_NAME, COLUMN_ID + "=" + id, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return -1;
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
