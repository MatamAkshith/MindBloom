package com.example.MindBloom;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * GoalFormActivity allows the user to create or edit a wellness goal.
 * Demonstrates Form Controls (EditText, SeekBar, Button, DatePickerDialog) from Week 4,
 * Activity Navigation from Week 2(b), and Firebase Realtime Database writes from Week 11.
 */
public class GoalFormActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID = "extra_goal_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DESCRIPTION = "extra_description";
    public static final String EXTRA_TARGET_DATE = "extra_target_date";
    public static final String EXTRA_PROGRESS = "extra_progress";
    public static final String EXTRA_CREATED_AT = "extra_created_at";

    private TextView tvFormTitle;
    private EditText etGoalTitle;
    private EditText etGoalDescription;
    private Button btnPickTargetDate;
    private TextView tvSelectedTargetDate;
    private SeekBar sbGoalProgress;
    private TextView tvProgressValue;
    private ProgressBar pbGoalSave;
    private Button btnSaveGoal;
    private Button btnCancelGoal;

    private GoalManager goalManager;
    private boolean isEditMode = false;
    private String existingGoalId = null;
    private long existingCreatedAt = 0;
    private long selectedTargetDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goal_form);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, R.string.msg_require_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.goalFormRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        goalManager = new GoalManager();

        initViews();
        setupProgressControls();
        setupDatePicker();
        checkEditMode();

        btnCancelGoal.setOnClickListener(v -> finish());
        btnSaveGoal.setOnClickListener(v -> saveGoal());
    }

    private void initViews() {
        tvFormTitle = findViewById(R.id.tvGoalFormTitle);
        etGoalTitle = findViewById(R.id.etGoalTitle);
        etGoalDescription = findViewById(R.id.etGoalDescription);
        btnPickTargetDate = findViewById(R.id.btnPickTargetDate);
        tvSelectedTargetDate = findViewById(R.id.tvSelectedTargetDate);
        sbGoalProgress = findViewById(R.id.sbGoalProgress);
        tvProgressValue = findViewById(R.id.tvProgressValue);
        pbGoalSave = findViewById(R.id.pbGoalSave);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);
        btnCancelGoal = findViewById(R.id.btnCancelGoal);
    }

    /**
     * Configures the SeekBar and quick preset progress buttons (0%, 25%, 50%, 75%, 100%).
     */
    private void setupProgressControls() {
        sbGoalProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressValue.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        findViewById(R.id.btnFormPreset0).setOnClickListener(v -> setProgressValue(0));
        findViewById(R.id.btnFormPreset25).setOnClickListener(v -> setProgressValue(25));
        findViewById(R.id.btnFormPreset50).setOnClickListener(v -> setProgressValue(50));
        findViewById(R.id.btnFormPreset75).setOnClickListener(v -> setProgressValue(75));
        findViewById(R.id.btnFormPreset100).setOnClickListener(v -> setProgressValue(100));
    }

    private void setProgressValue(int value) {
        sbGoalProgress.setProgress(value);
        tvProgressValue.setText(value + "%");
    }

    /**
     * Opens a DatePickerDialog (Lab Manual Week 4 pattern) to select target date.
     */
    private void setupDatePicker() {
        btnPickTargetDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedTargetDate > 0) {
                calendar.setTimeInMillis(selectedTargetDate);
            }

            DatePickerDialog dialog = new DatePickerDialog(
                    GoalFormActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, dayOfMonth, 23, 59, 59);
                        selectedTargetDate = chosen.getTimeInMillis();
                        updateDateDisplay();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void updateDateDisplay() {
        if (selectedTargetDate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            tvSelectedTargetDate.setText("📅 " + sdf.format(new Date(selectedTargetDate)));
        } else {
            tvSelectedTargetDate.setText("No date chosen yet");
        }
    }

    /**
     * Prepopulates fields if launched in edit mode.
     */
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_GOAL_ID)) {
            isEditMode = true;
            existingGoalId = intent.getStringExtra(EXTRA_GOAL_ID);
            String title = intent.getStringExtra(EXTRA_TITLE);
            String desc = intent.getStringExtra(EXTRA_DESCRIPTION);
            selectedTargetDate = intent.getLongExtra(EXTRA_TARGET_DATE, 0);
            int progress = intent.getIntExtra(EXTRA_PROGRESS, 0);
            existingCreatedAt = intent.getLongExtra(EXTRA_CREATED_AT, System.currentTimeMillis());

            tvFormTitle.setText(R.string.goal_form_edit_title);
            btnSaveGoal.setText(R.string.btn_update_goal);

            if (title != null) {
                etGoalTitle.setText(title);
            }
            if (desc != null) {
                etGoalDescription.setText(desc);
            }
            updateDateDisplay();
            setProgressValue(progress);
        }
    }

    /**
     * Validates input and persists goal to Firebase Realtime Database.
     */
    private void saveGoal() {
        String title = etGoalTitle.getText().toString().trim();
        String desc = etGoalDescription.getText().toString().trim();
        int progress = sbGoalProgress.getProgress();

        // 1. Validation: Title cannot be blank
        if (title.isEmpty()) {
            etGoalTitle.setError(getString(R.string.err_empty_goal_title));
            etGoalTitle.requestFocus();
            Toast.makeText(this, R.string.err_empty_goal_title, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validation: Target Date must be chosen
        if (selectedTargetDate <= 0) {
            Toast.makeText(this, R.string.err_no_target_date, Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validation: Progress must be 0 to 100
        if (progress < 0 || progress > 100) {
            Toast.makeText(this, R.string.err_invalid_progress, Toast.LENGTH_SHORT).show();
            return;
        }

        long now = System.currentTimeMillis();
        long createdAt = isEditMode && existingCreatedAt > 0 ? existingCreatedAt : now;
        long updatedAt = now;

        Goal goal = new Goal(existingGoalId, title, desc, selectedTargetDate, progress, createdAt, updatedAt);

        setLoading(true);

        if (isEditMode) {
            goalManager.updateGoal(goal, new GoalManager.OperationCallback() {
                @Override
                public void onSuccess() {
                    setLoading(false);
                    Toast.makeText(GoalFormActivity.this, R.string.msg_goal_updated, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(GoalFormActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            goalManager.saveGoal(goal, new GoalManager.OperationCallback() {
                @Override
                public void onSuccess() {
                    setLoading(false);
                    Toast.makeText(GoalFormActivity.this, R.string.msg_goal_saved, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(GoalFormActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setLoading(boolean isLoading) {
        if (pbGoalSave != null) {
            pbGoalSave.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnSaveGoal.setEnabled(!isLoading);
        btnCancelGoal.setEnabled(!isLoading);
    }
}
