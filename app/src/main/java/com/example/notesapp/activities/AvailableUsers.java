package com.example.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.notesapp.R;
import com.example.notesapp.adapters.NotesAdapter;
import com.example.notesapp.adapters.UsersAdapter;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.User;
import com.example.notesapp.listeners.UsersListener;

import java.util.ArrayList;
import java.util.List;

public class AvailableUsers extends AppCompatActivity implements UsersListener {

    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_UPDATE_USER = 2;
    private static final int REQUEST_CODE_SHOW_USER = 3;

    SharedPreferences sp;
    private static final String SHARED_PREF = "myPref";
    private static final String KEY_ID = "id";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IMAGE = "image";

    private String token;
    private String username;
    private int userid;

    private RecyclerView userRecycleView;
    private List<User> userList;
    private UsersAdapter usersAdapter;

    private int userClickedPosition = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //Creating Shared Preferences to save user credentials
        sp = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        token = sp.getString(KEY_TOKEN, null);
        username = sp.getString(KEY_USER, null);
        userid = sp.getInt(KEY_ID, 0);


        userRecycleView = findViewById(R.id.recyclerViewUsers);
        userRecycleView.setLayoutManager(
                new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        );

        userList = new ArrayList<>();
        usersAdapter = new UsersAdapter(userList, this);
        userRecycleView.setAdapter(usersAdapter);

        getAllUsers(REQUEST_CODE_SHOW_USER);
    }


    // Fetch all the notes from the database
    private void getAllUsers(final int requestCode) {

        @SuppressLint("StaticFieldLeak")
        class GetUsers extends AsyncTask<Void, Void, List<User>> {

            @Override
            protected List<User> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).userDao().getAllUsers(userid);
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<User> users) {
                super.onPostExecute(users);
                if (requestCode == REQUEST_CODE_SHOW_USER) {
                    userList.addAll(users);
                    usersAdapter.notifyDataSetChanged();
                }
            }
        }
        new GetUsers().execute();
    }
    // When the user is clicked, start UserDetails class and set the views to the given data
    @Override
    public void onUserClicked(User user, int position) {
        userClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), UserDetails.class);
        intent.putExtra("isViewUser", true);
        intent.putExtra("available_user", user);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_USER);
    }
}
