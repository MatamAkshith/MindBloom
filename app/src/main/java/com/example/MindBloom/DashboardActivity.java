package com.example.MindBloom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * DashboardActivity represents the main home dashboard for MindBloom.
 * Guards against unauthenticated access, displays user session status,
 * provides sign-out action, and routes to module features.
 */
public class DashboardActivity extends AppCompatActivity {

    private TextView tvUserEmail;
    private Button btnSignOut;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        // Enforce authentication guard
        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        // Adjust window padding for system status and navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboardRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupUserSession();

        // Sign Out Action
        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(DashboardActivity.this, R.string.msg_logout_success, Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });

        // Initialize module cards
        LinearLayout cardMoodTracker = findViewById(R.id.cardMoodTracker);
        LinearLayout cardJournal = findViewById(R.id.cardJournal);
        LinearLayout cardRelaxation = findViewById(R.id.cardRelaxation);
        LinearLayout cardGoals = findViewById(R.id.cardGoals);

        // Mood Tracker Module: Navigate to MoodHistoryActivity (Week 2b)
        cardMoodTracker.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, MoodHistoryActivity.class);
            startActivity(intent);
        });

        // Wellness Journal Module: Navigate to JournalHistoryActivity (Phase 3)
        cardJournal.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, JournalHistoryActivity.class);
            startActivity(intent);
        });

        // Relaxation & Self-Care Module: Navigate to RelaxationActivity (Phase 4)
        cardRelaxation.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, RelaxationActivity.class);
            startActivity(intent);
        });

        // Goals & Progress Module: Navigate to GoalsActivity (Phase 5)
        cardGoals.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, GoalsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
        } else {
            setupUserSession();
        }
    }

    private void initViews() {
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnSignOut = findViewById(R.id.btnSignOut);
    }

    private void setupUserSession() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            tvUserEmail.setText(user.getEmail());
            tvUserEmail.setVisibility(View.VISIBLE);
        } else {
            tvUserEmail.setVisibility(View.GONE);
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
