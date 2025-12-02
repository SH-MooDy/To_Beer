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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.cookandroid.to_beer.R;
import com.cookandroid.to_beer.adapter.TodoAdapter;
import com.cookandroid.to_beer.db.TodoDatabaseHelper;
import com.cookandroid.to_beer.model.TodoItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TodoAdapter adapter;
    private TodoDatabaseHelper dbHelper;
    private String today;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // 맥주 채움(ClipDrawable)용
    private ImageView imageBeerFill;
    private ClipDrawable beerClipDrawable;
    private int currentLevel = 0; // 0 ~ 10000

    // 텍스트들
    private TextView textProgress;
    private TextView textStreak;

    // 거품 애니메이션
    private LottieAnimationView lottieFoam;
    private boolean isFull = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        today = dateFormat.format(new Date());
        dbHelper = new TodoDatabaseHelper(this);

        imageBeerFill = findViewById(R.id.imageBeerFill);
        textProgress = findViewById(R.id.textProgress);
        textStreak = findViewById(R.id.textStreak);
        lottieFoam = findViewById(R.id.lottieFoam);

        textProgress.setOnClickListener(v -> showTodayStatsDialog());

        if (lottieFoam != null) {
            lottieFoam.setVisibility(View.GONE);
        }

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
            updateStreak();
        });

        adapter.setOnItemLongClickListener(item -> {
            showEditDeleteDialog(item);
        });

        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddTodo);
        fab.setOnClickListener(v -> showAddTodoDialog());

        // seedTestData(); // 필요하면 한 번만 호출

        loadTodos();
        updateBeerProgress();
        updateStreak();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodos();
        updateBeerProgress();
        updateStreak();
    }

    // 오늘 날짜의 할 일 목록을 불러와서 어댑터에 반영
    private void loadTodos() {
        ArrayList<TodoItem> list = dbHelper.getTodosByDate(today);
        adapter.setItems(list);
    }

    // 맥주잔 진행도 + 거품 애니메이션
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

        // 100% 달성 시 거품 애니메이션 (서서히 등장/사라짐)
        if (ratio >= 1f) {
            if (!isFull && lottieFoam != null) {
                isFull = true;

                lottieFoam.setVisibility(View.VISIBLE);
                lottieFoam.setAlpha(0f);
                lottieFoam.setScaleX(0.9f);
                lottieFoam.setScaleY(0.9f);
                lottieFoam.playAnimation();

                lottieFoam.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(700)
                        .start();
            }
        } else {
            if (isFull && lottieFoam != null) {
                isFull = false;

                lottieFoam.animate()
                        .alpha(0f)
                        .setDuration(400)
                        .withEndAction(() -> {
                            lottieFoam.cancelAnimation();
                            lottieFoam.setVisibility(View.GONE);
                        })
                        .start();
            }
        }
    }

    // ClipDrawable 레벨 애니메이션
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

    // 할 일 추가 다이얼로그
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
                    updateStreak();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 연속 달성일(streak) 계산
    private void updateStreak() {
        if (textStreak == null) return;

        // 오늘 날짜 기준으로 뒤로 하루씩 줄여가며 검사
        Calendar cal = Calendar.getInstance();
        int streak = 0;

        // 최대 365일만 검사
        for (int i = 0; i < 365; i++) {
            String dateStr = dateFormat.format(cal.getTime());

            int total = dbHelper.getTotalWeightForDate(dateStr);
            int done = dbHelper.getDoneWeightForDate(dateStr);

            // 그날 등록한 할 일(weight) 전체를 다 끝낸 날만 연속 일수로 인정
            if (total > 0 && done >= total) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1); // 하루 전으로 이동
            } else {
                break;
            }
        }

        // 텍스트 표시
        if (streak <= 0) {
            textStreak.setText("🔥 0 days");
        } else if (streak == 1) {
            textStreak.setText("🔥 1 day");
        } else {
            textStreak.setText("🔥 " + streak + " days");
        }
    }

    private void showEditDeleteDialog(TodoItem item) {
        String[] options = {"수정", "삭제"};

        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // 수정
                        showEditTodoDialog(item);
                    } else if (which == 1) {
                        // 삭제
                        dbHelper.deleteTodo(item.getId());
                        loadTodos();
                        updateBeerProgress();
                        updateStreak();
                        Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showEditTodoDialog(TodoItem item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_add_todo, null);

        EditText editTitle = view.findViewById(R.id.editTitle);
        NumberPicker pickerWeight = view.findViewById(R.id.pickerWeight);

        // 기존 값 세팅
        editTitle.setText(item.getTitle());
        pickerWeight.setMinValue(1);
        pickerWeight.setMaxValue(5);
        pickerWeight.setValue(item.getWeight());

        new AlertDialog.Builder(this)
                .setTitle("할 일 수정")
                .setView(view)
                .setPositiveButton("저장", (dialog, which) -> {
                    String newTitle = editTitle.getText().toString().trim();
                    int newWeight = pickerWeight.getValue();

                    if (newTitle.isEmpty()) {
                        Toast.makeText(this, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dbHelper.updateTodo(item.getId(), newTitle, newWeight);
                    loadTodos();
                    updateBeerProgress();
                    updateStreak();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 오늘 통계 다이얼로그
    private void showTodayStatsDialog() {
        int totalWeight = dbHelper.getTotalWeightForDate(today);
        int doneWeight  = dbHelper.getDoneWeightForDate(today);
        int totalCount  = dbHelper.getTodoCountByDate(today);
        int doneCount   = dbHelper.getDoneCountByDate(today);

        int percent = 0;
        if (totalWeight > 0) {
            percent = Math.round(doneWeight * 100f / totalWeight);
        }

        String message =
                "오늘 등록한 할 일: " + totalCount + "개\n" +
                        "완료한 할 일: " + doneCount + "개\n\n" +
                        "총 목표 weight: " + totalWeight + "\n" +
                        "완료 weight: " + doneWeight + "\n" +
                        "달성률: " + percent + "%";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("오늘의 통계")
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }

    // 테스트 데이터
//    private void seedTestData() {
//        dbHelper.insertTodo(today, "C++ 알고리즘 공부", 5);
//        dbHelper.insertTodo(today, "빨래 널기", 1);
//        dbHelper.insertTodo(today, "Flutter UI 작업", 3);
//    }
}
