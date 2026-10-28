package com.example.MindBloom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * GoalsActivity displays previously created goals in a ListView.
 * Demonstrates ListView and custom ArrayAdapter (Week 5b), AlertDialog confirmation and detail viewing (Week 3b),
 * Explicit Intents for Navigation (Week 2b), and Firebase Realtime Database reads/deletions (Week 11).
 */
public class GoalsActivity extends AppCompatActivity implements GoalAdapter.OnGoalActionListener {

    private ListView lvGoals;
    private LinearLayout llEmptyState;
    private TextView tvStatusNotice;
    private Button btnNewGoal;

    private GoalManager goalManager;
    private ValueEventListener valueEventListener;
    private final List<Goal> goalList = new ArrayList<>();
    private GoalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goals);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goalsRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupList();

        goalManager = new GoalManager();

        btnNewGoal.setOnClickListener(v -> {
            Intent intent = new Intent(GoalsActivity.this, GoalFormActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        lvGoals = findViewById(R.id.lvGoals);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvStatusNotice = findViewById(R.id.tvStatusNotice);
        btnNewGoal = findViewById(R.id.btnNewGoal);
    }

    private void setupList() {
        adapter = new GoalAdapter(this, goalList, this);
        lvGoals.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        loadGoals();
    }

    private void redirectToLogin() {
        Toast.makeText(this, R.string.msg_require_login, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(GoalsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (valueEventListener != null && goalManager != null) {
            goalManager.removeListener(valueEventListener);
        }
    }

    /**
     * Listen for real-time updates from Firebase.
     * Accurately surfaces errors without faking data.
     */
    private void loadGoals() {
        valueEventListener = goalManager.listenToGoals(new GoalManager.DataCallback() {
            @Override
            public void onDataLoaded(List<Goal> goals) {
                tvStatusNotice.setVisibility(View.GONE);
                goalList.clear();
                if (goals != null && !goals.isEmpty()) {
                    goalList.addAll(goals);
                    adapter.notifyDataSetChanged();
                    lvGoals.setVisibility(View.VISIBLE);
                    llEmptyState.setVisibility(View.GONE);
                } else {
                    adapter.notifyDataSetChanged();
                    lvGoals.setVisibility(View.GONE);
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                tvStatusNotice.setText(errorMessage);
                tvStatusNotice.setVisibility(View.VISIBLE);
                if (goalList.isEmpty()) {
                    llEmptyState.setVisibility(View.VISIBLE);
                    lvGoals.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * View action: Displays the complete goal details in an AlertDialog (Week 3b).
     */
    @Override
    public void onView(Goal goal) {
        if (goal == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_goal_detail, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDetailGoalTitle);
        TextView tvStatus = dialogView.findViewById(R.id.tvDetailGoalStatus);
        TextView tvTarget = dialogView.findViewById(R.id.tvDetailTargetDate);
        TextView tvProgressText = dialogView.findViewById(R.id.tvDetailProgressText);
        ProgressBar pbProgress = dialogView.findViewById(R.id.pbDetailProgress);
        TextView tvDescription = dialogView.findViewById(R.id.tvDetailDescription);
        TextView tvCreatedAt = dialogView.findViewById(R.id.tvDetailCreatedAt);
        TextView tvUpdatedAt = dialogView.findViewById(R.id.tvDetailUpdatedAt);

        tvTitle.setText(goal.getTitle());
        tvStatus.setText(goal.isCompleted() ? getString(R.string.goal_status_completed) : getString(R.string.goal_status_in_progress));
        tvTarget.setText("📅 Target Date: " + goal.getTargetDateFormatted());
        tvProgressText.setText("Progress: " + goal.getProgress() + "%");
        pbProgress.setProgress(goal.getProgress());

        String desc = goal.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            tvDescription.setText(desc.trim());
        } else {
            tvDescription.setText("No additional description provided.");
        }

        tvCreatedAt.setText("Created: " + goal.getCreatedAtFormatted());
        tvUpdatedAt.setText("Last updated: " + goal.getUpdatedAtFormatted());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.btn_close, (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton(R.string.btn_edit, (dialog, which) -> {
            dialog.dismiss();
            onEdit(goal);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Quick progress update modal dialog allowing user to adjust percentage and save immediately.
     */
    @Override
    public void onUpdateProgress(Goal goal) {
        if (goal == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_update_progress, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvQuickGoalTitle);
        TextView tvVal = dialogView.findViewById(R.id.tvQuickProgressValue);
        SeekBar sb = dialogView.findViewById(R.id.sbQuickProgress);

        tvTitle.setText(goal.getTitle());
        sb.setProgress(goal.getProgress());
        tvVal.setText(goal.getProgress() + "%");

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvVal.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        dialogView.findViewById(R.id.btnPreset0).setOnClickListener(v -> sb.setProgress(0));
        dialogView.findViewById(R.id.btnPreset25).setOnClickListener(v -> sb.setProgress(25));
        dialogView.findViewById(R.id.btnPreset50).setOnClickListener(v -> sb.setProgress(50));
        dialogView.findViewById(R.id.btnPreset75).setOnClickListener(v -> sb.setProgress(75));
        dialogView.findViewById(R.id.btnPreset100).setOnClickListener(v -> sb.setProgress(100));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_update_progress_title);
        builder.setView(dialogView);

        builder.setPositiveButton(R.string.btn_update_goal, (dialog, which) -> {
            int newProgress = sb.getProgress();
            goalManager.updateGoalProgress(goal.getId(), newProgress, new GoalManager.OperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GoalsActivity.this, R.string.msg_goal_updated, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(GoalsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Edit action: Open GoalFormActivity in Edit Mode with goal values passed as Intent extras.
     */
    @Override
    public void onEdit(Goal goal) {
        if (goal == null) return;
        Intent intent = new Intent(GoalsActivity.this, GoalFormActivity.class);
        intent.putExtra(GoalFormActivity.EXTRA_GOAL_ID, goal.getId());
        intent.putExtra(GoalFormActivity.EXTRA_TITLE, goal.getTitle());
        intent.putExtra(GoalFormActivity.EXTRA_DESCRIPTION, goal.getDescription());
        intent.putExtra(GoalFormActivity.EXTRA_TARGET_DATE, goal.getTargetDate());
        intent.putExtra(GoalFormActivity.EXTRA_PROGRESS, goal.getProgress());
        intent.putExtra(GoalFormActivity.EXTRA_CREATED_AT, goal.getCreatedAt());
        startActivity(intent);
    }

    /**
     * Delete action: Confirm before deleting using AlertDialog.Builder (Lab Manual Week 3b).
     */
    @Override
    public void onDelete(Goal goal) {
        if (goal == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_delete_goal_title);
        builder.setMessage(R.string.dialog_delete_goal_message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goalManager.deleteGoal(goal.getId(), new GoalManager.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(GoalsActivity.this, R.string.msg_goal_deleted, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(GoalsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
