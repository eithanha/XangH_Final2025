package com.example.xangh_final2025.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xangh_final2025.R;
import com.example.xangh_final2025.models.Activities;
import com.example.xangh_final2025.models.Category;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
    private List<Activities> activities = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final OnReminderClickListener listener;
    private static final String TAG = "ActivityAdapter";

    public interface OnReminderClickListener {
        void onEditClick(Activities activity);
        void onDeleteClick(Activities activity);
    }

    public ActivityAdapter(OnReminderClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<Activities> activities) {
        if (activities == null) {
            Log.d(TAG, "Received null activities list");
            this.activities = new ArrayList<>();
        } else {
            Log.d(TAG, "Activities list updated, new size: " + activities.size());
            this.activities = new ArrayList<>(activities);
        }
        notifyDataSetChanged();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_listview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activities activity = activities.get(position);
        holder.txtTitle.setText(activity.getTitle());
        holder.txtDescription.setText(activity.getDescription());
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(activity.getDate());
        holder.txtDueDate.setText(holder.itemView.getContext().getString(R.string.due_date_set, formattedDate));
        
        // Set category
        String categoryName = holder.itemView.getContext().getString(R.string.no_category);
        if (activity.getCategoryId() > 0 && categories != null) {
            for (Category category : categories) {
                if (category.getId() == activity.getCategoryId()) {
                    categoryName = category.getName();
                    break;
                }
            }
        }
        holder.txtCategory.setText(holder.itemView.getContext().getString(R.string.category_set, categoryName));
        
        // Set status
        String statusText = activity.getStatus();
        holder.txtStatus.setText(holder.itemView.getContext().getString(R.string.status_set, statusText));

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(activity);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtDescription;
        TextView txtDueDate;
        TextView txtCategory;
        TextView txtStatus;
        ImageButton btnEdit;
        ImageButton btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.titleText);
            txtDescription = itemView.findViewById(R.id.descriptionText);
            txtDueDate = itemView.findViewById(R.id.dueDateText);
            txtCategory = itemView.findViewById(R.id.categoryText);
            txtStatus = itemView.findViewById(R.id.statusText);
            btnEdit = itemView.findViewById(R.id.editButton);
            btnDelete = itemView.findViewById(R.id.deleteButton);
        }
    }
}