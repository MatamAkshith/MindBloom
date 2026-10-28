package com.example.MindBloom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * GoalAdapter custom ArrayAdapter to bind Goal items into the ListView.
 * Follows the ListView and ArrayAdapter concepts demonstrated in Week 5(b) of the Lab Manual.
 */
public class GoalAdapter extends ArrayAdapter<Goal> {

    public interface OnGoalActionListener {
        void onView(Goal goal);
        void onUpdateProgress(Goal goal);
        void onEdit(Goal goal);
        void onDelete(Goal goal);
    }

    private final Context context;
    private final List<Goal> goals;
    private final OnGoalActionListener listener;

    public GoalAdapter(@NonNull Context context, @NonNull List<Goal> goals, OnGoalActionListener listener) {
        super(context, R.layout.item_goal, goals);
        this.context = context;
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.item_goal, parent, false);
        }

        Goal goal = goals.get(position);

        View cardGoalItem = rowView.findViewById(R.id.cardGoalItem);
        TextView tvGoalTitle = rowView.findViewById(R.id.tvGoalTitle);
        TextView tvGoalStatus = rowView.findViewById(R.id.tvGoalStatus);
        TextView tvGoalTargetDate = rowView.findViewById(R.id.tvGoalTargetDate);
        TextView tvGoalSnippet = rowView.findViewById(R.id.tvGoalSnippet);
        TextView tvGoalProgress = rowView.findViewById(R.id.tvGoalProgress);
        ProgressBar pbGoalProgress = rowView.findViewById(R.id.pbGoalProgress);

        Button btnDetails = rowView.findViewById(R.id.btnDetails);
        Button btnQuickProgress = rowView.findViewById(R.id.btnQuickProgress);
        Button btnEdit = rowView.findViewById(R.id.btnEditGoal);
        Button btnDelete = rowView.findViewById(R.id.btnDeleteGoal);

        if (goal != null) {
            tvGoalTitle.setText(goal.getTitle());

            // Target date
            tvGoalTargetDate.setText("📅 Target: " + goal.getTargetDateFormatted());

            // Optional description snippet
            String desc = goal.getDescription();
            if (desc != null && !desc.trim().isEmpty()) {
                tvGoalSnippet.setText(desc.trim());
                tvGoalSnippet.setVisibility(View.VISIBLE);
            } else {
                tvGoalSnippet.setVisibility(View.GONE);
            }

            // Progress text and bar
            tvGoalProgress.setText("Progress: " + goal.getProgress() + "%");
            pbGoalProgress.setProgress(goal.getProgress());

            // Status Badge
            if (goal.isCompleted()) {
                tvGoalStatus.setText(R.string.goal_status_completed);
            } else {
                tvGoalStatus.setText(R.string.goal_status_in_progress);
            }

            // Click listener for details view
            View.OnClickListener viewListener = v -> {
                if (listener != null) {
                    listener.onView(goal);
                }
            };
            cardGoalItem.setOnClickListener(viewListener);
            btnDetails.setOnClickListener(viewListener);

            // Action listeners
            btnQuickProgress.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateProgress(goal);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(goal);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(goal);
                }
            });
        }

        return rowView;
    }
}
