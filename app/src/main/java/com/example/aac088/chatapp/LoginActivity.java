package com.example.aac088.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private Button reg_but, log_but;
    private EditText email,password;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.log_usrn_edittxt);
        password = (EditText) findViewById(R.id.log_pass_edittxt);

        mAuth = FirebaseAuth.getInstance();

        loadingBar = new ProgressDialog(this);

        reg_but = (Button) findViewById(R.id.log_reg_but);
        reg_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regIntent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });
        log_but = (Button) findViewById(R.id.login_sign_but);
        log_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_str = email.getText().toString();
                String pass_str = password.getText().toString();

                LoginUserAccount(email_str,pass_str);
            }
        });
    }

    private void LoginUserAccount(String email_str, String pass_str) {
        if(email_str.isEmpty()){
            Toast.makeText(this,"Email can't be empty",Toast.LENGTH_SHORT).show();
        }
        if(pass_str.isEmpty()){
            Toast.makeText(this,"Password can't be empty",Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Signing into Account");
            loadingBar.setMessage("Please Wait...");
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email_str,pass_str).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Intent logIntent = new Intent(LoginActivity.this,MainActivity.class);
                        logIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//to prevent user from returning
                        startActivity(logIntent);
                        finish();
                    }else{
                        Toast.makeText(LoginActivity.this,"Wrong email and/or password. Try Again",Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                }
            });
        }
    }
}
