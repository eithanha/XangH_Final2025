package com.example.xangh_final2025;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xangh_final2025.data_access.ActivitiesDataAccess;
import com.example.xangh_final2025.data_access.CategoryDataAccess;
import com.example.xangh_final2025.models.Activities;
import com.example.xangh_final2025.models.Category;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.widget.ArrayAdapter;

public class ActivityDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_ACTIVITY_ID = "activity_id";
    public static final String EXTRA_ACTIVITY_TITLE = "activity_title";
    public static final String EXTRA_ACTIVITY_DESCRIPTION = "activity_description";
    public static final String EXTRA_ACTIVITY_DATE = "activity_date";
    public static final String EXTRA_ACTIVITY_CATEGORY_ID = "activity_category_id";
    private static final String TAG = "ActivityDetailsActivity";

    private ActivitiesDataAccess activitiesDb;
    private CategoryDataAccess categoryDb;
    private Activities activity;
    private List<Category> categories = new ArrayList<>();

    private EditText txtTitle;
    private EditText txtDescription;
    private EditText txtDueDate;
    private Spinner spinnerCategory;
    private Button btnSave;
    private Button btnDelete;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Initialize UI components
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtDueDate = findViewById(R.id.txtDueDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // Initialize database access
        activitiesDb = new ActivitiesDataAccess(this);
        categoryDb = new CategoryDataAccess(this);

        // Get activity ID from intent
        int activityId = getIntent().getIntExtra(EXTRA_ACTIVITY_ID, 0);
        Log.d(TAG, "Received activity ID: " + activityId);

        // Load categories for spinner
        categories = categoryDb.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("No Category");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        // If editing existing activity, load its data
        if (activityId > 0) {
            activity = activitiesDb.getActivityById(activityId);
            if (activity != null) {
                txtTitle.setText(activity.getTitle());
                txtDescription.setText(activity.getDescription());
                txtDueDate.setText(dateFormat.format(activity.getDate()));
                
                // Set category spinner
                if (activity.getCategoryId() > 0) {
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getId() == activity.getCategoryId()) {
                            spinnerCategory.setSelection(i + 1);
                            break;
                        }
                    }
                }
                btnDelete.setVisibility(View.VISIBLE);
            }
        } else {
            // New activity
            activity = new Activities();
            btnDelete.setVisibility(View.GONE);
        }

        // Set up date picker
        txtDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (activity != null && activity.getDate() != null) {
                calendar.setTime(activity.getDate());
            }

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        calendar.set(year, month, day);
                        txtDueDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        // Set up save button
        btnSave.setOnClickListener(view -> {
            try {
                if (validate()) {
                    // Get data from UI
                    String title = txtTitle.getText().toString();
                    String description = txtDescription.getText().toString();
                    String dueDateStr = txtDueDate.getText().toString();
                    int categoryId = spinnerCategory.getSelectedItemPosition() == 0 ? 0 :
                            categories.get(spinnerCategory.getSelectedItemPosition() - 1).getId();

                    // Parse date
                    Date date = dateFormat.parse(dueDateStr);
                    if (date == null) {
                        Log.e(TAG, "Failed to parse date: " + dueDateStr);
                        Toast.makeText(this, getString(R.string.error_invalid_date), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update the activity object
                    activity.setTitle(title);
                    activity.setDescription(description);
                    activity.setDate(date);
                    activity.setCategoryId(categoryId);
                    activity.setStatus("Pending");

                    Log.d(TAG, "Saving activity: " + activity.getTitle() + 
                              " with ID: " + activity.getId() +
                              " and date: " + dateFormat.format(activity.getDate()));

                    Activities savedActivity;
                    if (activity.getId() > 0) {
                        // Update existing activity
                        savedActivity = activitiesDb.updateActivity(activity);
                        Log.d(TAG, "Updating existing activity with ID: " + activity.getId());
                    } else {
                        // Insert new activity
                        savedActivity = activitiesDb.insertActivity(activity);
                        Log.d(TAG, "Inserting new activity");
                    }

                    if (savedActivity != null && savedActivity.getId() > 0) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra(EXTRA_ACTIVITY_ID, savedActivity.getId());
                        setResult(RESULT_OK, resultIntent);
                        Log.d(TAG, "Setting result with activity ID: " + savedActivity.getId());
                        finish();
                    } else {
                        Log.e(TAG, "Failed to save activity");
                        Toast.makeText(this, getString(R.string.error_save_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving activity: " + e.getMessage(), e);
                Toast.makeText(this, getString(R.string.error_save_error, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up delete button
        setupDeleteButton();
    }

    private void setupDeleteButton() {
        Button deleteButton = findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(v -> {
            if (activity != null) {
                new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_delete_title))
                    .setMessage(getString(R.string.confirm_delete_message))
                    .setPositiveButton(getString(R.string.confirm_delete_positive), (dialog, which) -> {
                        new Thread(() -> {
                            try {
                                int rowsDeleted = activitiesDb.deleteActivity(activity.getId());
                                runOnUiThread(() -> {
                                    if (rowsDeleted > 0) {
                                        Log.d(TAG, "Activity deleted successfully");
                                        Intent resultIntent = new Intent();
                                        resultIntent.putExtra("activity_deleted", true);
                                        setResult(RESULT_OK, resultIntent);
                                        finish();
                                    } else {
                                        Log.e(TAG, "Failed to delete activity");
                                        Toast.makeText(this, getString(R.string.error_deleting_activity), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Error deleting activity: " + e.getMessage(), e);
                                runOnUiThread(() -> 
                                    Toast.makeText(this, getString(R.string.error_deleting_activity), Toast.LENGTH_SHORT).show()
                                );
                            }
                        }).start();
                    })
                    .setNegativeButton(getString(R.string.confirm_delete_negative), null)
                    .show();
            }
        });
    }

    private boolean validate() {
        boolean isValid = true;
        if (txtTitle.getText().toString().isEmpty()) {
            isValid = false;
            txtTitle.setError(getString(R.string.error_title_required));
        }
        if (txtDescription.getText().toString().isEmpty()) {
            isValid = false;
            txtDescription.setError(getString(R.string.error_description_required));
        }
        if (txtDueDate.getText().toString().isEmpty()) {
            isValid = false;
            txtDueDate.setError(getString(R.string.error_date_required));
        }
        return isValid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activitiesDb != null) activitiesDb.close();
        if (categoryDb != null) categoryDb.close();
    }
} 