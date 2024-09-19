package com.example.notesapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.User;
import com.example.notesapp.listeners.NotesListener;
import com.example.notesapp.listeners.UsersListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;
import java.util.Timer;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private List<User> userList;
    private UsersListener usersListener;

    public UsersAdapter(List<User> userList, UsersListener usersListener) {
        this.userList = userList;
        this.usersListener = usersListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_container_user, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setUser(userList.get(position));
        holder.layoutUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersListener.onUserClicked(userList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder{

        TextView textUsername;
        LinearLayout layoutUsers;
        RoundedImageView imageUser;
        //ImageView imageNote;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUsername = itemView.findViewById(R.id.textUsername);
            layoutUsers = itemView.findViewById(R.id.layoutUsers);
            imageUser = itemView.findViewById(R.id.imageUserView);
        }

        void setUser(User user){
            textUsername.setText(user.getUsername());

            GradientDrawable gd = (GradientDrawable) layoutUsers.getBackground();
            gd.setColor(Color.parseColor("#333333"));

            if(user.getProfileImage()!= null){
                imageUser.setImageBitmap(BitmapFactory.decodeFile(user.getProfileImage()));
                imageUser.setVisibility(View.VISIBLE);
            }else{
                imageUser.setVisibility(View.GONE);
            }
        }
    }
}
