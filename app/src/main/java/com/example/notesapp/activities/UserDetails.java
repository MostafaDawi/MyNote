package com.example.notesapp.activities;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notesapp.R;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.Friends;
import com.example.notesapp.entities.User;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Random;

public class UserDetails extends AppCompatActivity {

    SharedPreferences sp;
    private static final String SHARED_PREF = "myPref";
    private static final String KEY_ID = "id";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IMAGE = "image";

    private String username;
    private int userid;

    User receiver;
    Friends friend;

    TextView added_user;
    RoundedImageView added_user_image;
    TextView add_button;
    TextView remove_button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_details);

        //Creating Shared Preferences to save user credentials
        sp = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        username = sp.getString(KEY_USER, null);
        userid = sp.getInt(KEY_ID, 0);

        added_user = findViewById(R.id.UserDetailsUser);
        added_user_image = findViewById(R.id.UserDetailsImage);
        add_button = findViewById(R.id.AddUser);
        remove_button = findViewById(R.id.RemoveUser);

        if(getIntent().getBooleanExtra("isViewUser", false)){
            receiver = (User) getIntent().getSerializableExtra("available_user");
            friend = new Friends();
            friend.setUserIdSender(userid);
            friend.setUserIdReceiver(receiver.getUserId());
            checkFriend(receiver.getUserId());
        }

        added_user.setText(receiver.getUsername());
        if(receiver.getProfileImage() != ""){
            added_user_image.setImageBitmap(BitmapFactory.decodeFile(receiver.getProfileImage()));
        }else{
            added_user_image.setImageResource(R.drawable.profile_pic);
        }

        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                class AddFriend extends AsyncTask<Void, Void, Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NoteDatabase.getNoteDatabase(getApplicationContext()).friendDao().insertFriend(friend);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Toast.makeText(getApplicationContext(), "Added New Friend", Toast.LENGTH_SHORT).show();
                        add_button.setVisibility(View.GONE);
                        remove_button.setVisibility(View.VISIBLE);
                        //finish();
                    }
                }
                new AddFriend().execute();
            }
        });

        remove_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                class RemoveFriend extends AsyncTask<Void, Void, Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {
                        NoteDatabase.getNoteDatabase(getApplicationContext()).friendDao().deleteFriendByIds(userid, receiver.getUserId());
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Toast.makeText(getApplicationContext(), "Removed Friend", Toast.LENGTH_SHORT).show();
                        add_button.setVisibility(View.VISIBLE);
                        remove_button.setVisibility(View.GONE);
                        //finish();
                    }
                }
                new RemoveFriend().execute();
            }
        });
    }

    public void checkFriend(int friendId){

        class CheckFriend extends AsyncTask<Void, Void, Boolean>{

            @Override
            protected Boolean doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).friendDao().checkFriend(userid, friendId);
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(aBoolean.booleanValue() == true){
                    add_button.setVisibility(View.GONE);
                    remove_button.setVisibility(View.VISIBLE);
                }else{
                    add_button.setVisibility(View.VISIBLE);
                    remove_button.setVisibility(View.GONE);
                }
            }
        }
        new CheckFriend().execute();
    }
}
