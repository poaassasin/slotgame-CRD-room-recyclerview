package com.example.challengeuts;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SlotItemAdapter.ItemCallbackListener {
    private Handler handler; // menangani pesan dari thread
    private ImageView slot1, slot2, slot3;
    private TextView resultText;
    private Button btStartStop;
    private Thread randomThread;
    private boolean[] terStopSlot = new boolean[3];// status slot apakah berhenti
    private int[] currentImages = new int[3];
    private int[] ImageRes = {
            R.drawable.grapes, R.drawable.apple, R.drawable.kiwi, R.drawable.cherry,
            R.drawable.strawberry, R.drawable.hati, R.drawable.banana
            , R.drawable.orange, R.drawable.diamond, R.drawable.seven
    };
    private boolean isRunning = false;

    private SlotDatabase slotDb;

    private ArrayList<SlotEntity> listData = new ArrayList<>();

    private int clickCount = 0;
    private RecyclerView rvList;

    private View placeholderContainer;

    private SlotItemAdapter slotAdapter;

    @Override
    // tempat menginisasi elemen UI dan handler untuk menangani pesan dari thread  ketika gambar slot diacak
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        slot1 = findViewById(R.id.slot1);
        slot2 = findViewById(R.id.slot2);
        slot3 = findViewById(R.id.slot3);
        btStartStop = findViewById(R.id.btnStartStop);
        resultText = findViewById(R.id.hasilSpin);
        this.rvList = this.findViewById(R.id.rvList); //menginisasi var rvList dengan layout recycler view di ID-nya
        this.placeholderContainer = findViewById(R.id.placeholderContainer); //menginisiasi placeholder recycler view apabila kosong
        this.rvList.setLayoutManager(new LinearLayoutManager(this)); //setting layout recycler view
        // dengan linear layout di halaman main activity
        this.rvList.setAdapter(this.slotAdapter); //setting adapter recycler view dengan class SlotItemAdapter

        btStartStop.setOnClickListener(new View.OnClickListener() { //tombol klik memulai dan berhenti
            @Override
            public void onClick(View view) {
                if (!isRunning) { //berarti belum berjalan, maka dijalankan
                    clickCount = 0; //disetting menjadi 0
                    startSlotMachine(); // menjalankan metode start slot
                    resultText.setText("Semoga menang!");
                } else {
                    stopNextSlot(); //karena berjalan, menjalankan metode stopNextSlot untuk ditekan
                }
            }
        });

        RoomDatabase.Callback myCallBack = new RoomDatabase.Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
            }

            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                super.onOpen(db);
            }
        };

        slotDb = Room.databaseBuilder(getApplicationContext(), SlotDatabase.class,
                "SlotDb").allowMainThreadQueries().addCallback(myCallBack).build();

        handler = new Handler(Looper.getMainLooper()) { //handler untuk menangani pesan dari thread
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle b = msg.getData();
                int slotIndex = b.getInt("slotIndex"); //indeks slot yang akan diubah gambar
                int imageIndex = b.getInt("imageIndex"); //indeks gambar yang akan ditamilkan

                if (slotIndex == 0) {
                    slot1.setImageResource(ImageRes[imageIndex]);
                    currentImages[0] = ImageRes[imageIndex];
                } else if (slotIndex == 1) {
                    slot2.setImageResource(ImageRes[imageIndex]);
                    currentImages[1] = ImageRes[imageIndex];
                } else if (slotIndex == 2) {
                    slot3.setImageResource(ImageRes[imageIndex]);
                    currentImages[2] = ImageRes[imageIndex];
                }

            }
        };
        loadDatabase(); //memanggil metode read database
    }

    //untuk read database dan dimasukkan ke item view holder recycler view
    private void loadDatabase() {
        listData.clear();
        List<SlotEntity> slot = slotDb.getSlotDAO().getAllSlot();
        if (slot.isEmpty()) {
            this.rvList.setVisibility(View.INVISIBLE);
            this.placeholderContainer.setVisibility(View.VISIBLE);
        }else {
            for (SlotEntity slotItem : slot) {
                this.listData.add(new SlotEntity(
                        slotItem.getImageUri1(),
                        slotItem.getImageUri2(),
                        slotItem.getImageUri3(),
                        slotItem.getTanggal(),
                        slotItem.getWaktu()
                ));
            }
            this.slotAdapter = new SlotItemAdapter(this.listData, this::onClick, this);
            this.rvList.setVisibility(View.VISIBLE);
            this.placeholderContainer.setVisibility(View.INVISIBLE);
            this.rvList.setAdapter(slotAdapter);
            this.slotAdapter.notifyDataSetChanged();
        }
    }

    //untuk tombol menghapus database dan position di recyler view
    @Override
    public void onClick(int position) {
        slotDb = Room.databaseBuilder(getApplicationContext(), SlotDatabase.class,
                "SlotDb").allowMainThreadQueries().build();
        SlotDAO slotDAO = slotDb.getSlotDAO();
        try {
            String waktuDiTarik = listData.get(position).getWaktu();
            slotDAO.deleteSlot(slotDb.getSlotDAO().findByWaktu(waktuDiTarik));
            listData.remove(position); //menghapus data slot dari list data
            List<SlotEntity> slot = slotDb.getSlotDAO().getAllSlot();
            if (slot.isEmpty()) {
                this.rvList.setVisibility(View.INVISIBLE);
                this.placeholderContainer.setVisibility(View.VISIBLE);
            }
        }catch (Exception e) {
            Log.d("MainActivity", e.getMessage());
            Toast.makeText(this, "Tidak Berhasil", Integer.parseInt(e.getMessage()));
        }
        this.slotAdapter.notifyDataSetChanged();
        this.slotAdapter.notifyItemRemoved(position);
    }



    // tempat proses pengacakan gambar slot dan thread dijalankan
    private void startSlotMachine() {

        //disetting false karena di awal belum ada yang diberhentikan karena masih dijalankan setiap slot.
        terStopSlot = new boolean[]{false, false, false};

        //dijalankan yang sebelumnya false, jadi apabila button diklik lagi, akan deteksi
        // bahwa isRunning itu true dan memasukki else
        isRunning = true;

        //inisiasi fungsi random thread
        randomThread = new Thread(() -> { //ini merupakan new Runnable
            Random random = new Random(); //inisiasi random
            while (!terStopSlot[0] || !terStopSlot[1] || !terStopSlot[2]) { //selama slot belum berhenti semua
                for (int i = 0; i < 3; i++) { //loop untuk setiap slot
                    if (!terStopSlot[i]) { //jika slot belum berhenti
                        int randomImageIndex = random.nextInt(ImageRes.length); //random gambar slot
                        Message message = handler.obtainMessage(); //membuat pesan untuk handler
                        Bundle bundle = new Bundle(); //membuat bundle untuk data pesan
                        bundle.putInt("slotIndex", i); //menambah data slot index ke bundle
                        bundle.putInt("imageIndex", randomImageIndex); //menambah data image index ke bundle
                        message.setData(bundle); //menambah bundle ke pesan
                        handler.sendMessage(message); //mengirim pesan ke handler
                    }
                }
                try {
                    Thread.sleep(1000); //jeda selama 1 detik
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        randomThread.start(); //memulai random thread
    }

    // MENGGEHANTIKAN 1 SLOT PADA SATU WAKTU PER KLIK
    private void stopNextSlot() {
        clickCount++; //menambah jumlah klik
        for (int i = 0; i < 3; i++) {
            if (!terStopSlot[i]) {
                terStopSlot[i] = true; //untuk di index i, diberhentikan alias dijadikan true supaya berhenti
                break;
            }
        }


        if (terStopSlot[0] && terStopSlot[1] && terStopSlot[2]) { //apabila sudah true semua, dijalankan if-nya
            isRunning = false; //karena sudah true semua, pernyataan isRunning dijadikan false karena sudah berhenti semua
            if (randomThread != null && randomThread.isAlive()) { //ini apabila randomThread masih berjalan
                randomThread.interrupt(); //maka diinterupt supaya berhenti
            }
            Date dateAndTime = Calendar.getInstance().getTime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
            String tanggal = dateFormat.format(dateAndTime);
            String waktu = timeFormat.format(dateAndTime);
            SlotEntity p1 = new SlotEntity(currentImages[0],currentImages[1],
                    currentImages[2],tanggal,waktu);
            this.listData.add(p1);
            slotDb.getSlotDAO().addSlot(p1);
            this.slotAdapter = new SlotItemAdapter(this.listData, this::onClick, this);
            this.rvList.setVisibility(View.VISIBLE);
            this.placeholderContainer.setVisibility(View.INVISIBLE);
            this.rvList.setAdapter(slotAdapter);
            this.slotAdapter.notifyDataSetChanged();
            if (currentImages[0] == currentImages[1] && currentImages[1] == currentImages[2]) { //jika sama
                resultText.setText("Congrats! Menang Gazilion Dollars");
                showDialogSuccess();
            } else {
                showDialogFail();
                resultText.setText("Ga Beruntung Nih");
            }
            handler.postDelayed(() -> resultText.setText("Semangat mencoba lagi!"), 2000);
            //menggunakan handler untuk settingg message untuk resultText
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDatabase();
    }

    private void showDialogFail() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogfail);
        Button lanjut = dialog.findViewById(R.id.continueFail);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        lanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {dialog.dismiss();}
        });
    }

    private void showDialogSuccess() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogsuccess);
        Button lanjut = dialog.findViewById(R.id.continueSuccess);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        lanjut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {dialog.dismiss();}
        });
    }

}