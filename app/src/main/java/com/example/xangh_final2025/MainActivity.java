package com.example.xangh_final2025;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xangh_final2025.adapter.ActivityAdapter;
import com.example.xangh_final2025.data_access.ActivitiesDataAccess;
import com.example.xangh_final2025.data_access.CategoryDataAccess;
import com.example.xangh_final2025.database_helper.MySQLiteHelper;
import com.example.xangh_final2025.models.Activities;
import com.example.xangh_final2025.models.Category;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ActivityAdapter.OnReminderClickListener {

    public static final String TAG = "MainActivity";
    private MySQLiteHelper dbHelper;
    private TabLayout tabLayout;
    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryId = -1;
    private ActivityAdapter activityAdapter;
    private List<Activities> activities = new ArrayList<>();
    private ActivitiesDataAccess activitiesDb;
    private CategoryDataAccess categoryDb;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupRecyclerView();
        setupDatabase();
        setupTabLayout();
        setupAddButton();
    }

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityAdapter = new ActivityAdapter(this);
        recyclerView.setAdapter(activityAdapter);
    }

    private void setupDatabase() {
        activitiesDb = new ActivitiesDataAccess(this);
        categoryDb = new CategoryDataAccess(this);
        categories = categoryDb.getAllCategories();
        activities = activitiesDb.getAllActivities();
        activityAdapter.setCategories(categories);
        activityAdapter.setActivities(activities);
    }

    private void setupTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        for (Category category : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(category.getName()));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedCategoryId = tab.getPosition() == 0 ? -1 : categories.get(tab.getPosition() - 1).getId();
                filterActivities(selectedCategoryId);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupAddButton() {
        findViewById(R.id.addButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, ActivityDetailsActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void showActivityDialog(Activities activity) {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_details, null);
        EditText titleInput = dialogView.findViewById(R.id.txtTitle);
        EditText descriptionInput = dialogView.findViewById(R.id.txtDescription);
        EditText dateInput = dialogView.findViewById(R.id.txtDueDate);
        Spinner categorySpinner = dialogView.findViewById(R.id.spinnerCategory);
        Button saveButton = dialogView.findViewById(R.id.btnSave);
        Button deleteButton = dialogView.findViewById(R.id.btnDelete);


        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("No Category");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);


        Calendar calendar = Calendar.getInstance();
        if (activity != null) {
            titleInput.setText(activity.getTitle());
            descriptionInput.setText(activity.getDescription());
            calendar.setTime(activity.getDate());
            dateInput.setText(dateFormat.format(activity.getDate()));
            if (activity.getCategoryId() != 0) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId() == activity.getCategoryId()) {
                        categorySpinner.setSelection(i + 1);
                        break;
                    }
                }
            }
            deleteButton.setVisibility(View.VISIBLE);
        }

        dateInput.setOnClickListener(v -> {
            DatePickerDialog dp = new DatePickerDialog(this, (datePicker, y, m, d) -> {
                calendar.set(y, m, d);
                dateInput.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            dp.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(activity == null ? R.string.add_activity : R.string.edit_activity)
                .setView(dialogView)
                .create();

        saveButton.setOnClickListener(view -> {
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String dateStr = dateInput.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int categoryId = categorySpinner.getSelectedItemPosition() == 0 ? 0 :
                    categories.get(categorySpinner.getSelectedItemPosition() - 1).getId();

            if (activity == null) {
                Activities newActivity = new Activities(title, description, calendar.getTime(), "Pending", categoryId);
                addActivity(newActivity);
            } else {
                activity.setTitle(title);
                activity.setDescription(description);
                activity.setDate(calendar.getTime());
                activity.setCategoryId(categoryId);
                updateActivity(activity);
            }
            dialog.dismiss();
        });

        if (activity != null) {
            deleteButton.setOnClickListener(view -> {
                onDeleteClick(activity);
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void addActivity(Activities activity) {
        if (activity == null || activity.getDate() == null) {
            Toast.makeText(this, "Invalid activity data", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Activities savedActivity = activitiesDb.insertActivity(activity);
                runOnUiThread(() -> {
                    if (savedActivity != null) {
                        activities.add(savedActivity);
                        activityAdapter.setActivities(activities);
                        Toast.makeText(this, R.string.activity_added_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.error_activity_add_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error adding activity", e);
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error adding activity: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void filterActivities(int categoryId) {
        if (categoryId == -1) {
            // Show all activities
            activityAdapter.setActivities(activities);
            Log.d(TAG, "Showing all activities, count: " + activities.size());
        } else {
            // Filter activities by category
            List<Activities> filteredActivities = new ArrayList<>();
            for (Activities activity : activities) {
                if (activity.getCategoryId() == categoryId) {
                    filteredActivities.add(activity);
                }
            }
            activityAdapter.setActivities(filteredActivities);
            Log.d(TAG, "Filtered activities by category " + categoryId + 
                  ", count: " + filteredActivities.size());
        }
    }

    @Override
    public void onEditClick(Activities activity) {
        Intent intent = new Intent(MainActivity.this, ActivityDetailsActivity.class);
        intent.putExtra(ActivityDetailsActivity.EXTRA_ACTIVITY_ID, activity.getId());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onDeleteClick(Activities activity) {
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this activity?")
            .setPositiveButton("Delete", (dialog, which) -> {
                new Thread(() -> {
                    try {
                        int rowsDeleted = activitiesDb.deleteActivity(activity.getId());
                        runOnUiThread(() -> {
                            if (rowsDeleted > 0) {
                                activities.remove(activity);
                                activityAdapter.setActivities(activities);
                                Toast.makeText(MainActivity.this, "Activity deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to delete activity", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error deleting activity", e);
                        runOnUiThread(() -> 
                            Toast.makeText(MainActivity.this, "Error deleting activity: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }).start();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onStatusChange(Activities activity, boolean isCompleted) {
        new Thread(() -> {
            try {
                activity.setStatus(isCompleted ? "Completed" : "Pending");
                Activities updatedActivity = activitiesDb.updateActivity(activity);
                if (updatedActivity != null) {
                    runOnUiThread(() -> {
                        // Update the activity in the list
                        int index = activities.indexOf(activity);
                        if (index != -1) {
                            activities.set(index, updatedActivity);
                            activityAdapter.setActivities(activities);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating activity status", e);
                runOnUiThread(() -> 
                    Toast.makeText(MainActivity.this, "Error updating status: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void updateActivity(Activities activity) {
        new Thread(() -> {
            try {
                Activities updatedActivity = activitiesDb.updateActivity(activity);
                runOnUiThread(() -> {
                    if (updatedActivity != null) {
                        // Find and update the activity in the list
                        for (int i = 0; i < activities.size(); i++) {
                            if (activities.get(i).getId() == activity.getId()) {
                                activities.set(i, updatedActivity);
                                break;
                            }
                        }
                        activityAdapter.setActivities(activities);
                        filterActivities(selectedCategoryId); // Reapply the current filter
                        Toast.makeText(this, R.string.activity_updated_success, Toast.LENGTH_SHORT).show();
                    } else {
                        // If update failed, refresh the entire list from database
                        activities = activitiesDb.getAllActivities();
                        activityAdapter.setActivities(activities);
                        filterActivities(selectedCategoryId); // Reapply the current filter
                        Toast.makeText(this, R.string.error_activity_update_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating activity", e);
                runOnUiThread(() -> 
                    Toast.makeText(this, "Error updating activity: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Load Menu onto screen
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Controls menu toolbar - opens CategoryManagement and navigates to categories page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_manage_categories) {
            Intent intent = new Intent(this, CategoryManagementActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.menu_exit) {
            finish();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            boolean activityDeleted = data.getBooleanExtra("activity_deleted", false);
            if (activityDeleted) {
                Log.d(TAG, "Activity was deleted, refreshing list");
                new Thread(() -> {
                    try {
                        List<Activities> updatedActivities = activitiesDb.getAllActivities();
                        Log.d(TAG, "Retrieved " + updatedActivities.size() + " activities from database");
                        
                        Collections.sort(updatedActivities, (a1, a2) -> a1.getDate().compareTo(a2.getDate()));
                        
                        List<Category> updatedCategories = categoryDb.getAllCategories();
                        Log.d(TAG, "Retrieved " + updatedCategories.size() + " categories from database");
                        
                        runOnUiThread(() -> {
                            activities = updatedActivities;
                            categories = updatedCategories;
                            activityAdapter.setCategories(categories);
                            activityAdapter.setActivities(activities);
                            filterActivities(selectedCategoryId);
                            Toast.makeText(this, getString(R.string.activity_deleted_success), Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error refreshing activities: " + e.getMessage(), e);
                        runOnUiThread(() -> 
                            Toast.makeText(this, getString(R.string.error_refreshing_activities), Toast.LENGTH_SHORT).show()
                        );
                    }
                }).start();
                return;
            }

            int activityId = data.getIntExtra("activity_id", -1);
            Log.d(TAG, "Received updated activity ID: " + activityId);

            if (activityId > 0) {
                new Thread(() -> {
                    try {
                        Log.d(TAG, "Starting to refresh activities and categories");
                        List<Activities> updatedActivities = activitiesDb.getAllActivities();
                        Log.d(TAG, "Retrieved " + updatedActivities.size() + " activities from database");
                        
                        Collections.sort(updatedActivities, (a1, a2) -> a1.getDate().compareTo(a2.getDate()));
                        
                        List<Category> updatedCategories = categoryDb.getAllCategories();
                        Log.d(TAG, "Retrieved " + updatedCategories.size() + " categories from database");
                        
                        runOnUiThread(() -> {
                            Log.d(TAG, "Updating UI with new data");
                            activities = updatedActivities;
                            categories = updatedCategories;
                            activityAdapter.setCategories(categories);
                            activityAdapter.setActivities(activities);
                            filterActivities(selectedCategoryId);
                            Log.d(TAG, "UI update complete");
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error refreshing activities: " + e.getMessage(), e);
                        runOnUiThread(() -> 
                            Toast.makeText(this, getString(R.string.error_refreshing_activities), Toast.LENGTH_SHORT).show()
                        );
                    }
                }).start();
            } else {
                Log.e(TAG, getString(R.string.error_invalid_activity_id));
            }
        } else {
            Log.d(TAG, getString(R.string.error_activity_result));
        }
    }

    // Close Database
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activitiesDb != null) activitiesDb.close();
        if (categoryDb != null) categoryDb.close();
    }
}