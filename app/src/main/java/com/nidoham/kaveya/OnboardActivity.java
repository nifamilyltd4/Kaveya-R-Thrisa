package com.nidoham.kaveya;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nidoham.kaveya.databinding.ActivityOnboardBinding;
import com.nidoham.kaveya.firebase.google.authentication.User;

public class OnboardActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    private ActivityOnboardBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFirebase();
        configureGoogleSignIn();
        setupClickListeners();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        binding.googleSignInButton.setOnClickListener(v -> startGoogleSignIn());
        //binding.closeButton.setOnClickListener(v -> finish());
    }

    private void startGoogleSignIn() {
        binding.googleSignInButton.setEnabled(false);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                authenticateWithFirebase(account);
            } catch (ApiException e) {
                showErrorToast("Sign-in failed: " + e.getMessage());
                resetSignInButton();
            }
        }
    }

    private void authenticateWithFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            syncUserData(firebaseUser);
                        } else {
                            showErrorToast("Firebase user not found");
                            resetSignInButton();
                        }
                    } else {
                        showErrorToast("Authentication failed");
                        resetSignInButton();
                    }
                });
    }

    private void syncUserData(FirebaseUser firebaseUser) {
        usersRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user;
                if (dataSnapshot.exists()) {
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                    } else {
                        user = createNewUser(firebaseUser);
                    }
                } else {
                    user = createNewUser(firebaseUser);
                }
                saveUserToDatabase(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showErrorToast("Failed to access user data");
                resetSignInButton();
            }
        });
    }

    private User createNewUser(FirebaseUser firebaseUser) {
        User user = new User();
        user.setUserId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
        user.setFullName(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
        user.setProfilePictureUrl(firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        return user;
    }

    private void saveUserToDatabase(User user) {
        usersRef.child(user.getUserId()).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToMainActivity(user);
                    } else {
                        showErrorToast("Failed to save user data");
                        resetSignInButton();
                    }
                });
    }

    private void navigateToMainActivity(User user) {
        Toast.makeText(this, "Welcome " + user.getFullName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USER_ID", user.getUserId());
        startActivity(intent);
        finish();
    }

    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                .format(new java.util.Date());
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void resetSignInButton() {
        binding.googleSignInButton.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            syncUserData(currentUser);
        }
    }
}