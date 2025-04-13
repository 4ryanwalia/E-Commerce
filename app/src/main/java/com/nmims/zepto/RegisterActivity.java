package com.nmims.zepto;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;
public class RegisterActivity extends AppCompatActivity {
    private EditText editTextName, editTextMobile, editTextEmail, editTextPassword, editTextConfirmPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final String TAG = "RegisterActivity"; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editTextName = findViewById(R.id.editTextName);
        editTextMobile = findViewById(R.id.editTextMobile);
        editTextEmail = findViewById(R.id.editTextRegisterEmail);
        editTextPassword = findViewById(R.id.editTextRegisterPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        buttonRegister.setOnClickListener(v -> registerUser());
    }
    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String mobile = editTextMobile.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { 
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "mail eror maybe");
                                                    Toast.makeText(RegisterActivity.this, "email sent to " + user.getEmail(), Toast.LENGTH_LONG).show(); 
                                                    
                                                    String userId = user.getUid();
                                                    Map<String, Object> userData = new HashMap<>(); 
                                                    userData.put("name", name);
                                                    userData.put("mobile", mobile);
                                                    userData.put("email", email);
                                                    userData.put("walletBalance", 0.0); 
                                                    mDatabase.child(userId).setValue(userData)
                                                            .addOnSuccessListener(aVoid -> {

                                                                finish(); 
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(RegisterActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                
                                                            });
                                                } else {
                                                    Toast.makeText(RegisterActivity.this,
                                                            "Failed to send email.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                
                            }
                        } else {
                            
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(RegisterActivity.this, "Email already in use.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "registration failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}