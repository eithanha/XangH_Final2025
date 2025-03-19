package com.example.xangh_final2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.xangh_final2025.data_access.ActivitiesDataAccess;
import com.example.xangh_final2025.data_access.CategoryDataAccess;
import com.example.xangh_final2025.database_helper.MySQLiteHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MySQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        dbHelper = new MySQLiteHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the main view
        View mainView = findViewById(R.id.main);
        
        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the database
        setupDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_manage_categories) {
            startActivity(new Intent(this, CategoryManagementActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDatabase() {
        Log.d(TAG, "Setting up database...");
        
        ActivitiesDataAccess activitiesDb = new ActivitiesDataAccess(this);
        CategoryDataAccess categoryDb = new CategoryDataAccess(this);

        try {
            Log.d(TAG, "Database setup successful");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up database", e);
            e.printStackTrace();
        } finally {     
            activitiesDb.close();
            categoryDb.close();
            Log.d(TAG, "Database connections closed");
        }
    }
}