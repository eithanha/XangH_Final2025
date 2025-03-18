package com.example.xangh_final2025;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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
        // Initialize database access objects
        ActivitiesDataAccess activitiesDb = new ActivitiesDataAccess(this);
        CategoryDataAccess categoryDb = new CategoryDataAccess(this);

        // Test database connection
        try {
            // Try to get all categories to verify database is working
            categoryDb.getAllCategories();
            // Try to get all activities to verify database is working
            activitiesDb.getAllActivities();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close database connections
            activitiesDb.close();
            categoryDb.close();
        }
    }
}