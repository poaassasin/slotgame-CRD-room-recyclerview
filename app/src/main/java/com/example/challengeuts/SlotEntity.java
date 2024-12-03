package com.example.challengeuts;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "slot")
public class SlotEntity {

    @ColumnInfo(name="slotID")
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name="image1")
    private int imageUri1;

    @ColumnInfo(name="image2")
    private int imageUri2;

    @ColumnInfo(name="image3")
    private int imageUri3;

    @ColumnInfo(name="tanggal")
    private String tanggal;

    @ColumnInfo(name="waktu")
    private String waktu;

    @Ignore
    public SlotEntity() {

    }

    public SlotEntity(int imageUri1, int imageUri2, int imageUri3, String tanggal,
                      String waktu) {
        this.id = 0;
        this.imageUri1 = imageUri1;
        this.imageUri2 = imageUri2;
        this.imageUri3 = imageUri3;
        this.tanggal = tanggal;
        this.waktu = waktu;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public int getImageUri1() {return imageUri1;}

    public void setImageUri1(int imageUri1) {this.imageUri1 = imageUri1;}

    public int getImageUri2() {return imageUri2;}

    public void setImageUri2(int imageUri2) {this.imageUri2 = imageUri2;}

    public int getImageUri3() {return imageUri3;}

    public void setImageUri3(int imageUri3) {this.imageUri3 = imageUri3;}

    public void setTanggal(String tanggal) {this.tanggal = tanggal;}

    public String getTanggal() {return tanggal;}

    public void setWaktu(String waktu) {this.waktu = waktu;}

    public String getWaktu() {return waktu;}
}
