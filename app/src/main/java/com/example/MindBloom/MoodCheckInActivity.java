package com.example.MindBloom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

/**
 * MoodCheckInActivity allows the user to record or edit a mood check-in.
 * Demonstrates Form Controls (RadioGroup, Spinner, EditText, Button) from Week 4,
 * Activity Navigation from Week 2(b), and Firebase Realtime Database writes from Week 11.
 */
public class MoodCheckInActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "extra_entry_id";
    public static final String EXTRA_MOOD = "extra_mood";
    public static final String EXTRA_INTENSITY = "extra_intensity";
    public static final String EXTRA_NOTE = "extra_note";
    public static final String EXTRA_TIMESTAMP = "extra_timestamp";

    private RadioGroup rgMood;
    private RadioButton rbHappy, rbCalm, rbOkay, rbSad, rbStressed;
    private Spinner spinnerIntensity;
    private EditText etNote;
    private Button btnSave, btnCancel;
    private TextView tvTitle;

    private MoodManager moodManager;
    private boolean isEditMode = false;
    private String existingId = null;
    private long existingTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mood_checkin);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, R.string.msg_require_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkinRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        moodManager = new MoodManager();

        initViews();
        setupIntensitySpinner();
        checkEditMode();

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveMoodEntry());
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvCheckInTitle);
        rgMood = findViewById(R.id.rgMood);
        rbHappy = findViewById(R.id.rbHappy);
        rbCalm = findViewById(R.id.rbCalm);
        rbOkay = findViewById(R.id.rbOkay);
        rbSad = findViewById(R.id.rbSad);
        rbStressed = findViewById(R.id.rbStressed);
        spinnerIntensity = findViewById(R.id.spinnerIntensity);
        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    /**
     * Populate the Spinner with intensity options 1 to 5 (Week 4 pattern).
     */
    private void setupIntensitySpinner() {
        String[] intensityLevels = {
                "1 - Very Mild",
                "2 - Mild",
                "3 - Moderate",
                "4 - Strong",
                "5 - Intense"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                intensityLevels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIntensity.setAdapter(adapter);
        spinnerIntensity.setSelection(2); // Default to "3 - Moderate"
    }

    /**
     * Check if an existing entry was passed via Intent for editing.
     */
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ENTRY_ID)) {
            isEditMode = true;
            existingId = intent.getStringExtra(EXTRA_ENTRY_ID);
            String existingMood = intent.getStringExtra(EXTRA_MOOD);
            int existingIntensity = intent.getIntExtra(EXTRA_INTENSITY, 3);
            String existingNote = intent.getStringExtra(EXTRA_NOTE);
            existingTimestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis());

            tvTitle.setText(R.string.mood_checkin_edit_title);
            btnSave.setText(R.string.btn_update_mood);

            // Pre-select the mood radio button
            if (existingMood != null) {
                switch (existingMood) {
                    case "Happy":
                        rbHappy.setChecked(true);
                        break;
                    case "Calm":
                        rbCalm.setChecked(true);
                        break;
                    case "Okay":
                        rbOkay.setChecked(true);
                        break;
                    case "Sad":
                        rbSad.setChecked(true);
                        break;
                    case "Stressed":
                        rbStressed.setChecked(true);
                        break;
                }
            }

            // Pre-select intensity (1-indexed to 0-indexed position)
            if (existingIntensity >= 1 && existingIntensity <= 5) {
                spinnerIntensity.setSelection(existingIntensity - 1);
            }

            if (existingNote != null) {
                etNote.setText(existingNote);
            }
        }
    }

    /**
     * Validates input and persists the entry to Firebase Realtime Database.
     */
    private void saveMoodEntry() {
        int selectedRadioId = rgMood.getCheckedRadioButtonId();

        // 1. Validation: Require a mood to be selected
        if (selectedRadioId == -1) {
            Toast.makeText(this, R.string.err_no_mood_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        String mood;
        if (selectedRadioId == R.id.rbHappy) {
            mood = "Happy";
        } else if (selectedRadioId == R.id.rbCalm) {
            mood = "Calm";
        } else if (selectedRadioId == R.id.rbOkay) {
            mood = "Okay";
        } else if (selectedRadioId == R.id.rbSad) {
            mood = "Sad";
        } else if (selectedRadioId == R.id.rbStressed) {
            mood = "Stressed";
        } else {
            mood = "Okay";
        }

        int intensity = spinnerIntensity.getSelectedItemPosition() + 1;
        String note = etNote.getText().toString().trim();
        long timestamp = isEditMode && existingTimestamp > 0 ? existingTimestamp : System.currentTimeMillis();

        MoodEntry entry = new MoodEntry(existingId, mood, intensity, note, timestamp);

        btnSave.setEnabled(false);

        if (isEditMode) {
            moodManager.updateEntry(entry, new MoodManager.OperationCallback() {
                @Override
                public void onSuccess() {
                    btnSave.setEnabled(true);
                    Toast.makeText(MoodCheckInActivity.this, R.string.msg_entry_updated, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    btnSave.setEnabled(true);
                    Toast.makeText(MoodCheckInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            moodManager.saveEntry(entry, new MoodManager.OperationCallback() {
                @Override
                public void onSuccess() {
                    btnSave.setEnabled(true);
                    Toast.makeText(MoodCheckInActivity.this, R.string.msg_entry_saved, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    btnSave.setEnabled(true);
                    Toast.makeText(MoodCheckInActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
