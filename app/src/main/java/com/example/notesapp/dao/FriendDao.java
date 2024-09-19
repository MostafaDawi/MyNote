package com.example.notesapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.notesapp.entities.Friends;
import com.example.notesapp.entities.User;

import java.util.List;

@Dao
public interface FriendDao {

    @Query("SELECT userIdReceiver FROM friends WHERE friends.userIdSender = :userIdSender")
    List<Integer> getFriends(int userIdSender);

    @Query("SELECT COUNT(*) FROM friends WHERE friends.userIdSender = :userIdSender")
    Integer getFriendsNum(int userIdSender);

    @Query("SELECT * FROM friends WHERE friends.userIdSender = :userIdSender AND friends.userIdReceiver = :userIdReceiver")
    boolean checkFriend(int userIdSender, int userIdReceiver);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFriend(Friends friend);

    @Delete
    void deleteFriend(Friends friend);

    @Query("DELETE FROM friends WHERE userIdSender = :userIdSender AND userIdReceiver = :userIdReceiver")
    void deleteFriendByIds(int userIdSender, int userIdReceiver);

}
