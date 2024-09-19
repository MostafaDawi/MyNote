package com.example.notesapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.notesapp.entities.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE users.username = :uname AND users.password = :pass")
    User loginUser(String uname, String pass);

    @Query("SELECT * FROM users WHERE username = :uname")
    User getUserByUsername(String uname);

    @Query("SELECT * FROM users WHERE users.userId != :userId")
    List<User> getAllUsers(int userId);

    @Query("SELECT username FROM users WHERE users.userId = :userId")
    String getUsernameById(int userId);

    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteUserById(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Delete
    void deleteUser(User user);

    @Update
    void updateUser(User user);
}
