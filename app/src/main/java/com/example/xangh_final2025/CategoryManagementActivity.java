package com.example.xangh_final2025;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.lang.ref.WeakReference;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xangh_final2025.adapter.CategoryAdapter;
import com.example.xangh_final2025.data_access.CategoryDataAccess;
import com.example.xangh_final2025.database_helper.MySQLiteHelper;
import com.example.xangh_final2025.models.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {
    private CategoryDataAccess categoryDb;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories = new ArrayList<>();

    private MySQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        setupDatabase();
        setupToolbar();
        setupRecyclerView();
        setupFab();
        loadCategories();
    }

    private void setupDatabase() {
        categoryDb = new CategoryDataAccess(this);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.categorytoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.categories_title);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categories, this::onCategoryClick);
        recyclerView.setAdapter(categoryAdapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setContentDescription(getString(R.string.add_category));
        fab.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void loadCategories() {
        categories.clear();
        categories.addAll(categoryDb.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCategoryClick(Category category) {
        String message = String.format(getString(R.string.clicked_category), category.getName());
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        showEditCategoryDialog(category);
    }

    private void showAddCategoryDialog() {
        showCategoryDialog(null, (name) -> {
            if (validateCategoryName(name)) {
                addCategory(name);
            }
        });
    }

    private void showEditCategoryDialog(Category category) {
        showCategoryDialog(category, (name) -> {
            if (validateCategoryName(name)) {
                category.setName(name);
                updateCategory(category);
            }
        });
    }

    private void showCategoryDialog(Category category, CategoryDialogListener listener) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_category, null);
        TextInputEditText categoryNameInput = dialogView.findViewById(R.id.categoryNameInput);
        if (category != null) {
            categoryNameInput.setText(category.getName());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(category == null ? R.string.add_category : R.string.edit_category)
                .setView(dialogView)
                .setPositiveButton(category == null ? R.string.add : R.string.save, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String categoryName = categoryNameInput.getText().toString().trim();
                listener.onCategoryNameEntered(categoryName);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private boolean validateCategoryName(String name) {
        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_category_name_required), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (name.length() > 30) {
            Toast.makeText(this, getString(R.string.error_category_name_too_long), Toast.LENGTH_SHORT).show();
            return false;
        }
        for (Category existingCategory : categories) {
            if (existingCategory.getName().equalsIgnoreCase(name)) {
                Toast.makeText(this, getString(R.string.error_category_exists), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void addCategory(String name) {
        new AddCategoryTask(this, categoryDb, categories, categoryAdapter).execute(name);
    }

    private void updateCategory(Category category) {
        new UpdateCategoryTask(this, categoryDb, categories, categoryAdapter).execute(category);
    }

    private int findCategoryPosition(int categoryId) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == categoryId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (categoryDb != null) {
            categoryDb.close();
        }
    }

    private interface CategoryDialogListener {
        void onCategoryNameEntered(String name);
    }

    private static class AddCategoryTask extends AsyncTask<String, Void, Category> {
        private final WeakReference<CategoryManagementActivity> activityReference;
        private final CategoryDataAccess categoryDb;
        private final List<Category> categories;
        private final CategoryAdapter adapter;
        private String name;

        AddCategoryTask(CategoryManagementActivity activity, CategoryDataAccess categoryDb, 
                       List<Category> categories, CategoryAdapter adapter) {
            this.activityReference = new WeakReference<>(activity);
            this.categoryDb = categoryDb;
            this.categories = categories;
            this.adapter = adapter;
        }

        @Override
        protected Category doInBackground(String... params) {
            name = params[0];
            Category newCategory = new Category();
            newCategory.setName(name);
            return categoryDb.insertCategory(newCategory);
        }

        @Override
        protected void onPostExecute(Category result) {
            CategoryManagementActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (result != null) {
                categories.add(result);
                adapter.notifyItemInserted(categories.size() - 1);
                Toast.makeText(activity,
                        activity.getString(R.string.category_added_success),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity,
                        activity.getString(R.string.error_category_add_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class UpdateCategoryTask extends AsyncTask<Category, Void, Boolean> {
        private final WeakReference<CategoryManagementActivity> activityReference;
        private final CategoryDataAccess categoryDb;
        private final List<Category> categories;
        private final CategoryAdapter adapter;
        private Category category;

        UpdateCategoryTask(CategoryManagementActivity activity, CategoryDataAccess categoryDb,
                         List<Category> categories, CategoryAdapter adapter) {
            this.activityReference = new WeakReference<>(activity);
            this.categoryDb = categoryDb;
            this.categories = categories;
            this.adapter = adapter;
        }

        @Override
        protected Boolean doInBackground(Category... params) {
            try {
                category = params[0];
                categoryDb.updateCategory(category);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            CategoryManagementActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (success) {
                int position = activity.findCategoryPosition(category.getId());
                if (position != -1) {
                    categories.set(position, category);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(activity,
                            activity.getString(R.string.category_updated_success),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity,
                        activity.getString(R.string.error_category_update_failed),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
} 