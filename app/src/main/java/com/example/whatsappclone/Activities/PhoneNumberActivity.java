package com.example.whatsappclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.databinding.ActivityPhoneNumberBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth mAuth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth=FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP");
        dialog.setCancelable(false);

        getSupportActionBar().hide();

        if(mAuth.getCurrentUser()!=null){
            Intent intent = new Intent(PhoneNumberActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }


        binding.continueBT.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {


                String phoneNumber = binding.phoneET.getText().toString().trim();

//                Toast.makeText(PhoneNumberActivity.this, "NO: "+phoneNumber, Toast.LENGTH_SHORT).show();

                if(phoneNumber.length()==10) {
                    dialog.show();

                    phoneNumber="+91"+phoneNumber;

                    String finalPhoneNumber = phoneNumber;
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneNumberActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                        @Override
                                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                                            // This callback will be invoked in two situations:
                                            // 1 - Instant verification. In some cases the phone number can be instantly
                                            //     verified without needing to send or enter a verification code.
                                            // 2 - Auto-retrieval. On some devices Google Play services can automatically
                                            //     detect the incoming verification SMS and perform verification without
                                            //     user action.
//                                            Toast.makeText(PhoneNumberActivity.this, "onVerificationCompleted", Toast.LENGTH_SHORT).show();

                                            signInWithPhoneAuthCredential(credential);
                                        }

                                        @Override
                                        public void onVerificationFailed(FirebaseException e) {
                                            // This callback is invoked in an invalid request for verification is made,
                                            // for instance if the the phone number format is not valid.

                                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                                // Invalid request
                                                Log.d("Failed 1","onVerificationFailed: "+e.toString());
                                            } else if (e instanceof FirebaseTooManyRequestsException) {
                                                Log.d("Failed 2","onVerificationFailed: "+e.toString());
                                                // The SMS quota for the project has been exceeded
                                            }

                                            // Show a message and update the UI
                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String verificationId,
                                                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                            // The SMS verification code has been sent to the provided phone number, we
                                            // now need to ask the user to enter the code and then construct a credential
                                            // by combining the code with a verification ID.
//                                            Toast.makeText(PhoneNumberActivity.this, "onCodeSent", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            Intent intent = new Intent(PhoneNumberActivity.this, OTP_Activity.class);
                                            intent.putExtra("OTP",verificationId);
                                            intent.putExtra("resendToken",token);
                                            intent.putExtra("phoneNumber", finalPhoneNumber);
                                            startActivity(intent);

                                            // Save verification ID and resending token so we can use them later
                                        }
                                    })          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);


                }else{
                    Toast.makeText(PhoneNumberActivity.this, "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(mAuth.getCurrentUser()!=null){
//            startActivity(new Intent(PhoneNumberActivity.this, MainActivity.class));
//        }
//    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(PhoneNumberActivity.this, "Auth Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PhoneNumberActivity.this,MainActivity.class));
                        } else {
                            // Sign in failed, display a message and update the UI
                            Toast.makeText(PhoneNumberActivity.this, "Auth Failed: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

}