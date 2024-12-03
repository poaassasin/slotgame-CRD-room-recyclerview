package com.example.challengeuts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.challengeuts.databinding.ItemSlotBinding;

import java.util.List;

public class SlotItemAdapter extends RecyclerView.Adapter<SlotItemAdapter.ViewHolder> {

    private final Context context;
    private List<SlotEntity> listData;


    private ItemCallbackListener listener;

    public interface ItemCallbackListener{
        void onClick(int position);
    }

    public SlotItemAdapter(List<SlotEntity> listData, ItemCallbackListener itemCallbackListener, Context context) {
        this.listData = listData;
        this.listener = itemCallbackListener;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivSlot1;
        private final ImageView ivSlot2;
        private final ImageView ivSlot3;
        private final TextView tvDate;
        private final View btRemove;
        private final TextView tvHasil;

        private List<SlotEntity> slotData;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.ivSlot1 = view.findViewById(R.id.imageSlot1);
            this.ivSlot2 = view.findViewById(R.id.imageSlot2);
            this.ivSlot3 = view.findViewById(R.id.imageSlot3);
            this.tvDate = view.findViewById(R.id.date);
            this.btRemove = view.findViewById(R.id.delete_item);
            this.tvHasil = view.findViewById(R.id.pernyataanHasil);
        }

        public void bind(SlotEntity entity, int position, ItemCallbackListener listener) {
            this.ivSlot1.setImageResource(entity.getImageUri1());
            this.ivSlot2.setImageResource(entity.getImageUri2());
            this.ivSlot3.setImageResource(entity.getImageUri3());
            this.tvDate.setText(entity.getTanggal() +" | "+entity.getWaktu());
            if (entity.getImageUri1() == entity.getImageUri2()
                    && entity.getImageUri2() == entity.getImageUri3()) { //jika semuanya sama
                this.tvHasil.setText("Win Win Win");
            } else { //kalau tidak sama
                this.tvHasil.setText("Lose");
            }
            this.btRemove.setOnClickListener(v -> listener.onClick(position));
        }
    }

    @NonNull
    @Override
    public SlotItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(this.context)
                .inflate(R.layout.item_slot, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotItemAdapter.ViewHolder holder, int position) {
        holder.bind(this.listData.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
