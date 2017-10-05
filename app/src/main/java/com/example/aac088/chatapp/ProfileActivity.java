package com.example.aac088.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private Button sendRequestbtn, declineRequestbtn;
    private TextView visit_username,visit_status;
    private ImageView profile_image;

    private DatabaseReference usersReference;

    private String CURRENT_STATE;

    private DatabaseReference friendRequestReference;
    private FirebaseAuth mAuth;
    private String sender_user_id,reciever_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();



        sendRequestbtn = (Button) findViewById(R.id.profile_visit_send_request_btn);
        declineRequestbtn = (Button) findViewById(R.id.profile_visit_decline_request_btn);
        visit_username = (TextView) findViewById(R.id.profile_visit_username);
        visit_status = (TextView) findViewById(R.id.profile_visit_user_status);
        profile_image = (ImageView) findViewById(R.id.profile_visit_user_image);

        CURRENT_STATE = "not_friends";

        reciever_user_id = getIntent().getExtras().get("visit_user_id").toString();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        usersReference.child(reciever_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                visit_username.setText(name);
                visit_status.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_img_user).into(profile_image);

                friendRequestReference.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(reciever_user_id)){
                            String req_type = dataSnapshot.child(reciever_user_id).child("request_type").getValue().toString();

                            if(req_type.equals("sent")){
                                CURRENT_STATE = "request_sent";

                                sendRequestbtn.setText("Cancel Friend Request");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestbtn.setEnabled(false);

                if(CURRENT_STATE.equals("not_friends")){
                    SendFriendRequest();
                }

                if(CURRENT_STATE.equals("request_sent")){
                    CancelFriendRequest();
                }
            }
        });

        declineRequestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void CancelFriendRequest() {
        friendRequestReference.child(sender_user_id).child(reciever_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestReference.child(reciever_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendRequestbtn.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                sendRequestbtn.setText("Send Friend Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendFriendRequest() {
        friendRequestReference.child(sender_user_id).child(reciever_user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    friendRequestReference.child(reciever_user_id).child(sender_user_id).child("request_type").setValue("reciever").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendRequestbtn.setEnabled(true);
                                CURRENT_STATE="request_sent";
                                sendRequestbtn.setText("Cancel Friend Request");
                            }
                        }
                    });
                }
            }
        });
    }
}
