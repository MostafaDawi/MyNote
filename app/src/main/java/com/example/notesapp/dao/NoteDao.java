package com.example.notesapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.notesapp.entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes WHERE notes.userId = :userId ORDER BY id DESC")
    List<Note> getAllNotes(int userId);

    @Query("SELECT COUNT(*) FROM notes WHERE notes.userId = :userId")
    Integer getNumNotes(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);

}
