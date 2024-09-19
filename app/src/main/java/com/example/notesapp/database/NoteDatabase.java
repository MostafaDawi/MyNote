package com.example.notesapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.notesapp.dao.FriendDao;
import com.example.notesapp.dao.NoteDao;
import com.example.notesapp.dao.UserDao;
import com.example.notesapp.entities.Friends;
import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.User;

@Database(entities = {Note.class, User.class, Friends.class}, version = 7, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase noteDatabase;

    public static synchronized NoteDatabase getNoteDatabase(Context context){

        if (noteDatabase == null) {
            noteDatabase = Room.databaseBuilder(
                    context,
                    NoteDatabase.class,
                    "notes_db"
            ).addMigrations(new Migration(6,7) {
                @Override
                public void migrate(@NonNull SupportSQLiteDatabase database) {
                    database.execSQL("DELETE FROM users WHERE users.username = 'sayfo'");
//                    database.execSQL("DROP TABLE IF EXISTS friends");
//
//                    database.execSQL("DROP TABLE IF EXISTS users");
//
//                    database.execSQL("DROP TABLE IF EXISTS notes");
//
//                    // Create the 'users' table
//                    database.execSQL("CREATE TABLE IF NOT EXISTS users (" +
//                            "userId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//                            "username TEXT UNIQUE NOT NULL, " +
//                            "password TEXT NOT NULL, " +
//                            "token TEXT, " +
//                            "profileImage TEXT" +
//                            ")");
//
//                    // Create the 'notes' table
//                    database.execSQL("CREATE TABLE IF NOT EXISTS notes (" +
//                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//                            "title TEXT, " +
//                            "date_time TEXT, " +
//                            "subtitle TEXT, " +
//                            "note_text TEXT, " +
//                            "color TEXT, " +
//                            "web_link TEXT, " +
//                            "image_path TEXT, " +
//                            "userId INTEGER NOT NULL, " +
//                            "FOREIGN KEY(userId) REFERENCES users(userId) ON DELETE CASCADE ON UPDATE CASCADE" +
//                            ")");
//
//                    // Create the 'friends' table
//                    database.execSQL("CREATE TABLE IF NOT EXISTS friends (" +
//                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
//                            "userIdSender INTEGER NOT NULL, " +
//                            "userIdReceiver INTEGER NOT NULL, " +
//                            "FOREIGN KEY(userIdSender) REFERENCES users(userId) ON DELETE CASCADE ON UPDATE NO ACTION, " +
//                            "FOREIGN KEY(userIdReceiver) REFERENCES users(userId) ON DELETE CASCADE ON UPDATE NO ACTION" +
//                            ")");
                }
            }).build();
        }
        return noteDatabase;
    }

    public abstract NoteDao noteDao();

    public abstract UserDao userDao();

    public abstract FriendDao friendDao();
}
