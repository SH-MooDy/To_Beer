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

        // 리스너 중복 호출 방지용
        holder.checkTodo.setOnCheckedChangeListener(null);

        holder.textTitle.setText(item.getTitle());
        holder.textWeight.setText("Weight: " + item.getWeight());
        holder.checkTodo.setChecked(item.getIsComplete() == 1);

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

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkTodo = itemView.findViewById(R.id.checkTodo);
            textTitle = itemView.findViewById(R.id.textTitle);
            textWeight = itemView.findViewById(R.id.textWeight);
        }
    }
}
