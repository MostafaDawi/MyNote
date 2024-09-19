package com.example.notesapp.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.R;
import com.example.notesapp.database.NoteDatabase;
import com.example.notesapp.entities.Friends;
import com.example.notesapp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AlreadyBoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inTitle, inSubtitle, inText;
    private TextView dateTime;
    private ImageView imageDone;
    private ImageView imageNote;
    private TextView textWebUrl;
    private LinearLayout layoutWebUrl;

    private String selectedNoteColor;
    private String imagePath;
    private String temp_name = "";
    private View viewSubtitleIndicator;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_ADD_IMAGE = 2;

    AlertDialog dialogAddUrl;
    AlertDialog dialogDeleteNote;
    AlertDialog dialogShareNote;

    private Note alreadyCreatedNote;

    private List<Integer> friends_ids = new ArrayList<>();
    private List<String> friends_names = new ArrayList<>();

    SharedPreferences sp;
    private static final String SHARED_PREF = "myPref";
    private static final String KEY_ID = "id";
    private static final String DATA_SHARING = "data_sharing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        checkPermissions();

        ImageView imageBack = findViewById(R.id.imageBack);

        sp = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);

        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inTitle = findViewById(R.id.inputNoteTitle);
        inSubtitle = findViewById(R.id.inputNoteSubtitle);
        inText = findViewById(R.id.inputNote);
        dateTime = findViewById(R.id.textDateTime);
        viewSubtitleIndicator = findViewById(R.id.viewSubIndicator);
        imageNote = findViewById(R.id.imageNote);
        textWebUrl = findViewById(R.id.textWebURL);
        layoutWebUrl = findViewById(R.id.layoutWebURL);

        dateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        imageDone = findViewById(R.id.imageSave);
        imageDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        selectedNoteColor = "#333333";
        imagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            alreadyCreatedNote = (Note) getIntent().getSerializableExtra("note");
            selectedNoteColor = alreadyCreatedNote.getColor();
            setViewOrUpdateNote();
        }

        findViewById(R.id.deleteURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textWebUrl.setText(null);
                layoutWebUrl.setVisibility(View.GONE);
                findViewById(R.id.deleteURL).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.deleteImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.deleteImage).setVisibility(View.GONE);
                imagePath = "";
            }
        });

        initMisc();
        setViewSubtitleIndicatorColor();
    }

    // Check if Note is already created
    private void setViewOrUpdateNote() {
        inTitle.setText(alreadyCreatedNote.getTitle());
        inSubtitle.setText(alreadyCreatedNote.getSubtitle());
        inText.setText(alreadyCreatedNote.getNoteText());
        dateTime.setText(alreadyCreatedNote.getDateTime());
        //Toast.makeText(this, "User ID is " + sp.getInt(KEY_ID, 0), Toast.LENGTH_SHORT).show();

        if (alreadyCreatedNote.getImagePath() != null && !alreadyCreatedNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyCreatedNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            imagePath = alreadyCreatedNote.getImagePath();
            findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
        }

        if (alreadyCreatedNote.getWebLink() != null && !alreadyCreatedNote.getWebLink().trim().isEmpty()) {
            textWebUrl.setText(alreadyCreatedNote.getWebLink());
            layoutWebUrl.setVisibility(View.VISIBLE);
            findViewById(R.id.deleteURL).setVisibility(View.VISIBLE);
        }
        fetchFriends();
    }

    // Save the Note in the database
    private void saveNote() {
        if (inTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note Title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (inSubtitle.getText().toString().trim().isEmpty() &&
                inText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inTitle.getText().toString());
        note.setSubtitle(inSubtitle.getText().toString());
        note.setNoteText(inText.getText().toString());
        note.setDateTime(dateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(imagePath);
        note.setUserId(sp.getInt(KEY_ID, 0));

        if (layoutWebUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(textWebUrl.getText().toString());
        }

        if (alreadyCreatedNote != null) {
            note.setId(alreadyCreatedNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                selectedNoteColor = "#333333";
                finish();
            }
        }

        new SaveNoteTask().execute();

    }

    // Initialize the views and layout
    private void initMisc() {

        final LinearLayout layoutMisc = findViewById(R.id.layoutMisc);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMisc);

        layoutMisc.findViewById(R.id.textMisc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != bottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(bottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(bottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        ImageView imageColor1 = findViewById(R.id.imageColor1);
        ImageView imageColor2 = findViewById(R.id.imageColor2);
        ImageView imageColor3 = findViewById(R.id.imageColor3);
        ImageView imageColor4 = findViewById(R.id.imageColor4);
        ImageView imageColor5 = findViewById(R.id.imageColor5);

        layoutMisc.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#edae3d";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#ea5654";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#3a62dc";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setViewSubtitleIndicatorColor();
            }
        });

        layoutMisc.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#141711";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setViewSubtitleIndicatorColor();
            }
        });

        if (alreadyCreatedNote != null && alreadyCreatedNote.getColor() != null && !alreadyCreatedNote.getColor().trim().isEmpty()) {
            switch (alreadyCreatedNote.getColor()) {
                case "#333333":
                    layoutMisc.findViewById(R.id.viewColor1).performClick();
                    break;
                case "#fdbe3d":
                    layoutMisc.findViewById(R.id.viewColor2).performClick();
                    break;
                case "#ff4842":
                    layoutMisc.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3a52fc":
                    layoutMisc.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#000000":
                    layoutMisc.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }

        layoutMisc.findViewById(R.id.layoutAddImageFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(bottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        });

        layoutMisc.findViewById(R.id.layoutAddUrlFrame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        if (alreadyCreatedNote != null) {
            layoutMisc.findViewById(R.id.layoutDeleteNote).setVisibility(View.VISIBLE);
            layoutMisc.findViewById(R.id.layoutShareNote).setVisibility(View.VISIBLE);
            if(sp.getBoolean(DATA_SHARING, true)){
                layoutMisc.findViewById(R.id.layoutShareNoteFrame).setVisibility(View.VISIBLE);
            }else{
                layoutMisc.findViewById(R.id.layoutShareNoteFrame).setVisibility(View.GONE);
            }

            layoutMisc.findViewById(R.id.layoutDeleteNoteFrame).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });

            layoutMisc.findViewById(R.id.layoutShareNoteFrame).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showShareNoteDialog();
                }
            });

            layoutMisc.findViewById(R.id.layoutExportNoteFrame).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    generatePDF(alreadyCreatedNote);
                }
            });
        }
    }

    // Delete Note Dialog
    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layoutDeleteNoteContainer)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();

            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.textYes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    @SuppressLint("StaticFieldLeak")
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().deleteNote(alreadyCreatedNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void unused) {
                            super.onPostExecute(unused);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });
            view.findViewById(R.id.textNo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            });
        }
        dialogDeleteNote.show();
    }

    // Share Note Dialog
    private void showShareNoteDialog() {
        String[] dialog_names;
        boolean[] dialog_checks;
        ArrayList<Integer> toSend = new ArrayList<>();

        dialog_names = new String[friends_names.size()];
        dialog_names = friends_names.toArray(dialog_names);
        dialog_checks = new boolean[friends_names.size()];

        if (dialogShareNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            builder.setTitle("Friends Available");
            builder.setMultiChoiceItems(dialog_names, dialog_checks, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                    if (isChecked) {
                        if (!toSend.contains(which)) {
                            toSend.add(which);
                        } else {
                            toSend.remove(which);
                        }
                    }
                }

            });
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    for (int i = 0; i < toSend.size(); i++) {
                        insertNoteIntoUser(friends_ids.get(toSend.get(i)));
                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialogShareNote = builder.create();
        }
        dialogShareNote.show();
    }

    // Set Note Color
    private void setViewSubtitleIndicatorColor() {
        GradientDrawable gd = (GradientDrawable) viewSubtitleIndicator.getBackground();
        gd.setColor(Color.parseColor(selectedNoteColor));
    }

    // Method for selecting image
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_ADD_IMAGE);
        }
    }

    // Providing permission so that the system can access the storage
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // On Activity Result, Set image bitmap on the view after choosing an image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inStr = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inStr);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.deleteImage).setVisibility(View.VISIBLE);
                        imagePath = getImagePathFromUri(selectedImageUri);

                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // Get image path from URI
    private String getImagePathFromUri(Uri contentUri) {

        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);

        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }

        return filePath;
    }

    // Dialog for adding URL
    private void showAddURLDialog() {
        if (dialogAddUrl == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(CreateNoteActivity.this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layoutAddUrlContainer)
            );
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURL = view.findViewById(R.id.inputURL);
            inputURL.requestFocus();

            view.findViewById(R.id.textAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURL.getText().toString()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter a valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textWebUrl.setText(inputURL.getText().toString());
                        layoutWebUrl.setVisibility(View.VISIBLE);
                        dialogAddUrl.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddUrl.dismiss();
                }
            });
        }
        dialogAddUrl.show();
    }

    // Send the shared note to the checked User
    private void insertNoteIntoUser(int userId) {
        class ShareIntoUser extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                final Note note = new Note();
                note.setTitle(inTitle.getText().toString());
                note.setSubtitle(inSubtitle.getText().toString());
                note.setNoteText(inText.getText().toString());
                note.setDateTime(dateTime.getText().toString());
                note.setColor(selectedNoteColor);
                note.setImagePath(imagePath);
                note.setUserId(userId);
                NoteDatabase.getNoteDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
            }
        }
        new ShareIntoUser().execute();
    }

    // Fetch the added friends' IDs into list
    private void fetchFriends() {

        class GetFriends extends AsyncTask<Void, Void, List<Integer>> {

            @Override
            protected List<Integer> doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).friendDao().getFriends(sp.getInt(KEY_ID, 0));
            }

            @Override
            protected void onPostExecute(List<Integer> friends) {
                super.onPostExecute(friends);
                friends_ids = friends;
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                for(Integer i : friends_ids){
                    fetchFriendsNames(i);
                }
            }
        }
        new GetFriends().execute();
    }

    // Fetch the added friends' Names into list
    private void fetchFriendsNames(Integer userId) {

        class GetFriendsNames extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                return NoteDatabase.getNoteDatabase(getApplicationContext()).userDao().getUsernameById(userId);
            }

            @Override
            protected void onPostExecute(String friend) {
                super.onPostExecute(friend);
                friends_names.add(friend);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
            }
        }
        new GetFriendsNames().execute();
    }

    // Check permission to write files to external storage
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    // Generate and Export a PDF file containing the information of a note
    private void generatePDF(Note note) {
        PdfDocument document = new PdfDocument();
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(24);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);

        Paint subtitlePaint = new Paint();
        subtitlePaint.setColor(Color.DKGRAY);
        subtitlePaint.setTextSize(18);
        subtitlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        subtitlePaint.setTextAlign(Paint.Align.CENTER);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(14);
        textPaint.setTextAlign(Paint.Align.LEFT);

        Paint datePaint = new Paint();
        datePaint.setColor(Color.GRAY);
        datePaint.setTextSize(12);
        datePaint.setTextAlign(Paint.Align.RIGHT);

        Paint linkPaint = new Paint();
        linkPaint.setColor(Color.GRAY);
        linkPaint.setTextSize(15);
        linkPaint.setUnderlineText(true);
        linkPaint.setTextAlign(Paint.Align.CENTER);

        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        int yPos = margin;
        int lineHeight = (int) (textPaint.descent() - textPaint.ascent());

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw title
        canvas.drawText(note.getTitle(), pageWidth / 2, yPos, titlePaint);
        yPos += 30;

        // Draw subtitle
        canvas.drawText(note.getSubtitle(), pageWidth / 2, yPos, subtitlePaint);
        yPos += 30;

        // Draw date
        canvas.drawText(note.getDateTime(), pageWidth - margin, yPos, datePaint);
        yPos += 20;

        // Draw image if available
        String imagePath = note.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                int scaledWidth = pageWidth - 2 * margin;
                int scaledHeight = (bitmap.getHeight() * scaledWidth) / bitmap.getWidth();
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, false);

                if (yPos + scaledHeight > pageHeight - margin) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPos = margin;
                }
                canvas.drawBitmap(scaledBitmap, margin, yPos, null);
                yPos += scaledHeight + 20;
            } else {
                Toast.makeText(this, "Failed to load image from " + imagePath, Toast.LENGTH_LONG).show();
            }
        }

        // Draw weblink if available
        String webLink = note.getWebLink();
        if (webLink != null && !webLink.isEmpty()) {
            canvas.drawText(webLink, pageWidth/2, yPos, linkPaint);
            yPos += lineHeight + 20;
        }

        // Draw note text
        String[] lines = note.getNoteText().split("\n");
        for (String line : lines) {
            while (line.length() > 0) {
                int length = textPaint.breakText(line, true, pageWidth - 2 * margin, null);
                canvas.drawText(line.substring(0, length), margin, yPos, textPaint);
                line = line.substring(length);
                yPos += lineHeight;

                if (yPos > pageHeight - margin) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPos = margin;
                }
            }
        }

        document.finishPage(page);

        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_LONG).show();
                return;
            }
        }

        File filePath = new File(directory, note.getTitle() + ".pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(this, "PDF saved to " + filePath, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        document.close();
    }
}