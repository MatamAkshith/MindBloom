package com.example.MindBloom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * RelaxationActivity implements Phase 4: Relaxation & Self-Care.
 * Provides:
 * 1. A persistent, user-scoped daily self-care checklist (CheckBox controls, Week 4)
 *    persisted via Android SharedPreferences with automatic daily reset behavior.
 * 2. A clean selection of 5 beginner-friendly guided relaxation activities
 *    displaying duration, summary, and practical step-by-step guidance in Dialogs (Week 3b).
 */
public class RelaxationActivity extends AppCompatActivity {

    private TextView tvSelfCareCount;
    private TextView tvSelfCareDate;
    private TextView tvAllDoneBanner;
    private ProgressBar pbSelfCare;

    private CheckBox cbWater;
    private CheckBox cbMovement;
    private CheckBox cbScreen;
    private CheckBox cbPositive;
    private CheckBox cbSleep;

    private LinearLayout cardBreathing;
    private LinearLayout cardMindful;
    private LinearLayout cardStretch;
    private LinearLayout cardGratitude;
    private LinearLayout cardScreenBreak;

    private SelfCareManager selfCareManager;
    private final List<RelaxationItem> relaxationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_relaxation);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.relaxationRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selfCareManager = new SelfCareManager(this, user.getUid());

        initViews();
        setupSelfCareChecklist();
        setupRelaxationActivities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            redirectToLogin();
            return;
        }
        // Refresh date and checklist state if resumed on a new day
        selfCareManager.checkAndResetForNewDay();
        refreshChecklistUI();
    }

    private void initViews() {
        tvSelfCareCount = findViewById(R.id.tvSelfCareCount);
        tvSelfCareDate = findViewById(R.id.tvSelfCareDate);
        tvAllDoneBanner = findViewById(R.id.tvAllDoneBanner);
        pbSelfCare = findViewById(R.id.pbSelfCare);

        cbWater = findViewById(R.id.cbWater);
        cbMovement = findViewById(R.id.cbMovement);
        cbScreen = findViewById(R.id.cbScreen);
        cbPositive = findViewById(R.id.cbPositive);
        cbSleep = findViewById(R.id.cbSleep);

        cardBreathing = findViewById(R.id.cardBreathing);
        cardMindful = findViewById(R.id.cardMindful);
        cardStretch = findViewById(R.id.cardStretch);
        cardGratitude = findViewById(R.id.cardGratitude);
        cardScreenBreak = findViewById(R.id.cardScreenBreak);
    }

    /**
     * Initializes and binds the daily self-care checklist.
     */
    private void setupSelfCareChecklist() {
        tvSelfCareDate.setText(selfCareManager.getFormattedDisplayDate() + " • Resets daily at midnight");

        CheckBox[] checkBoxes = {cbWater, cbMovement, cbScreen, cbPositive, cbSleep};

        for (int i = 0; i < checkBoxes.length; i++) {
            final int index = i;
            CheckBox cb = checkBoxes[i];

            // Set initial state from persistent SharedPreferences
            cb.setChecked(selfCareManager.isItemChecked(index));

            // Bind change listener
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selfCareManager.setItemChecked(index, isChecked);
                    updateProgressDisplay();
                }
            });
        }

        updateProgressDisplay();
    }

    private void refreshChecklistUI() {
        CheckBox[] checkBoxes = {cbWater, cbMovement, cbScreen, cbPositive, cbSleep};
        for (int i = 0; i < checkBoxes.length; i++) {
            checkBoxes[i].setChecked(selfCareManager.isItemChecked(i));
        }
        updateProgressDisplay();
    }

    /**
     * Updates completion counter, progress bar, and encouragement banner.
     */
    private void updateProgressDisplay() {
        int completed = selfCareManager.getCompletedCount();
        int total = selfCareManager.getTotalCount();

        tvSelfCareCount.setText(getString(R.string.self_care_progress_format, completed, total));
        pbSelfCare.setProgress(completed);

        if (completed == total) {
            tvAllDoneBanner.setVisibility(View.VISIBLE);
        } else {
            tvAllDoneBanner.setVisibility(View.GONE);
        }
    }

    /**
     * Configures the 5 relaxation practices and binds click events to show guided instructions.
     */
    private void setupRelaxationActivities() {
        relaxationList.clear();

        // 1. Deep Breathing
        relaxationList.add(new RelaxationItem(
                "breathing",
                "Deep Breathing (4-4-4 Box Breathing)",
                "3 mins",
                "🌬️",
                "Inhale, hold, and exhale steadily to activate your body's natural relaxation response.",
                "• Step 1: Sit upright in a comfortable position with your shoulders relaxed.\n\n" +
                "• Step 2: Inhale slowly and deeply through your nose for a count of 4.\n\n" +
                "• Step 3: Gently hold your breath for a count of 4.\n\n" +
                "• Step 4: Exhale smoothly and completely through your mouth for a count of 4.\n\n" +
                "• Step 5: Pause for a count of 4 before taking your next breath.\n\n" +
                "• Step 6: Repeat this cycle 4 to 5 times until you feel steady and calm."
        ));

        // 2. Mindful Pause
        relaxationList.add(new RelaxationItem(
                "mindful",
                "Mindful Pause (5-4-3-2-1 Sensory Grounding)",
                "2 mins",
                "🌿",
                "Anchor yourself in the present moment by noticing what is around you right now.",
                "• Step 1: Take one relaxed, deep breath and rest your hands on your lap.\n\n" +
                "• Step 2: Look around and silently name 5 distinct things you can see.\n\n" +
                "• Step 3: Notice 4 physical sensations (e.g. feet touching floor, clothes on shoulders).\n\n" +
                "• Step 4: Listen carefully for 3 subtle sounds in your environment.\n\n" +
                "• Step 5: Notice 2 things you can smell or enjoy in the air.\n\n" +
                "• Step 6: Bring to mind 1 positive thought or affirmation about yourself."
        ));

        // 3. Gentle Stretch
        relaxationList.add(new RelaxationItem(
                "stretch",
                "Gentle Stretch (Neck & Shoulder Release)",
                "3 mins",
                "🙆",
                "Release physical tension and stiffness built up from sitting, studying, or screen time.",
                "• Step 1: Drop your chin gently toward your chest; hold for 10 seconds.\n\n" +
                "• Step 2: Slowly tilt your right ear toward your right shoulder; hold for 15 seconds, then repeat smoothly on the left.\n\n" +
                "• Step 3: Roll your shoulders backward in slow, smooth circles 5 times, then forward 5 times.\n\n" +
                "• Step 4: Interlace your fingers, press your palms forward, and stretch your upper back gently.\n\n" +
                "• Step 5: Release your arms, shake out your hands, and breathe naturally."
        ));

        // 4. Gratitude Moment
        relaxationList.add(new RelaxationItem(
                "gratitude",
                "Gratitude Moment (Three Small Joys)",
                "2 mins",
                "🌸",
                "Shift your mental perspective toward warmth, appreciation, and simple joys.",
                "• Step 1: Settle into a comfortable posture and close your eyes if comfortable.\n\n" +
                "• Step 2: Recall one simple physical comfort you experienced today (e.g., a warm drink, fresh air, a soft seat).\n\n" +
                "• Step 3: Think of one person, friendly message, or supportive interaction you appreciated.\n\n" +
                "• Step 4: Acknowledge one small accomplishment or effort you made today, no matter how small.\n\n" +
                "• Step 5: Let that feeling of warmth and contentment linger for a few moments."
        ));

        // 5. Screen Break
        relaxationList.add(new RelaxationItem(
                "screen_break",
                "Screen Break (The 20-20-20 Eye Rest)",
                "1 min",
                "👀",
                "Relieve digital eye fatigue and reset your mental focus with a simple optical pause.",
                "• Step 1: Look away from your phone, laptop, or book.\n\n" +
                "• Step 2: Direct your gaze toward an object or window at least 20 feet (6 meters) away.\n\n" +
                "• Step 3: Maintain a soft, relaxed focus on that distant object for 20 seconds.\n\n" +
                "• Step 4: Blink smoothly 10 times to rehydrate and soothe your eyes.\n\n" +
                "• Step 5: Rub your palms together to create warmth, and gently cup them over your closed eyes for 10 seconds."
        ));

        // Bind clicks to display guide dialog
        cardBreathing.setOnClickListener(v -> showGuideDialog(relaxationList.get(0)));
        cardMindful.setOnClickListener(v -> showGuideDialog(relaxationList.get(1)));
        cardStretch.setOnClickListener(v -> showGuideDialog(relaxationList.get(2)));
        cardGratitude.setOnClickListener(v -> showGuideDialog(relaxationList.get(3)));
        cardScreenBreak.setOnClickListener(v -> showGuideDialog(relaxationList.get(4)));
    }

    /**
     * Displays practical step-by-step instructions in an AlertDialog (Lab Manual Week 3b).
     */
    private void showGuideDialog(RelaxationItem item) {
        if (item == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_relaxation_guide, null);

        TextView tvIcon = dialogView.findViewById(R.id.tvGuideIcon);
        TextView tvTitle = dialogView.findViewById(R.id.tvGuideTitle);
        TextView tvDuration = dialogView.findViewById(R.id.tvGuideDuration);
        TextView tvSummary = dialogView.findViewById(R.id.tvGuideSummary);
        TextView tvInstructions = dialogView.findViewById(R.id.tvGuideInstructions);

        tvIcon.setText(item.getIcon());
        tvTitle.setText(item.getTitle());
        tvDuration.setText("⏱️ " + item.getDuration() + " • Beginner Friendly");
        tvSummary.setText(item.getSummary());
        tvInstructions.setText(item.getInstructions());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.btn_done, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void redirectToLogin() {
        Toast.makeText(this, R.string.msg_require_login, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RelaxationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
