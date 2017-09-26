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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private EditText status_input;
    private Button change_status_but;

    private DatabaseReference changeStatusRef;
    private FirebaseAuth mAuth;

    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        toolbar = (Toolbar) findViewById(R.id.status_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        status_input = (EditText) findViewById(R.id.change_status_et);
        change_status_but = (Button) findViewById(R.id.change_status_button);
        pd = new ProgressDialog(this);

        String old_status = getIntent().getExtras().get("user_status").toString();
        status_input.setText(old_status);

        mAuth = FirebaseAuth.getInstance();
        String user_id = mAuth.getCurrentUser().getUid();
        changeStatusRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);

        change_status_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_status = status_input.getText().toString();

                changeProfileStatus(new_status);
            }
        });
    }

    private void changeProfileStatus(String new_status) {
        if(new_status.isEmpty()){
            Toast.makeText(StatusActivity.this, "Please Write a Status", Toast.LENGTH_SHORT).show();
        }else{
            pd.setTitle("Change Profile Status");
            pd.setMessage("Please Wait...");
            pd.show();

            changeStatusRef.child("user_status").setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        pd.dismiss();
                        Intent settingsIntent = new Intent(StatusActivity.this,SettingsActivity.class);
                        startActivity(settingsIntent);

                        Toast.makeText(StatusActivity.this,"Profile Status Updated Successfully",Toast.LENGTH_SHORT).show();
                    }     else{
                        Toast.makeText(StatusActivity.this, "Error Occurred...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
