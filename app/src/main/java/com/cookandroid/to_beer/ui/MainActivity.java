package com.cookandroid.to_beer.ui;

import android.widget.TextView;
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

    // 진행률 텍스트
    private TextView textProgress;
    // (나중용) 스트릭 텍스트
    private TextView textStreak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        dbHelper = new TodoDatabaseHelper(this);

        imageBeerFill = findViewById(R.id.imageBeerFill);
        textProgress = findViewById(R.id.textProgress);
        textStreak = findViewById(R.id.textStreak);

        // ClipDrawable 초기화
        if (imageBeerFill != null) {
            Drawable d = imageBeerFill.getDrawable();
            if (d instanceof ClipDrawable) {
                beerClipDrawable = (ClipDrawable) d;
            } else if (d != null) {
                beerClipDrawable = new ClipDrawable(d, Gravity.BOTTOM, ClipDrawable.VERTICAL);
                imageBeerFill.setImageDrawable(beerClipDrawable);
            }
            if (beerClipDrawable != null) beerClipDrawable.setLevel(currentLevel);
        }

        RecyclerView recyclerView = findViewById(R.id.todoRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TodoAdapter((item, isChecked) -> {
            dbHelper.updateTodoComplete(item.getId(), isChecked);
            loadTodos();
            updateBeerProgress();
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddTodo);
        fab.setOnClickListener(v -> showAddTodoDialog());

        // seedTestData(); // 필요하면 한 번만 호출

        loadTodos();
        updateBeerProgress();
        updateStreakPlaceholder(); // 지금은 가짜 값만
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodos();
        updateBeerProgress();
    }

    private void loadTodos() {
        ArrayList<TodoItem> list = dbHelper.getTodosByDate(today);
        adapter.setItems(list);
    }

    private void updateBeerProgress() {
        if (beerClipDrawable == null) return;

        int total = dbHelper.getTotalWeightForDate(today);
        int done = dbHelper.getDoneWeightForDate(today);

        if (total <= 0) {
            animateBeerLevel(0);
            if (textProgress != null) textProgress.setText("0%");
            return;
        }

        float ratio = done / (float) total;  // 0.0 ~ 1.0
        ratio = Math.max(0f, Math.min(1f, ratio));

        int targetLevel = (int) (ratio * 10000);
        animateBeerLevel(targetLevel);

        int percent = Math.round(ratio * 100);
        if (textProgress != null) {
            textProgress.setText(percent + "%");
        }
    }

    private void animateBeerLevel(int targetLevel) {
        if (beerClipDrawable == null) return;

        ValueAnimator animator = ValueAnimator.ofInt(currentLevel, targetLevel);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> {
            int level = (int) animation.getAnimatedValue();
            beerClipDrawable.setLevel(level);
            currentLevel = level;
        });
        animator.start();
    }

    private void showAddTodoDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_add_todo, null);

        EditText editTitle = view.findViewById(R.id.editTitle);
        NumberPicker pickerWeight = view.findViewById(R.id.pickerWeight);

        pickerWeight.setMinValue(1);
        pickerWeight.setMaxValue(5);
        pickerWeight.setValue(3);

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

    // 스트릭 로직은 아직 미구현 – 일단 자리만 채워두기
    private void updateStreakPlaceholder() {
        if (textStreak != null) {
            textStreak.setText("🔥 0 days"); // 나중에 DB 연결해서 진짜 값으로 바꾸면 됨
        }
    }

    // seedTestData()는 기존 그대로 두면 됨
}

