package com.example.notesapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.notesapp.R;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.User;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class Profile extends AppCompatActivity {

    SharedPreferences sp;
    private static final String SHARED_PREF = "myPref";
    private static final String KEY_ID = "id";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IMAGE = "image";

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_ADD_IMAGE = 2;

    private String username;
    private String pfp;
    private ImageView imageProfile;
    private Integer no_notes = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sp = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        username = sp.getString(KEY_USER, null);
        pfp = sp.getString(KEY_IMAGE, "");

        TextView user = findViewById(R.id.textViewUsername);
        user.setText(username);

        imageProfile = findViewById(R.id.imageViewProfile);
        if(sp.getString(KEY_IMAGE, "") != ""){
            imageProfile.setImageBitmap(BitmapFactory.decodeFile(sp.getString(KEY_IMAGE, "")));
        }else{
            imageProfile.setImageResource(R.drawable.profile_pic);
        }

        TextView editProfile = findViewById(R.id.buttonEditProfile);
        editProfile.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            Profile.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }else{
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if(intent.resolveActivity(getPackageManager()) != null){
                        startActivityForResult(intent, REQUEST_CODE_ADD_IMAGE);
                    }
                }
            }
        });

        TextView num_notes = findViewById(R.id.NumNotes_2);
        TextView num_fre = findViewById(R.id.NumFre);

        getNumNotes(num_notes);
        getNumFriends(num_fre);

    }

    private void getNumFriends(TextView num_fre) {
        @SuppressLint("StaticFieldLeak")
        class GetNotesNum extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).friendDao().getFriendsNum(sp.getInt(KEY_ID, 0));
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(Integer freNum) {
                super.onPostExecute(freNum);
                num_fre.setText(String.valueOf(freNum));
            }
        }
        new GetNotesNum().execute();
    }

    // On Activity Result, Set image bitmap on the view after choosing an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try{
                        InputStream inStr = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inStr);
                        imageProfile.setImageBitmap(bitmap);
                        String imagePath = getImagePathFromUri(selectedImageUri);

                        User user = new User();

                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(KEY_IMAGE, imagePath);
                        editor.apply();

                        user.setUserId(sp.getInt(KEY_ID, 0));
                        user.setUsername(sp.getString(KEY_USER, null));
                        user.setSessionToken(sp.getString(KEY_TOKEN, null));
                        user.setPasswordHash(sp.getString(KEY_PASS, null));
                        user.setProfileImage(sp.getString(KEY_IMAGE, null));

                        @SuppressLint("StaticFieldLeak")
                        class UpdateUser extends AsyncTask<Void, Void, Void> {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                NoteDatabase.getNoteDatabase(getApplicationContext()).userDao().insertUser(user);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void unused) {
                                super.onPostExecute(unused);
                            }
                        }
                        new UpdateUser().execute();

                    }catch (Exception exception){
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getImagePathFromUri(Uri contentUri){

        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);

        if(cursor == null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }

        return filePath;
    }

    private void getNumNotes(TextView num_notes) {

        @SuppressLint("StaticFieldLeak")
        class GetNotesNum extends AsyncTask<Void, Void, Integer> {

            @Override
            protected Integer doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().getNumNotes(sp.getInt(KEY_ID, 0));
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(Integer notesNum) {
                super.onPostExecute(notesNum);
                num_notes.setVisibility(View.VISIBLE);
                num_notes.setText(String.valueOf(notesNum));
            }
        }
        new GetNotesNum().execute();

    }
}
