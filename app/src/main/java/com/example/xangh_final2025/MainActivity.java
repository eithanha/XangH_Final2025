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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ActivityAdapter.OnReminderClickListener {
    private static final String TAG = "MainActivity";
    private MySQLiteHelper dbHelper;
    private TabLayout tabLayout;
    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryId = -1;
    private ActivityAdapter activityAdapter;
    private List<Activities> activities = new ArrayList<>();
    private ActivitiesDataAccess activitiesDb;
    private CategoryDataAccess categoryDb;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupRecyclerView();
        setupDatabase();
        setupTabLayout();
        setupAddButton();
    }

    private void setupDatabase() {
        activitiesDb = new ActivitiesDataAccess(this);
        categoryDb = new CategoryDataAccess(this);
        loadCategories();
        loadActivities();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupTabLayout() {
        tabLayout = findViewById(R.id.tabLayout);
        
        // Add "All" tab
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        
        // Add category tabs
        for (Category category : categories) {
            tabLayout.addTab(tabLayout.newTab().setText(category.getName()));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedCategoryId = tab.getPosition() == 0 ? -1 :
                        categories.get(tab.getPosition() - 1).getId();
                filterActivities();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        activityAdapter = new ActivityAdapter(this);
        recyclerView.setAdapter(activityAdapter);
    }

    private void setupAddButton() {
        findViewById(R.id.addButton).setOnClickListener(v -> showAddActivityDialog());
    }

    private void showAddActivityDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_input, null);
        TextInputEditText titleInput = dialogView.findViewById(R.id.titleInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        TextInputEditText dateInput = dialogView.findViewById(R.id.dateInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);

        // Set up category spinner
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("No Category");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        // Set up date picker
        Calendar calendar = Calendar.getInstance();
        dateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        calendar.set(year, month, day);
                        dateInput.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_activity)
                .setView(dialogView)
                .setPositiveButton(R.string.add, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String title = titleInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                String dateStr = dateInput.getText().toString().trim();

                if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int categoryId = categorySpinner.getSelectedItemPosition() == 0 ? 0 :
                        categories.get(categorySpinner.getSelectedItemPosition() - 1).getId();

                Activities newActivity = new Activities(title, description, calendar.getTime(), "Pending", categoryId);
                addActivity(newActivity);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void addActivity(Activities activity) {
        new Thread(() -> {
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
        }).start();
    }

    private void loadCategories() {
        categories = categoryDb.getAllCategories();
    }

    private void loadActivities() {
        activities = activitiesDb.getAllActivities();
        activityAdapter.setActivities(activities);
    }

    private void filterActivities() {
        if (selectedCategoryId == -1) {
            activityAdapter.setActivities(activities);
        } else {
            List<Activities> filteredActivities = new ArrayList<>();
            for (Activities activity : activities) {
                if (activity.getCategoryId() == selectedCategoryId) {
                    filteredActivities.add(activity);
                }
            }
            activityAdapter.setActivities(filteredActivities);
        }
    }

    @Override
    public void onEditClick(Activities activity) {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_input, null);
        TextInputEditText titleInput = dialogView.findViewById(R.id.titleInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        TextInputEditText dateInput = dialogView.findViewById(R.id.dateInput);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);

        
        titleInput.setText(activity.getTitle());
        descriptionInput.setText(activity.getDescription());
        dateInput.setText(dateFormat.format(activity.getDate()));

        
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("No Category");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        
        if (activity.getCategoryId() != 0) {
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getId() == activity.getCategoryId()) {
                    categorySpinner.setSelection(i + 1); 
                    break;
                }
            }
        }

        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(activity.getDate());
        dateInput.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        calendar.set(year, month, day);
                        dateInput.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_activity)
                .setView(dialogView)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String title = titleInput.getText().toString().trim();
                String description = descriptionInput.getText().toString().trim();
                String dateStr = dateInput.getText().toString().trim();

                if (title.isEmpty() || description.isEmpty() || dateStr.isEmpty()) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int categoryId = categorySpinner.getSelectedItemPosition() == 0 ? 0 :
                        categories.get(categorySpinner.getSelectedItemPosition() - 1).getId();

                activity.setTitle(title);
                activity.setDescription(description);
                activity.setDate(calendar.getTime());
                activity.setCategoryId(categoryId);

                updateActivity(activity);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    @Override
    public void onDeleteClick(Activities activity) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_activity)
                .setMessage(R.string.confirm_delete_activity)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    new Thread(() -> {
                        int rowsAffected = activitiesDb.deleteActivity(activity.getId());
                        runOnUiThread(() -> {
                            if (rowsAffected > 0) {
                                activities.remove(activity);
                                activityAdapter.setActivities(activities);
                                Toast.makeText(this, R.string.activity_deleted_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, R.string.error_activity_delete_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateActivity(Activities activity) {
        new Thread(() -> {
            Activities updatedActivity = activitiesDb.updateActivity(activity);
            runOnUiThread(() -> {
                if (updatedActivity != null) {
                    for (int i = 0; i < activities.size(); i++) {
                        if (activities.get(i).getId() == activity.getId()) {
                            activities.set(i, updatedActivity);
                            break;
                        }
                    }
                    activityAdapter.setActivities(activities);
                    Toast.makeText(this, R.string.activity_updated_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.error_activity_update_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activitiesDb != null) {
            activitiesDb.close();
        }
        if (categoryDb != null) {
            categoryDb.close();
        }
    }
}