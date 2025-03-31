package com.example.xangh_final2025;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ActivityAdapter.OnReminderClickListener {
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
                filterActivities();
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
            startActivity(intent);
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
        Intent intent = new Intent(this, ActivityDetailsActivity.class);
        intent.putExtra(ActivityDetailsActivity.EXTRA_ACTIVITY_ID, activity.getId());
        intent.putExtra(ActivityDetailsActivity.EXTRA_ACTIVITY_TITLE, activity.getTitle());
        intent.putExtra(ActivityDetailsActivity.EXTRA_ACTIVITY_DESCRIPTION, activity.getDescription());
        intent.putExtra(ActivityDetailsActivity.EXTRA_ACTIVITY_DATE, activity.getDate().getTime());
        intent.putExtra(ActivityDetailsActivity.EXTRA_ACTIVITY_CATEGORY_ID, activity.getCategoryId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Activities activity) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_activity)
                .setMessage(R.string.confirm_delete_activity)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    new Thread(() -> {
                        int rowsDeleted = activitiesDb.deleteActivity(activity.getId());
                        runOnUiThread(() -> {
                            if (rowsDeleted > 0) {
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
        if (activitiesDb != null) activitiesDb.close();
        if (categoryDb != null) categoryDb.close();
    }
}