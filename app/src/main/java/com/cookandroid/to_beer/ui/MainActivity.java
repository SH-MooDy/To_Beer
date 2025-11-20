package com.cookandroid.to_beer.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.cookandroid.to_beer.R;
import com.cookandroid.to_beer.adapter.TodoAdapter;
import com.cookandroid.to_beer.model.TodoItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TodoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.todoRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TodoAdapter();
        recyclerView.setAdapter(adapter);

        // ★ 테스트용 더미 데이터 ↓ (이제 바로 화면에 표시됨)
        ArrayList<TodoItem> temp = new ArrayList<>();
        temp.add(new TodoItem(1, "2025-11-21", "C++ 알고리즘 공부", 0, 5));
        temp.add(new TodoItem(2, "2025-11-21", "빨래 널기", 1, 1));
        temp.add(new TodoItem(3, "2025-11-21", "Flutter UI 작업", 0, 3));

        adapter.setItems(temp);
    }
}
