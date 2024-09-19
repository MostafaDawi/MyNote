package com.example.notesapp.listeners;

import com.example.notesapp.entities.Note;
import com.example.notesapp.entities.User;

public interface UsersListener {
    void onUserClicked(User user, int position);
}
