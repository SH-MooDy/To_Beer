package com.cookandroid.to_beer.ui;

import android.animation.ValueAnimator;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookandroid.to_beer.R;
import com.cookandroid.to_beer.adapter.TodoAdapter;
import com.cookandroid.to_beer.db.TodoDatabaseHelper;
import com.cookandroid.to_beer.model.TodoItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TodoAdapter adapter;
    private TodoDatabaseHelper dbHelper;
    private String today;

    // 맥주 채움(ClipDrawable)용
    private ImageView imageBeerFill;
    private ClipDrawable beerClipDrawable;
    private int currentLevel = 0; // 0 ~ 10000

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 오늘 날짜 문자열 (예: 2025-11-21)
        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        dbHelper = new TodoDatabaseHelper(this);

        // 맥주 채워지는 이미지뷰
        imageBeerFill = findViewById(R.id.imageBeerFill);
        if (imageBeerFill != null) {
            Drawable d = imageBeerFill.getDrawable();
            if (d instanceof ClipDrawable) {
                beerClipDrawable = (ClipDrawable) d;
            } else if (d != null) {
                // 혹시 xml에서 clip 이 아니더라도 안전하게 감싸기
                beerClipDrawable = new ClipDrawable(d, Gravity.BOTTOM, ClipDrawable.VERTICAL);
                imageBeerFill.setImageDrawable(beerClipDrawable);
            }
            if (beerClipDrawable != null) {
                beerClipDrawable.setLevel(currentLevel);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.todoRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TodoAdapter((item, isChecked) -> {
            // 체크박스 변경 시 DB 업데이트
            dbHelper.updateTodoComplete(item.getId(), isChecked);
            // 리스트와 맥주잔 진행도 갱신
            loadTodos();
            updateBeerProgress();
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddTodo);
        fab.setOnClickListener(v -> showAddTodoDialog());

        // 처음 테스트용 데이터 넣고 싶은 경우만 주석 해제
        // seedTestData();

        loadTodos();
        updateBeerProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 다시 화면에 돌아왔을 때도 데이터/진행도 갱신
        loadTodos();
        updateBeerProgress();
    }

    // 오늘 날짜의 Todo 목록을 DB에서 읽어서 RecyclerView에 반영
    private void loadTodos() {
        ArrayList<TodoItem> list = dbHelper.getTodosByDate(today);
        adapter.setItems(list);
    }

    // 오늘의 totalWeight, doneWeight 기반으로 맥주잔 진행도 업데이트
    private void updateBeerProgress() {
        if (beerClipDrawable == null) return;

        int total = dbHelper.getTotalWeightForDate(today);
        int done = dbHelper.getDoneWeightForDate(today);

        if (total <= 0) {
            // 할 일이 없으면 빈 잔
            animateBeerLevel(0);
            return;
        }

        float ratio = done / (float) total;  // 0.0 ~ 1.0
        if (ratio < 0) ratio = 0;
        if (ratio > 1) ratio = 1;

        int targetLevel = (int) (ratio * 10000); // ClipDrawable level: 0~10000
        animateBeerLevel(targetLevel);
    }

    // ClipDrawable level을 부드럽게 변경
    private void animateBeerLevel(int targetLevel) {
        if (beerClipDrawable == null) return;

        ValueAnimator animator = ValueAnimator.ofInt(currentLevel, targetLevel);
        animator.setDuration(500); // 0.5초 정도
        animator.addUpdateListener(animation -> {
            int level = (int) animation.getAnimatedValue();
            beerClipDrawable.setLevel(level);
            currentLevel = level;
        });
        animator.start();
    }

    // 테스트용으로 한 번만 호출해서 초기 데이터 심고 싶을 때 사용
    private void seedTestData() {
        dbHelper.insertTodo(today, "C++ 알고리즘 공부", 5);
        dbHelper.insertTodo(today, "빨래 널기", 1);
        dbHelper.insertTodo(today, "Flutter UI 작업", 3);
    }

    private void showAddTodoDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_add_todo, null);

        EditText editTitle = view.findViewById(R.id.editTitle);
        NumberPicker pickerWeight = view.findViewById(R.id.pickerWeight);

        pickerWeight.setMinValue(1);
        pickerWeight.setMaxValue(5);
        pickerWeight.setValue(3); // 기본값

        new AlertDialog.Builder(this)
                .setTitle("할 일 추가")
                .setView(view)
                .setPositiveButton("추가", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    int weight = pickerWeight.getValue();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbHelper.insertTodo(today, title, weight);
                    loadTodos();
                    updateBeerProgress();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
