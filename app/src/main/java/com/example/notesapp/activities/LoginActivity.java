package com.example.notesapp.activities;

import static org.mindrot.jbcrypt.BCrypt.gensalt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notesapp.R;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.User;

import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_REGISTER = 2;
    private final static int REQUEST_CODE_LOGIN = 1;

    SharedPreferences sp;
    private static final String SHARED_PREF = "myPref";
    private static final String KEY_ID = "id";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IMAGE = "image";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login_screen);

        TextView loginButton = findViewById(R.id.loginButton);
        TextView regButton = findViewById(R.id.registerAct);

        sp = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        String username = sp.getString(KEY_USER, null);

        if(username != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        if(getIntent().getBooleanExtra("UserCreated", false)){
            Toast.makeText(this, "User successfully added!", Toast.LENGTH_SHORT).show();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser();
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), RegisterActivity.class),
                        REQUEST_CODE_REGISTER);
            }
        });
    }

    // Search for user in database
    private boolean searchUser(){

        TextView username = findViewById(R.id.textUser);
        TextView password = findViewById(R.id.textPass);

        String user = username.getText().toString().trim();
        String pass = password.getText().toString();
        String hashedPass = BCrypt.hashpw(pass, BCrypt.gensalt());


        if(!user.isEmpty() && !hashedPass.isEmpty()){

            @SuppressLint("StaticFieldLeak")
            class SearchUser extends AsyncTask<Void, Void, User>{

                @Override
                protected User doInBackground(Void... voids) {
                    Log.d("HashedPassLog", ""+BCrypt.checkpw(pass, hashedPass));
                    User u = NoteDatabase.getNoteDatabase(getApplicationContext()).userDao().getUserByUsername(user);
                    if(u != null){
                        if(BCrypt.checkpw(pass, u.getPasswordHash())){
                            return u;
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(User user) {
                    super.onPostExecute(user);
                    if(user != null){
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt(KEY_ID, user.getUserId());
                        editor.putString(KEY_USER, user.getUsername());
                        editor.putString(KEY_PASS, user.getPasswordHash());
                        editor.putString(KEY_TOKEN, user.getSessionToken());
                        editor.putString(KEY_IMAGE, user.getProfileImage());
                        editor.apply();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        onUserNotFound();
                    }
                }
            }
            new SearchUser().execute();
        }else{
            Toast.makeText(this, "Please enter your credentials", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // Toast if user isn't found
    private void onUserNotFound() {
        // Handle case when user is not found (e.g., display error message)
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
    }

}
