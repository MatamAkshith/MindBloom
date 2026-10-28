package com.example.MindBloom;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
 * MoodHistoryActivity displays previously saved mood entries in a ListView.
 * Demonstrates ListView and custom ArrayAdapter (Week 5b), AlertDialog confirmation (Week 3b),
 * Explicit Intents for Navigation (Week 2b), and Firebase Realtime Database reads/deletions (Week 11).
 */
public class MoodHistoryActivity extends AppCompatActivity implements MoodAdapter.OnEntryActionListener {

    private ListView lvMoodHistory;
    private LinearLayout llEmptyState;
    private TextView tvStatusNotice;
    private Button btnNewCheckIn;

    private MoodManager moodManager;
    private ValueEventListener valueEventListener;
    private final List<MoodEntry> entryList = new ArrayList<>();
    private MoodAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mood_history);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.historyRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupList();

        moodManager = new MoodManager();

        btnNewCheckIn.setOnClickListener(v -> {
            Intent intent = new Intent(MoodHistoryActivity.this, MoodCheckInActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        lvMoodHistory = findViewById(R.id.lvMoodHistory);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvStatusNotice = findViewById(R.id.tvStatusNotice);
        btnNewCheckIn = findViewById(R.id.btnNewCheckIn);
    }

    private void setupList() {
        adapter = new MoodAdapter(this, entryList, this);
        lvMoodHistory.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }
        loadMoodEntries();
    }

    private void redirectToLogin() {
        Toast.makeText(this, R.string.msg_require_login, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MoodHistoryActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (valueEventListener != null && moodManager != null) {
            moodManager.removeListener(valueEventListener);
        }
    }

    /**
     * Listen for real-time updates from Firebase.
     * Accurately surfaces errors (e.g., locked rules / permission denied) without faking data.
     */
    private void loadMoodEntries() {
        valueEventListener = moodManager.listenToEntries(new MoodManager.DataCallback() {
            @Override
            public void onDataLoaded(List<MoodEntry> entries) {
                tvStatusNotice.setVisibility(View.GONE);
                entryList.clear();
                if (entries != null && !entries.isEmpty()) {
                    entryList.addAll(entries);
                    adapter.notifyDataSetChanged();
                    lvMoodHistory.setVisibility(View.VISIBLE);
                    llEmptyState.setVisibility(View.GONE);
                } else {
                    adapter.notifyDataSetChanged();
                    lvMoodHistory.setVisibility(View.GONE);
                    llEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // If permission is denied due to locked rules, surface clear message
                tvStatusNotice.setText(errorMessage);
                tvStatusNotice.setVisibility(View.VISIBLE);
                if (entryList.isEmpty()) {
                    llEmptyState.setVisibility(View.VISIBLE);
                    lvMoodHistory.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Edit action: Open MoodCheckInActivity in Edit Mode with entry values passed as Intent extras.
     */
    @Override
    public void onEdit(MoodEntry entry) {
        if (entry == null) return;
        Intent intent = new Intent(MoodHistoryActivity.this, MoodCheckInActivity.class);
        intent.putExtra(MoodCheckInActivity.EXTRA_ENTRY_ID, entry.getId());
        intent.putExtra(MoodCheckInActivity.EXTRA_MOOD, entry.getMood());
        intent.putExtra(MoodCheckInActivity.EXTRA_INTENSITY, entry.getIntensity());
        intent.putExtra(MoodCheckInActivity.EXTRA_NOTE, entry.getNote());
        intent.putExtra(MoodCheckInActivity.EXTRA_TIMESTAMP, entry.getTimestamp());
        startActivity(intent);
    }

    /**
     * Delete action: Confirm before deleting using AlertDialog.Builder (Lab Manual Week 3b).
     */
    @Override
    public void onDelete(MoodEntry entry) {
        if (entry == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_delete_title);
        builder.setMessage(R.string.dialog_delete_message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton(R.string.btn_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moodManager.deleteEntry(entry.getId(), new MoodManager.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MoodHistoryActivity.this, R.string.msg_entry_deleted, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MoodHistoryActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
