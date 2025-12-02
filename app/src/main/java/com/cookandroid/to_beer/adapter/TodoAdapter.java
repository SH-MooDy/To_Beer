package com.cookandroid.to_beer.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.to_beer.R;
import com.cookandroid.to_beer.model.TodoItem;

import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private ArrayList<TodoItem> items = new ArrayList<>();

    public interface OnItemCheckChangedListener {
        void onItemCheckChanged(TodoItem item, boolean isChecked);
    }

    private OnItemCheckChangedListener listener;

    public TodoAdapter(OnItemCheckChangedListener listener) {
        this.listener = listener;
    }

    public void setItems(ArrayList<TodoItem> list) {
        this.items = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        TodoItem item = items.get(position);

        // 리스너 중복 호출 방지
        holder.checkTodo.setOnCheckedChangeListener(null);

        holder.textTitle.setText(item.getTitle());
        holder.textWeight.setText("Weight: " + item.getWeight());
        holder.checkTodo.setChecked(item.getIsComplete() == 1);

        // ✔ weight 만큼 🍺🍺🍺 생성
        int w = item.getWeight();
        if (w < 1) w = 1;
        if (w > 5) w = 5; // 혹시 모를 값 방어
        StringBuilder beer = new StringBuilder();
        for (int i = 0; i < w; i++) {
            beer.append("🍺");
        }
        holder.textBeerIcons.setText(beer.toString());

        // ✔ 완료 스타일 (취소선 + 색 연하게)
        if (item.getIsComplete() == 1) {
            holder.textTitle.setPaintFlags(
                    holder.textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textTitle.setTextColor(0xFFAAAAAA);
        } else {
            holder.textTitle.setPaintFlags(
                    holder.textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textTitle.setTextColor(0xFFFFFFFF);
        }

        holder.checkTodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onItemCheckChanged(item, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkTodo;
        TextView textTitle;
        TextView textWeight;
        TextView textBeerIcons;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTodo = itemView.findViewById(R.id.checkTodo);
            textTitle = itemView.findViewById(R.id.textTitle);
            textWeight = itemView.findViewById(R.id.textWeight);
            textBeerIcons = itemView.findViewById(R.id.textBeerIcons);
        }
    }
}
