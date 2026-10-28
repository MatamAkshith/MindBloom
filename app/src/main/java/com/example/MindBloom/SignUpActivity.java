package com.example.MindBloom;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
 * SignUpActivity provides user registration using Firebase Authentication.
 * Validates inputs, handles duplicate email and weak password errors gracefully,
 * and navigates to the dashboard upon successful account creation.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvGoToLogin, tvSignUpError;
    private ProgressBar pbSignUp;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signUpRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        initViews();

        btnSignUp.setOnClickListener(v -> performSignUp());
        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        etEmail = findViewById(R.id.etSignUpEmail);
        etPassword = findViewById(R.id.etSignUpPassword);
        etConfirmPassword = findViewById(R.id.etSignUpConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        tvSignUpError = findViewById(R.id.tvSignUpError);
        pbSignUp = findViewById(R.id.pbSignUp);
    }

    private void performSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 1. Validation
        if (email.isEmpty()) {
            showError(getString(R.string.err_empty_email));
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(getString(R.string.err_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            showError(getString(R.string.err_empty_password));
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            showError(getString(R.string.err_password_too_short));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(getString(R.string.err_passwords_do_not_match));
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);
        tvSignUpError.setVisibility(View.GONE);

        // 2. Firebase Authentication Create User
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignUpActivity.this, "Welcome to MindBloom!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SignUpActivity.this, DashboardActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Registration failed.";
                        showError(errorMessage);
                    }
                });
    }

    private void showError(String message) {
        tvSignUpError.setText(message);
        tvSignUpError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean isLoading) {
        pbSignUp.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!isLoading);
    }
}
