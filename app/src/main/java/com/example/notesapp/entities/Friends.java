package com.example.notesapp.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "friends",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "userId",
                        childColumns = "userIdSender",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.NO_ACTION),
                @ForeignKey(entity = User.class,
                        parentColumns = "userId",
                        childColumns = "userIdReceiver",
                        onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.NO_ACTION)
        })
public class Friends implements Serializable {

        @PrimaryKey(autoGenerate = true)
        private int id;

        @NonNull
        @ColumnInfo(name = "userIdSender")
        private int userIdSender;

        @NonNull
        @ColumnInfo(name = "userIdReceiver")
        private int userIdReceiver;

        // Getters and setters for all fields
        public int getId() {
                return id;
        }

        public void setId(int id) {
                this.id = id;
        }

        public int getUserIdSender() {
                return userIdSender;
        }

        public void setUserIdSender(int userIdSender) {
                this.userIdSender = userIdSender;
        }

        public int getUserIdReceiver() {
                return userIdReceiver;
        }

        public void setUserIdReceiver(int userIdReceiver) {
                this.userIdReceiver = userIdReceiver;
        }
}
