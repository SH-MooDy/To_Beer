package com.cookandroid.to_beer.adapter;

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

    // ★ setItems() 메서드 반드시 포함 필요
    public void setItems(ArrayList<TodoItem> list) {
        items = list;
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

        holder.textTitle.setText(item.getTitle());
        holder.textWeight.setText("Weight: " + item.getWeight());
        holder.checkTodo.setChecked(item.getIsComplete() == 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkTodo;
        TextView textTitle;
        TextView textWeight;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTodo = itemView.findViewById(R.id.checkTodo);
            textTitle = itemView.findViewById(R.id.textTitle);
            textWeight = itemView.findViewById(R.id.textWeight);
        }
    }
}
