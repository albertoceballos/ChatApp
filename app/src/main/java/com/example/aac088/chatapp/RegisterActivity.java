package com.example.aac088.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText display_name,email,password,confirm;
    private Button reg_btn;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference storeUserDefaultDataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        toolbar = (Toolbar) findViewById(R.id.reg_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        display_name = (EditText) findViewById(R.id.reg_display_name_et);
        email = (EditText) findViewById(R.id.reg_email_et);
        password = (EditText) findViewById(R.id.reg_pass_et);
        confirm = (EditText) findViewById(R.id.reg_confirm_pass_et);
        loadingBar = new ProgressDialog(this);

        reg_btn = (Button) findViewById(R.id.reg_create_acnt_btn);
        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name_str = display_name.getText().toString();
                String email_str = email.getText().toString();
                String password_str = password.getText().toString();
                String confirm_str = confirm.getText().toString();
                if(validateFields(display_name_str,email_str,password_str,confirm_str)){
                    register_user(display_name_str,email_str,password_str);
                }else{
                    Toast.makeText(RegisterActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean validateFields(String display, String email, String password, String confirm) {
        if(display.isEmpty()){
            Toast.makeText(RegisterActivity.this, "Display can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(email.isEmpty()){
            Toast.makeText(RegisterActivity.this, "Email can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(password.isEmpty()){
            Toast.makeText(RegisterActivity.this,"Password field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(confirm.isEmpty()){
            Toast.makeText(RegisterActivity.this,"Confirm password can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!password.equals(confirm)){
            Toast.makeText(RegisterActivity.this,"Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void register_user(final String display_name_str, String email_str, String password_str) {
        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please Wait...");
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email_str, password_str)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String device_token = FirebaseInstanceId.getInstance().getToken();
                            //Gets user ID
                            String current_user_id = mAuth.getCurrentUser().getUid();

                            //Initializes the instance of Firebase
                            storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                            //Store Data to FirebaseDB
                            storeUserDefaultDataReference.child("user_name").setValue(display_name_str);
                            storeUserDefaultDataReference.child("user_status").setValue("Hey there, I am using BoostChat!");
                            storeUserDefaultDataReference.child("device_token").setValue(device_token);
                            storeUserDefaultDataReference.child("user_image").setValue("default_img_user");
                            storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Intent mainIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        }
                                    });
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        else{
                            Toast.makeText(RegisterActivity.this, "Unable to register.",
                                    Toast.LENGTH_LONG).show();
                        }
                        loadingBar.dismiss();
                        // ...
                    }
                });

    }
}
