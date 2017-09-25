package com.example.aac088.chatapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar settings_toolbar;
    private CircleImageView settingsDisplayImage;
    private TextView settingsDisplayName;
    private TextView settingsDisplayStatus;
    private Button settingsChangeProfileImage, settingsChangeStatus;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;

    private final static int Gallery_PICK =1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings_toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(settings_toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        String online_user_id = mAuth.getCurrentUser().getUid();
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);

        settingsDisplayImage = (CircleImageView) findViewById(R.id.settings_profile_image_circlimageview);
        settingsDisplayName = (TextView) findViewById(R.id.settings_username_txtvw);
        settingsDisplayStatus = (TextView) findViewById(R.id.settings_user_status_txtvw);
        settingsChangeProfileImage = (Button) findViewById(R.id.settings_change_profile_img_btn);
        settingsChangeStatus = (Button) findViewById(R.id.settings_change_status_btn);

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name= dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();
                String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                settingsDisplayName.setText(name);
                settingsDisplayStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settingsChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        settingsChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_PICK);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_PICK && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(this);
        }
    }
}
