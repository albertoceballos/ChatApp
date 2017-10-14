package com.example.aac088.chatapp;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar chatToolbar;
    private String messageRecieverId,messageRecieverName;
    private TextView userNameTitle;
    private TextView userLastSeen;
    private CircleImageView userChatProfileImage;
    private DatabaseReference rootRef;
    private ImageButton sendMessageButton, selectImageButton;
    private EditText inputMessageText;
    private FirebaseAuth mAuth;
    private String messageSenderId;
    private RecyclerView userMessagesList;
    private final List<Messages> messageList = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;

    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();

        messageRecieverId = getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName = getIntent().getExtras().get("user_name").toString();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);

        userNameTitle = (TextView) findViewById(R.id.custom_bar_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        userChatProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        sendMessageButton = (ImageButton) findViewById(R.id.chat_send_message);
        selectImageButton = (ImageButton) findViewById(R.id.chat_select_image);

        inputMessageText = (EditText) findViewById(R.id.chat_input_message);

        messageAdapter = new MessageAdapter(messageList);


        userMessagesList = (RecyclerView) findViewById(R.id.messages_list_users);

        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);

        userMessagesList.setAdapter(messageAdapter);

        FetchMessages();

        userNameTitle.setText(messageRecieverName);

        rootRef.child("Users").child(messageRecieverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String online = dataSnapshot.child("online").getValue().toString();
                final String userThumb = dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.with(ChatActivity.this).load(userThumb).placeholder(R.drawable.default_img_user)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(userChatProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ChatActivity.this).load(userThumb).placeholder(R.drawable.default_img_user).into(userChatProfileImage);
                            }
                        });

                if(online.equals("true")){
                    userLastSeen.setText("online");
                }else{
                    LastSeenTime getTime = new LastSeenTime();
                    long last_seen = Long.parseLong(online);
                    String lastSeendDisplayTime = getTime.getTimeAgo(last_seen,getApplicationContext()).toString();
                    userLastSeen.setText(lastSeendDisplayTime);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

    }

    private void FetchMessages() {
        rootRef.child("Messages").child(messageSenderId).child(messageRecieverId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messageList.add(messages);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage() {
        String messageText = inputMessageText.getText().toString();

        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(ChatActivity.this,"Please Write your Message...",Toast.LENGTH_SHORT).show();
        }else{
            String message_sender_ref = "Messages/" + messageSenderId + "/"+messageRecieverId;
            String message_receiver_ref = "Messages/" + messageRecieverId + "/"+messageSenderId;
            DatabaseReference user_message_key = rootRef.child("Messages").child(messageSenderId).child(messageRecieverId).push();

            String message_push_id = user_message_key.getKey();

            Map messageTextBody = new HashMap();

            messageTextBody.put("message",messageText);
            messageTextBody.put("seen",false);
            messageTextBody.put("type","text");
            messageTextBody.put("time", ServerValue.TIMESTAMP);
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetailes = new HashMap();
            messageBodyDetailes.put(message_sender_ref + "/" + message_push_id,messageTextBody);
            messageBodyDetailes.put(message_receiver_ref + "/" + message_push_id,messageTextBody);

            rootRef.updateChildren(messageBodyDetailes, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("Chat_Log",databaseError.getMessage().toString());
                    }

                    inputMessageText.setText("");
                }
            });
        }
    }
}
