package com.example.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notesapp.PasswordValidator;
import com.example.notesapp.R;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.User;

import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    EditText confirm;

    TextView regButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register_screen);

        username = findViewById(R.id.textUserReg);
        password = findViewById(R.id.textPassReg);
        confirm = findViewById(R.id.confirmPass);

        regButton = findViewById(R.id.registerButton);

        ImageView imageBack = findViewById(R.id.imageBackLogin);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();
            }
        });
    }

    private void saveUser(){
        if(!username.getText().toString().trim().isEmpty() &&
                !password.getText().toString().trim().isEmpty() &&
                !confirm.getText().toString().trim().isEmpty()){

            if(!PasswordValidator.validate(password.getText().toString().trim())){
                Toast.makeText(getApplicationContext(), "Password should be at least 8 characters, one uppercase, and one lowercase", Toast.LENGTH_SHORT).show();
                return;
            }

            if(password.getText().toString().equals(confirm.getText().toString())){

                User user = new User();
                user.setUsername(username.getText().toString());
                user.setPasswordHash(BCrypt.hashpw(password.getText().toString(), BCrypt.gensalt()));
                Log.d("HashedPassReg", ""+BCrypt.hashpw(password.getText().toString(), BCrypt.gensalt()));
                user.setSessionToken(UUID.randomUUID().toString());
                user.setProfileImage("");

                @SuppressLint("StaticFieldLeak")
                class AddUser extends AsyncTask<Void, Void, Void>{
                    @Override
                    protected Void doInBackground(Void... voids) {
                        NoteDatabase.getNoteDatabase(getApplicationContext()).userDao().insertUser(user);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.putExtra("UserCreated", true);
                        startActivity(intent);
                        finish();
                    }
                }
                new AddUser().execute();
            }else{
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            Toast.makeText(this, "Required fields are empty", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}

