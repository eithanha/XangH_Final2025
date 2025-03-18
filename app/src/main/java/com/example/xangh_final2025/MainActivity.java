package com.example.xangh_final2025;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.xangh_final2025.models.Category;
import com.example.xangh_final2025.models.Activities;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

    private void setupDatabase() {
        Log.d(TAG, "Setting up database...");
        // Initialize database access objects
        ActivitiesDataAccess activitiesDb = new ActivitiesDataAccess(this);
        CategoryDataAccess categoryDb = new CategoryDataAccess(this);

        try {
            Log.d(TAG, "Database setup successful");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up database", e);
            e.printStackTrace();
        } finally {
            // Close database connections
            activitiesDb.close();
            categoryDb.close();
            Log.d(TAG, "Database connections closed");
        }
    }
}