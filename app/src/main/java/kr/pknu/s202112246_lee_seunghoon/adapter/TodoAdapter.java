package kr.pknu.s202112246_lee_seunghoon.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import kr.pknu.s202112246_lee_seunghoon.R;
import kr.pknu.s202112246_lee_seunghoon.model.TodoItem;

import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private ArrayList<TodoItem> items = new ArrayList<>();

    public interface OnItemCheckChangedListener {
        void onItemCheckChanged(TodoItem item, boolean isChecked);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(TodoItem item);
    }

    private OnItemCheckChangedListener checkListener;
    private OnItemLongClickListener longClickListener;

    public TodoAdapter(OnItemCheckChangedListener listener) {
        this.checkListener = listener;
    }

    // 롱클릭 리스너 설정
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
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

        // 리스너 중복 방지: 먼저 null로
        holder.checkTodo.setOnCheckedChangeListener(null);

        holder.textTitle.setText(item.getTitle());
        holder.textWeight.setText("Weight: " + item.getWeight());
        holder.checkTodo.setChecked(item.getIsComplete() == 1);

        // weight 만큼 🍺 찍기
        int w = item.getWeight();
        if (w < 1) w = 1;
        if (w > 5) w = 5;
        StringBuilder beer = new StringBuilder();
        for (int i = 0; i < w; i++) beer.append("🍺");
        holder.textBeerIcons.setText(beer.toString());

        // 완료 스타일
        if (item.getIsComplete() == 1) {
            holder.textTitle.setPaintFlags(
                    holder.textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textTitle.setTextColor(0xFFAAAAAA);
        } else {
            holder.textTitle.setPaintFlags(
                    holder.textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textTitle.setTextColor(0xFFFFFFFF);
        }

        // 체크박스 변경 리스너
        holder.checkTodo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkListener != null) {
                checkListener.onItemCheckChanged(item, isChecked);
            }
        });

        // 롱클릭 리스너
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(item);
            }
            return true;
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
