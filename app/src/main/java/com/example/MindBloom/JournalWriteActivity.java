package com.example.MindBloom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
 * JournalWriteActivity allows the user to create or edit a wellness reflection.
 * Demonstrates Form Controls (Spinner, EditText, Button) from Week 4,
 * Activity Navigation from Week 2(b), and Firebase Realtime Database writes from Week 11.
 */
public class JournalWriteActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "extra_entry_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_PROMPT = "extra_prompt";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_TIMESTAMP = "extra_timestamp";

    private TextView tvTitle;
    private EditText etTitle;
    private Spinner spinnerPrompt;
    private EditText etContent;
    private Button btnSave;
    private Button btnCancel;
    private ProgressBar pbSave;

    private JournalManager journalManager;
    private ArrayAdapter<String> promptAdapter;

    private boolean isEditMode = false;
    private String existingId = null;
    private long existingTimestamp = 0;

    private final String[] prompts = {
            "What was one good thing about today?",
            "What challenged me today?",
            "What am I grateful for?",
            "What would I like to improve tomorrow?",
            "Free reflection"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal_write);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, R.string.msg_require_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.journalWriteRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        journalManager = new JournalManager();

        initViews();
        setupPromptSpinner();
        checkEditMode();

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveJournalEntry());
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvJournalWriteTitle);
        etTitle = findViewById(R.id.etJournalTitle);
        spinnerPrompt = findViewById(R.id.spinnerPrompt);
        etContent = findViewById(R.id.etJournalContent);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        pbSave = findViewById(R.id.pbJournalSave);
    }

    /**
     * Populate the Spinner with reflective prompt options (Week 4 pattern).
     */
    private void setupPromptSpinner() {
        promptAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                prompts
        );
        promptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrompt.setAdapter(promptAdapter);
        spinnerPrompt.setSelection(0);
    }

    /**
     * Check if an existing entry was passed via Intent for editing.
     */
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ENTRY_ID)) {
            isEditMode = true;
            existingId = intent.getStringExtra(EXTRA_ENTRY_ID);
            String existingTitle = intent.getStringExtra(EXTRA_TITLE);
            String existingPrompt = intent.getStringExtra(EXTRA_PROMPT);
            String existingContent = intent.getStringExtra(EXTRA_CONTENT);
            existingTimestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis());

            tvTitle.setText(R.string.journal_write_edit_title);
            btnSave.setText(R.string.btn_update_journal);

            if (existingTitle != null) {
                etTitle.setText(existingTitle);
            }

            if (existingContent != null) {
                etContent.setText(existingContent);
            }

            // Pre-select matching prompt in spinner
            if (existingPrompt != null) {
                for (int i = 0; i < prompts.length; i++) {
                    if (prompts[i].equalsIgnoreCase(existingPrompt.trim())) {
                        spinnerPrompt.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Validates input and persists the entry to Firebase Realtime Database.
     */
    private void saveJournalEntry() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String selectedPrompt = (String) spinnerPrompt.getSelectedItem();

        // 1. Validation: Content must not be blank
        if (content.isEmpty()) {
            etContent.setError(getString(R.string.err_empty_journal_content));
            etContent.requestFocus();
            Toast.makeText(this, R.string.err_empty_journal_content, Toast.LENGTH_SHORT).show();
            return;
        }

        long timestamp = isEditMode && existingTimestamp > 0 ? existingTimestamp : System.currentTimeMillis();

        JournalEntry entry = new JournalEntry(existingId, title, content, selectedPrompt, timestamp);

        setLoading(true);

        if (isEditMode) {
            journalManager.updateEntry(entry, new JournalManager.OperationCallback() {
                @Override
                public void onSuccess() {
                    setLoading(false);
                    Toast.makeText(JournalWriteActivity.this, R.string.msg_journal_updated, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(JournalWriteActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            journalManager.saveEntry(entry, new JournalManager.OperationCallback() {
                @Override
                public void onSuccess() {
                    setLoading(false);
                    Toast.makeText(JournalWriteActivity.this, R.string.msg_journal_saved, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorMessage) {
                    setLoading(false);
                    Toast.makeText(JournalWriteActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setLoading(boolean isLoading) {
        if (pbSave != null) {
            pbSave.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnSave.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
    }
}
