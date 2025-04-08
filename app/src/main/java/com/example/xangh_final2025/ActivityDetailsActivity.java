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

        // Initialize database access
        activitiesDb = new ActivitiesDataAccess(this);
        categoryDb = new CategoryDataAccess(this);
        categories = categoryDb.getAllCategories();

        // Initialize views
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtDueDate = findViewById(R.id.txtDueDate);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        // Setup category spinner
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("No Category");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }
        spinnerCategory.setAdapter(new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames));

        // Get activity ID from intent
        Intent intent = getIntent();
        long id = intent.getLongExtra(EXTRA_ACTIVITY_ID, 0);
        if (id > 0) {
            activity = activitiesDb.getActivityById((int) id);
            putDataIntoUI();
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            // Initialize new activity with current date
            activity = new Activities();
            activity.setDate(new Date()); // Set current date
            activity.setStatus("Pending");
            
            // Get data from extras if available
            String title = intent.getStringExtra(EXTRA_ACTIVITY_TITLE);
            String description = intent.getStringExtra(EXTRA_ACTIVITY_DESCRIPTION);
            long dateMillis = intent.getLongExtra(EXTRA_ACTIVITY_DATE, 0);
            int categoryId = intent.getIntExtra(EXTRA_ACTIVITY_CATEGORY_ID, 0);
            
            if (title != null) activity.setTitle(title);
            if (description != null) activity.setDescription(description);
            if (dateMillis > 0) activity.setDate(new Date(dateMillis));
            if (categoryId > 0) activity.setCategoryId(categoryId);
            
            putDataIntoUI();
            btnDelete.setVisibility(View.GONE);
        }

        // Setup click listeners
        btnSave.setOnClickListener(view -> save());
        txtDueDate.setOnClickListener(view -> showDatePicker());
        btnDelete.setOnClickListener(view -> showDeleteDialog());
    }

    private void putDataIntoUI() {
        if (activity != null) {
            txtTitle.setText(activity.getTitle());
            txtDescription.setText(activity.getDescription());
            
            // Ensure date is not null before setting calendar
            if (activity.getDate() != null) {
                calendar.setTime(activity.getDate());
                txtDueDate.setText(dateFormat.format(activity.getDate()));
            } else {
                // If date is null, set to current date
                activity.setDate(new Date());
                calendar.setTime(activity.getDate());
                txtDueDate.setText(dateFormat.format(activity.getDate()));
            }
            
            if (activity.getCategoryId() != 0) {
                for (int i = 0; i < categories.size(); i++) {
                    if (categories.get(i).getId() == activity.getCategoryId()) {
                        spinnerCategory.setSelection(i + 1);
                        break;
                    }
                }
            }
        }
    }

    private boolean validate() {
        boolean isValid = true;
        if (txtTitle.getText().toString().isEmpty()) {
            isValid = false;
            txtTitle.setError("You must enter a title");
        }
        if (txtDescription.getText().toString().isEmpty()) {
            isValid = false;
            txtDescription.setError("You must enter a description");
        }
        if (txtDueDate.getText().toString().isEmpty()) {
            isValid = false;
            txtDueDate.setError("You must select a date");
        }
        return isValid;
    }

    private boolean save() {
        try {
            if (validate()) {
                getDataFromUI();
                if (activity == null) {
                    Log.e(TAG, "Activity is null after getDataFromUI");
                    Toast.makeText(this, "Error creating activity", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (activity.getDate() == null) {
                    Log.e(TAG, "Activity date is null");
                    Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show();
                    return false;
                }

                Log.d(TAG, "Saving activity: " + activity.getTitle() + 
                          " with date: " + dateFormat.format(activity.getDate()));

                if (activity.getId() > 0) {
                    Activities updatedActivity = activitiesDb.updateActivity(activity);
                    if (updatedActivity != null) {
                        Log.d(TAG, "Activity updated successfully");
                        startActivity(new Intent(this, MainActivity.class));
                        return true;
                    } else {
                        Log.e(TAG, "Failed to update activity");
                        Toast.makeText(this, "Failed to update activity", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Activities insertedActivity = activitiesDb.insertActivity(activity);
                    if (insertedActivity != null) {
                        Log.d(TAG, "Activity inserted successfully");
                        startActivity(new Intent(this, MainActivity.class));
                        return true;
                    } else {
                        Log.e(TAG, "Failed to insert activity");
                        Toast.makeText(this, "Failed to add activity", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Log.d(TAG, "Validation failed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving activity: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void getDataFromUI() {
        try {
            String title = txtTitle.getText().toString();
            String description = txtDescription.getText().toString();
            String dueDateStr = txtDueDate.getText().toString();
            int categoryId = spinnerCategory.getSelectedItemPosition() == 0 ? 0 :
                    categories.get(spinnerCategory.getSelectedItemPosition() - 1).getId();

            Log.d(TAG, "Parsing date: " + dueDateStr);
            Date date = dateFormat.parse(dueDateStr);
            if (date == null) {
                Log.e(TAG, "Failed to parse date: " + dueDateStr);
                return;
            }

            if (activity != null) {
                activity.setTitle(title);
                activity.setDescription(description);
                activity.setDate(date);
                activity.setCategoryId(categoryId);
            } else {
                activity = new Activities(title, description, date, "Pending", categoryId);
            }
            Log.d(TAG, "Activity data retrieved successfully");
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage(), e);
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error getting data from UI: " + e.getMessage(), e);
            Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        DatePickerDialog dp = new DatePickerDialog(this,
                (datePicker, year, month, day) -> {
                    calendar.set(year, month, day);
                    txtDueDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        dp.show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_activity)
                .setMessage(R.string.confirm_delete_activity)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    activitiesDb.deleteActivity(activity.getId());
                    startActivity(new Intent(this, MainActivity.class));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activitiesDb != null) activitiesDb.close();
        if (categoryDb != null) categoryDb.close();
    }
} 