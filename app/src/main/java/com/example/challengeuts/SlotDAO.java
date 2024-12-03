package com.example.challengeuts;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SlotDAO {

    @Insert
    public void addSlot(SlotEntity slot);

    @Delete
    public void deleteSlot(SlotEntity slot);

    @Query("select * from slot where waktu like:waktu limit 1")
    public SlotEntity findByWaktu(String waktu);

    @Query("DELETE FROM slot where slotID == :slotID")
    public void deleteById(int slotID);

    @Query("select * from slot")
    public List<SlotEntity> getAllSlot();

    @Query("select * from slot where slotID == :slotID")
    public SlotEntity getSlot(int slotID);

    @Query("select * from slot where slotID = (SELECT slotID from slot order by slotID DESC limit 1)")
    public SlotEntity getLatestSlot();

}
