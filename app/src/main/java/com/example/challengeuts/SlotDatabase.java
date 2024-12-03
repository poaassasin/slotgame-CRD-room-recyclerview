package com.example.challengeuts;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SlotEntity.class}, version = 1)
public abstract class SlotDatabase extends RoomDatabase {
    public abstract SlotDAO getSlotDAO();
}
