package com.example.MindBloom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
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
 * JournalHistoryActivity displays previously saved journal entries in a ListView.
 * Demonstrates ListView and custom ArrayAdapter (Week 5b), AlertDialog confirmation and detail viewing (Week 3b),
 * Explicit Intents for Navigation (Week 2b), and Firebase Realtime Database reads/deletions (Week 11).
 */
public class JournalHistoryActivity extends AppCompatActivity implements JournalAdapter.OnEntryActionListener {

    private ListView lvJournalHistory;
    private LinearLayout llEmptyState;
    private TextView tvStatusNotice;
    private Button btnNewJournal;

    private JournalManager journalManager;
    private ValueEventListener valueEventListener;
    private final List<JournalEntry> entryList = new ArrayList<>();
    private JournalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal_history);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.journalHistoryRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupList();

        journalManager = new JournalManager();

        btnNewJournal.setOnClickListener(v -> {
            Intent intent = new Intent(JournalHistoryActivity.this, JournalWriteActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        lvJournalHistory = findViewById(R.id.lvJournalHistory);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvStatusNotice = findViewById(R.id.tvStatusNotice);
        btnNewJournal = findViewById(R.id.btnNewJournal);
    }

    private void setupList() {
        adapter = new JournalAdapter(this, entryList, this);
        lvJournalHistory.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        loadJournalEntries();
    }

    private void redirectToLogin() {
        Toast.makeText(this, R.string.msg_require_login, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(JournalHistoryActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (valueEventListener != null && journalManager != null) {
            journalManager.removeListener(valueEventListener);
        }
    }

    /**
     * Listen for real-time updates from Firebase.
     * Accurately surfaces errors (e.g., locked rules / permission denied) without faking data.
     */
    private void loadJournalEntries() {
        valueEventListener = journalManager.listenToEntries(new JournalManager.DataCallback() {
            @Override
            public void onDataLoaded(List<JournalEntry> entries) {
                tvStatusNotice.setVisibility(View.GONE);
                entryList.clear();
                if (entries != null && !entries.isEmpty()) {
                    entryList.addAll(entries);
                    adapter.notifyDataSetChanged();
                    lvJournalHistory.setVisibility(View.VISIBLE);
                    llEmptyState.setVisibility(View.GONE);
                } else {
                    adapter.notifyDataSetChanged();
                    lvJournalHistory.setVisibility(View.GONE);
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Surface Firebase errors cleanly
                tvStatusNotice.setText(errorMessage);
                tvStatusNotice.setVisibility(View.VISIBLE);
                if (entryList.isEmpty()) {
                    llEmptyState.setVisibility(View.VISIBLE);
                    lvJournalHistory.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * View action: Displays the full reflection in an AlertDialog detail dialog.
     */
    @Override
    public void onView(JournalEntry entry) {
        if (entry == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_journal_detail, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDetailTitle);
        TextView tvTimestamp = dialogView.findViewById(R.id.tvDetailTimestamp);
        TextView tvPrompt = dialogView.findViewById(R.id.tvDetailPrompt);
        TextView tvContent = dialogView.findViewById(R.id.tvDetailContent);

        tvTitle.setText(entry.getDisplayTitle());
        tvTimestamp.setText(entry.getFormattedDate());

        if (entry.getPrompt() != null && !entry.getPrompt().trim().isEmpty()) {
            tvPrompt.setText("Prompt: " + entry.getPrompt());
            tvPrompt.setVisibility(View.VISIBLE);
        } else {
            tvPrompt.setVisibility(View.GONE);
        }

        tvContent.setText(entry.getContent());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.btn_close, (dialog, which) -> dialog.dismiss());
        builder.setNeutralButton(R.string.btn_edit, (dialog, which) -> {
            dialog.dismiss();
            onEdit(entry);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Edit action: Open JournalWriteActivity in Edit Mode with entry values passed as Intent extras.
     */
    @Override
    public void onEdit(JournalEntry entry) {
        if (entry == null) return;
        Intent intent = new Intent(JournalHistoryActivity.this, JournalWriteActivity.class);
        intent.putExtra(JournalWriteActivity.EXTRA_ENTRY_ID, entry.getId());
        intent.putExtra(JournalWriteActivity.EXTRA_TITLE, entry.getTitle());
        intent.putExtra(JournalWriteActivity.EXTRA_PROMPT, entry.getPrompt());
        intent.putExtra(JournalWriteActivity.EXTRA_CONTENT, entry.getContent());
        intent.putExtra(JournalWriteActivity.EXTRA_TIMESTAMP, entry.getTimestamp());
        startActivity(intent);
    }

    /**
     * Delete action: Confirm before deleting using AlertDialog.Builder (Lab Manual Week 3b).
     */
    @Override
    public void onDelete(JournalEntry entry) {
        if (entry == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_delete_journal_title);
        builder.setMessage(R.string.dialog_delete_journal_message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                journalManager.deleteEntry(entry.getId(), new JournalManager.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(JournalHistoryActivity.this, R.string.msg_journal_deleted, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(JournalHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
