package com.example.aac088.chatapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View mainView;
    private RecyclerView chatsList;
    private DatabaseReference friendsReference;
    private DatabaseReference userReference;
    private FirebaseAuth mAuth;

    private  String online_user_id;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();

        online_user_id = mAuth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatsList.setLayoutManager(linearLayoutManager);

        chatsList = (RecyclerView) mainView.findViewById(R.id.chats_list);
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder>(
                Chats.class, R.layout.all_users_display_layout,ChatsFragment.ChatsViewHolder.class,friendsReference
        ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, int position) {

                final String list_user_id = getRef(position).getKey();

                userReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                        String userStatus = dataSnapshot.child("user_status").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online_status = dataSnapshot.child("online").getValue().toString();

                            viewHolder.setUserOnline(online_status);
                        }

                        viewHolder.setUsername(userName);
                        viewHolder.setThumbImage(thumb_image, getContext());
                        viewHolder.setUserStatus(userStatus);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(dataSnapshot.child("online").exists()){
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id",list_user_id);
                                    chatIntent.putExtra("user_name",userName);
                                    startActivity(chatIntent);
                                }else{
                                    userReference.child(list_user_id).child("online").setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("visit_user_id",list_user_id);
                                                    chatIntent.putExtra("user_name",userName);
                                                    startActivity(chatIntent);
                                                }
                                            });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        chatsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_users_username);
            userNameDisplay.setText(username);
        }

        public void setThumbImage(final String thumbimage, final Context ctx) {
            final CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);

            Picasso.with(ctx).load(thumbimage).placeholder(R.drawable.default_img_user)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(thumb_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ctx).load(thumbimage).placeholder(R.drawable.default_img_user).into(thumb_image);
                        }
                    });
        }

        public void setUserOnline(String online_status) {
            ImageView onlineStatusView = (ImageView) mView.findViewById(R.id.online_status);

            if(online_status.equals("true")){
                onlineStatusView.setVisibility(View.VISIBLE);
            }else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserStatus(String userStatus) {
            TextView user_status = (TextView) mView.findViewById(R.id.all_users_status);
            user_status.setText(userStatus);
        }
    }
}
