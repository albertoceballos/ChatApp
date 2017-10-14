package com.example.aac088.chatapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView friendList;
    private DatabaseReference friendsReference;
    private DatabaseReference userReference;
    private FirebaseAuth mAuth;

    private  String online_user_id;
    private View mainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView=inflater.inflate(R.layout.fragment_friends, container, false);

        friendList = (RecyclerView) mainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();

        online_user_id = mAuth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        friendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class, R.layout.all_users_display_layout,FriendsViewHolder.class,friendsReference
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends model, int position) {
                viewHolder.setDate(model.getDate());

                String list_user_id = getRef(position).getKey();

                userReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumb_image = dataSnapshot.child("user_thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            Boolean online_status = (Boolean) dataSnapshot.child("online").getValue();

                            viewHolder.setUserOnline(online_status);
                        }

                        viewHolder.setUsername(userName);
                        viewHolder.setThumbImage(thumb_image, getContext());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date){
            TextView sinceFriendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            sinceFriendsDate.setText(date);
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

        public void setUserOnline(Boolean online_status) {
            ImageView onlineStatusView = (ImageView) mView.findViewById(R.id.online_status);

            if(online_status == true){
                onlineStatusView.setVisibility(View.VISIBLE);
            }else{
                onlineStatusView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
