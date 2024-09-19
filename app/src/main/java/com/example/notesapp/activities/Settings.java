package com.example.notesapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceFragmentCompat;

import com.example.notesapp.PasswordValidator;
import com.example.notesapp.R;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.User;

import org.mindrot.jbcrypt.BCrypt;

public class Settings extends AppCompatActivity {

    SharedPreferences sp;
    private static final String SHARED_PREF = "myPref";
    private static final String KEY_ID = "id";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_BACKGROUND = "background";
    private static final String KEY_BACKGROUND_QUICK = "background_quick";
    private static final String SWITCH_STATE = "switch_state";
    private static final String DATA_SHARING = "data_sharing";

    TextView changePass;
    TextView deleteAcc;

    AlertDialog dialogDeleteAccount;
    AlertDialog dialogChangePassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        changePass = findViewById(R.id.button_change_password);
        deleteAcc = findViewById(R.id.button_delete_account);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        Switch switch_mode = findViewById(R.id.switch_colorMode);
        switch_mode.setChecked(sp.getBoolean(SWITCH_STATE, false));

        Switch switch_data_share = findViewById(R.id.switch_data_sharing);
        switch_data_share.setChecked(sp.getBoolean(DATA_SHARING, true));

        switch_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int color, color_2;
                if(isChecked){
                    color = getResources().getColor(R.color.colorWhiteGrad);
                    color_2 = getResources().getColor(R.color.colorWhite);
                    Log.d("ColorSet", "Color is White " + color);

                }else{
                    color = getResources().getColor(R.color.colorPrimary);
                    color_2 = getResources().getColor(R.color.colorLogin);
                    Log.d("ColorSet", "Color is Primary "+ color);
                }
                // Start TargetActivity with the color
                editor.putInt(KEY_BACKGROUND, color);
                editor.putInt(KEY_BACKGROUND_QUICK, color_2);
                editor.putBoolean(SWITCH_STATE, isChecked);
                editor.commit(); }
        });

        switch_data_share.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(DATA_SHARING, isChecked);
                editor.commit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void changePassword() {
        if (dialogChangePassword == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_change_pass,
                    (ViewGroup) findViewById(R.id.layoutChangePasswordContainer)
            );
            builder.setView(view);
            dialogChangePassword = builder.create();

            if (dialogChangePassword.getWindow() != null) {
                dialogChangePassword.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            EditText usernameEditText = view.findViewById(R.id.editUsername);
            EditText newPasswordEditText = view.findViewById(R.id.editNewPassword);

            view.findViewById(R.id.textSave).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = usernameEditText.getText().toString().trim();
                    String newPassword = newPasswordEditText.getText().toString().trim();

                    if (!username.isEmpty() && !newPassword.isEmpty()) {
                        @SuppressLint("StaticFieldLeak")
                        class ChangePasswordTask extends AsyncTask<Void, Void, Void> {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                User user = NoteDatabase.getNoteDatabase(getApplicationContext()).userDao()
                                        .getUserByUsername(username);

                                if (user != null) {
                                    String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                                    if(!PasswordValidator.validate(newPassword)){
                                        Toast.makeText(getApplicationContext(), "Password should be at least 8 characters, one uppercase, and one lowercase", Toast.LENGTH_SHORT).show();
                                    }else{
                                        user.setPasswordHash(newHashedPassword);
                                        NoteDatabase.getNoteDatabase(getApplicationContext()).userDao().updateUser(user);
                                        SharedPreferences.Editor editor = sp.edit();
                                        editor.putString(KEY_PASS, newHashedPassword);
                                        editor.commit();
                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(), "User is not found", Toast.LENGTH_SHORT).show();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void unused) {
                                super.onPostExecute(unused);
                                dialogChangePassword.dismiss();
                                Toast.makeText(Settings.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                        new ChangePasswordTask().execute();
                    }else{
                        Toast.makeText(Settings.this, "Username and new password cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogChangePassword.dismiss();
                }
            });
        }
        dialogChangePassword.show();
    }

    // Delete Note Dialog
    private void deleteAccount() {
        if (dialogDeleteAccount == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_account,
                    (ViewGroup) findViewById(R.id.layoutDeleteAccountContainer)
            );
            builder.setView(view);
            dialogDeleteAccount = builder.create();

            if (dialogDeleteAccount.getWindow() != null) {
                dialogDeleteAccount.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textYesAcc).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getNoteDatabase(getApplicationContext()).userDao()
                                    .deleteUserById(sp.getInt(KEY_ID, 0));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.clear();
                            editor.commit();
                            Toast.makeText(Settings.this, "Account Deleted Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textNoAcc).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteAccount.dismiss();
                }
            });
        }
        dialogDeleteAccount.show();
    }
}