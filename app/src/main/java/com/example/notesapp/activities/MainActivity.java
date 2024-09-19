package com.example.notesapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.R;
import com.example.notesapp.adapters.NotesAdapter;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.User;
import com.example.notesapp.listeners.NotesListener;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {

    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private static final int REQUEST_CODE_UPDATE_NOTE = 2;
    private static final int REQUEST_CODE_SHOW_NOTE = 3;

    SharedPreferences sp;
    private static final String SHARED_PREF = "myPref";
    private static final String KEY_ID = "id";
    private static final String KEY_USER = "username";
    private static final String KEY_PASS = "password";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_BACKGROUND = "background";
    private static final String KEY_BACKGROUND_QUICK = "background_quick";

    private String token;
    private String username;

    private RecyclerView noteRecycleView;
    private ImageView pfp;
    private TextView pfp_text;
    private ConstraintLayout constraint;
    private LinearLayout llq;

    private List<Note> noteList;
    private NotesAdapter notesAdapter;

    private int noteClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creating Shared Preferences to save user credentials
        sp = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        token = sp.getString(KEY_TOKEN, null);
        username = sp.getString(KEY_USER, null);

        constraint = findViewById(R.id.mainAct);
        int defaultColor = getResources().getColor(R.color.colorPrimary);
        constraint.setBackgroundColor(sp.getInt(KEY_BACKGROUND, defaultColor));

        llq = findViewById(R.id.layoutQuickActions);
        int defaultColor_2 = getResources().getColor(R.color.colorLogin);
        llq.setBackgroundColor(sp.getInt(KEY_BACKGROUND_QUICK, defaultColor_2));
        Log.d("Color", sp.getInt(KEY_BACKGROUND, defaultColor)+"");

        //Check if user has logged in correctly
        if(token != null){
            //Toast.makeText(this, "Token is "+ token, Toast.LENGTH_SHORT).show();
        }else{
            //Toast.makeText(this, "Token is Unknown", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        findViewById(R.id.menuButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
                pfp = findViewById(R.id.profilePic);
                pfp_text = findViewById(R.id.profileText);
                pfp_text.setText(username);
                if(sp.getString(KEY_IMAGE, "") != ""){
                    pfp.setImageBitmap(BitmapFactory.decodeFile(sp.getString(KEY_IMAGE, "")));
                }else{
                    pfp.setImageResource(R.drawable.profile_pic);
                }
            }
        });

        // Navigation view
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.log_out){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.commit();
                    Toast.makeText(MainActivity.this, "Log Out Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                    return true;
                }
                if(id == R.id.profile){
                    //Toast.makeText(getApplicationContext(), "Profile is clicked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Profile.class));
                    if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                if(id == R.id.options){
                    //Toast.makeText(getApplicationContext(), "Profile is clicked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), Settings.class));
                    if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                if(id == R.id.available_user){
                    //Toast.makeText(getApplicationContext(), "Profile is clicked", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), AvailableUsers.class));
                    if(drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                return false;
            }
        });
        navView.setItemIconTintList(null);


        ImageView imageAddNoteMain = findViewById(R.id.imageAddNoteMain);
        ImageView changeView_1 = findViewById(R.id.imageChangeView_1);
        ImageView changeView_2 = findViewById(R.id.imageChangeView_2);



        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
            }
        });

        noteRecycleView = findViewById(R.id.notesRecyclerView);
        noteRecycleView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this);
        notesAdapter.setSpanCount(2);
        noteRecycleView.setAdapter(notesAdapter);

        changeView_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesAdapter.setSpanCount(1);
                noteRecycleView.setAdapter(notesAdapter);
                noteRecycleView.setLayoutManager(
                        new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                );
                changeView_1.setVisibility(View.GONE);
                changeView_2.setVisibility(View.VISIBLE);
            }
        });

        changeView_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesAdapter.setSpanCount(2);
                noteRecycleView.setAdapter(notesAdapter);
                noteRecycleView.setLayoutManager(
                        new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                );
                changeView_1.setVisibility(View.VISIBLE);
                changeView_2.setVisibility(View.GONE);
            }
        });

        getAllNotes(REQUEST_CODE_SHOW_NOTE, false);

        EditText inputSearch = findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size() != 0){
                    notesAdapter.searchNote(s.toString());
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Apply color in onResume to handle changes while the activity was paused
        int defaultColor = getResources().getColor(R.color.colorPrimary);  // use a sensible default color
        int defaultColor_2 = getResources().getColor(R.color.colorLogin);  // use a sensible default color
        int backgroundColor = sp.getInt(KEY_BACKGROUND, defaultColor);
        int backgroundColor_quick = sp.getInt(KEY_BACKGROUND_QUICK, defaultColor_2);

        TextView Title = findViewById(R.id.textMyNotes);

        if(backgroundColor != defaultColor){
            Title.setTextColor(getResources().getColor(R.color.colorPrimary));
        }else{
            Title.setTextColor(getResources().getColor(R.color.colorWhite));
        }

        constraint = findViewById(R.id.mainAct);
        llq = findViewById(R.id.layoutQuickActions);
        constraint.setBackgroundColor(backgroundColor);
        llq.setBackgroundColor(backgroundColor_quick);
        Log.d("MainActivity", "Color applied in onResume: " + backgroundColor);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);

        findViewById(R.id.menuButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
                pfp = findViewById(R.id.profilePic);
                pfp_text = findViewById(R.id.profileText);
                pfp_text.setText(username);
                if(sp.getString(KEY_IMAGE, "") != ""){
                    pfp.setImageBitmap(BitmapFactory.decodeFile(sp.getString(KEY_IMAGE, "")));
                }else{
                    pfp.setImageResource(R.drawable.profile_pic);
                }
            }
        });
    }

    // Fetch all the notes from the database
    private void getAllNotes(final int requestCode, final boolean isNoteDeleted){

        @SuppressLint("StaticFieldLeak")
        class GetNotes extends AsyncTask<Void, Void, List<Note>>{

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().getAllNotes(sp.getInt(KEY_ID, 0));
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if(requestCode == REQUEST_CODE_SHOW_NOTE){
                    noteList.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                    noteRecycleView.smoothScrollToPosition(0);
                }else if (requestCode == REQUEST_CODE_UPDATE_NOTE){
                    noteList.remove(noteClickedPosition);
                    if(isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            }
        }

        new GetNotes().execute();
    }

    // On Activity Result, after any modification
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getAllNotes(REQUEST_CODE_ADD_NOTE, false);
        }else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK){
            getAllNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
        }
    }

    // When the note is clicked, start CreateActivity class and set the views to the given data
    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        intent.putExtra("User", note.getUserId());
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }
}