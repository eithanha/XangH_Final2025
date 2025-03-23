package com.example.xangh_final2025.adapter;

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

    public interface OnReminderClickListener {
        void onEditClick(Activities activity);
        void onDeleteClick(Activities activity);
    }

    public ActivityAdapter(OnReminderClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<Activities> activities) {
        this.activities = activities;
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
        holder.titleText.setText(activity.getTitle());
        holder.descriptionText.setText(activity.getDescription());

        // Find category name
        String categoryInfo = "";
        if (activity.getCategoryId() != 0) {
            for (Category category : categories) {
                if (category.getId() == activity.getCategoryId()) {
                    categoryInfo = " • " + category.getName();
                    break;
                }
            }
        }

        holder.dueDateText.setText(String.format("Due: %s%s",
                dateFormat.format(activity.getDate()),
                categoryInfo));

        holder.editButton.setOnClickListener(v -> listener.onEditClick(activity));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(activity));
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView dueDateText;
        ImageButton editButton;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}