package com.example.abdlkdr.wowzasample.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.abdlkdr.wowzasample.Activity.MainActivity;
import com.example.abdlkdr.wowzasample.Model.User;
import com.example.abdlkdr.wowzasample.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdlkdr on 6.03.2018.
 */

public class RecyclerViewUserAdapter extends RecyclerView.Adapter<RecyclerViewUserAdapter.UserViewHolder> {
    List<User> mUsers = new ArrayList<>();
    User toUser = new User();
    static Context mContext;

    public RecyclerViewUserAdapter(Context context, ArrayList<User> users) {
        mContext = context;
        mUsers = users;
    }


    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, statusTextView;
        ImageView profilePictureImageView;

        public UserViewHolder(View itemView) {
            super(itemView);
            bindView(itemView);
            mContext = itemView.getContext();
        }

        public void bindView(View view) {
            usernameTextView = (TextView) view.findViewById(R.id.usernameTextView);
            statusTextView = (TextView) view.findViewById(R.id.statusTextView);
            profilePictureImageView = (ImageView) view.findViewById(R.id.personImageView);
        }
    }

    @Override
    public RecyclerViewUserAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_user_list, parent, false);
        UserViewHolder userViewHolder = new UserViewHolder(view);
        return userViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewUserAdapter.UserViewHolder holder, final int position) {
        final User user = mUsers.get(position);
        holder.statusTextView.setText(user.getStatus());
        holder.usernameTextView.setText("@"+user.getUsername());
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public User getUser (int position){
        return mUsers.get(position);
    }


}
