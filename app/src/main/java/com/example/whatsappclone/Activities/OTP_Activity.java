package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.databinding.ActivityOtpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTP_Activity extends AppCompatActivity {

    ActivityOtpBinding binding;
    FirebaseAuth mAuth;
    String OTP;
    PhoneAuthProvider.ForceResendingToken token;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        String phoneNumber = getIntent().   getStringExtra("phoneNumber");
        String resendToken = getIntent().getStringExtra("resendToken");
        String OTP = getIntent().getStringExtra("OTP");

        dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying OTP");
        dialog.setCancelable(false);

        getSupportActionBar().hide();

        binding.textView2.setText("Verify "+phoneNumber);

        binding.continueBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String typedOTP = binding.phoneET.getText().toString().trim();
                
                if(typedOTP.length()==6){
                    dialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(OTP,typedOTP);
                    signInWithPhoneAuthCredential(credential);
                }else{
                    Toast.makeText(OTP_Activity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                }
                
            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
//                            Toast.makeText(OTP_Activity.this, "Auth Successfully", Toast.LENGTH_SHORT).show();
//                            dialog.dismiss();
                            startActivity(new Intent(OTP_Activity.this,SetUpProfileActivity.class));
                            finishAffinity();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Toast.makeText(OTP_Activity.this, "Auth Failed: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

}