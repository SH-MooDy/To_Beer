package com.cookandroid.to_beer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.to_beer.R;
import com.cookandroid.to_beer.model.BeerSkin;

public class SkinAdapter extends RecyclerView.Adapter<SkinAdapter.SkinViewHolder> {

    public interface OnSkinClickListener {
        void onSkinClick(BeerSkin skin);
    }

    private final BeerSkin[] skins;
    private final OnSkinClickListener listener;
    private final SharedPreferences prefs;
    private int coinCount;
    private int selectedSkinId;

    public SkinAdapter(Context context, BeerSkin[] skins, OnSkinClickListener listener) {
        this.skins = skins;
        this.listener = listener;
        this.prefs = context.getSharedPreferences("to_beer_prefs", Context.MODE_PRIVATE);
        this.coinCount = prefs.getInt("coin_count", 0);
        this.selectedSkinId = prefs.getInt("selected_skin", 0);
    }

    public void refreshState() {
        coinCount = prefs.getInt("coin_count", 0);
        selectedSkinId = prefs.getInt("selected_skin", 0);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SkinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skin, parent, false);
        return new SkinViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SkinViewHolder holder, int position) {
        BeerSkin skin = skins[position];

        holder.textSkinName.setText(skin.name);
        holder.textSkinPrice.setText(skin.price + " 코인");

        // 색 미리보기 (액체 색)
        try {
            holder.viewColorPreview.setBackgroundColor(Color.parseColor(skin.liquidColor));
        } catch (IllegalArgumentException e) {
            holder.viewColorPreview.setBackgroundColor(Color.YELLOW);
        }

        boolean purchased = prefs.getBoolean("purchased_" + skin.id, (skin.price == 0));
        boolean isSelected = (skin.id == selectedSkinId);

        // 기본 스타일
        holder.itemView.setBackgroundColor(0x33000000);
        holder.textSkinName.setTextColor(Color.WHITE);
        holder.textStatus.setTextColor(Color.WHITE);

        if (!purchased) {
            // 잠금 상태
            holder.imageLock.setVisibility(View.VISIBLE);

            if (coinCount < skin.price) {
                holder.textStatus.setText("코인 부족");
                holder.textStatus.setTextColor(0xFF888888);
                holder.itemView.setAlpha(0.6f);
            } else {
                holder.textStatus.setText("구매 가능");
                holder.itemView.setAlpha(1.0f);
            }
        } else {
            // 해금 상태
            holder.imageLock.setVisibility(View.GONE);
            holder.itemView.setAlpha(1.0f);

            if (isSelected) {
                holder.textStatus.setText("사용 중");
                holder.itemView.setBackgroundColor(0x6655FF55); // 연한 초록 하이라이트
            } else {
                holder.textStatus.setText("선택");
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSkinClick(skin);
            }
        });
    }

    @Override
    public int getItemCount() {
        return skins.length;
    }

    static class SkinViewHolder extends RecyclerView.ViewHolder {
        View viewColorPreview;
        TextView textSkinName;
        TextView textSkinPrice;
        ImageView imageLock;
        TextView textStatus;

        public SkinViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorPreview = itemView.findViewById(R.id.viewColorPreview);
            textSkinName = itemView.findViewById(R.id.textSkinName);
            textSkinPrice = itemView.findViewById(R.id.textSkinPrice);
            imageLock = itemView.findViewById(R.id.imageLock);
            textStatus = itemView.findViewById(R.id.textStatus);
        }
    }
}
