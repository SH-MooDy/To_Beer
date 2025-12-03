package com.cookandroid.to_beer.ui;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.cookandroid.to_beer.R;
import com.cookandroid.to_beer.adapter.TodoAdapter;
import com.cookandroid.to_beer.db.TodoDatabaseHelper;
import com.cookandroid.to_beer.model.TodoItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private TodoAdapter adapter;
    private TodoDatabaseHelper dbHelper;
    private String today;
    private String currentDate;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // 맥주 채움(ClipDrawable)용
    private ImageView imageBeerFill;
    private ClipDrawable beerClipDrawable;
    private int currentLevel = 0; // 0 ~ 10000

    // 텍스트들
    private TextView textProgress;
    private TextView textStreak;
    private TextView textCurrentDate;
    private TextView textBestStreak;

    // 거품 애니메이션
    private LottieAnimationView lottieFoam;
    private boolean isFull = false;

    // 축하 애니메이션
    private LottieAnimationView lottieCongrats;


    private SharedPreferences prefs;
    private int bestStreak = 0;

    private GestureDetector gestureDetector;

    private static final int SWIPE_THRESHOLD = 100;         // 최소 이동 거리(px)
    private static final int SWIPE_VELOCITY_THRESHOLD = 100; // 최소 속도

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        today = dateFormat.format(new Date());
        currentDate = today;

        dbHelper = new TodoDatabaseHelper(this);

        prefs = getSharedPreferences("to_beer_prefs", MODE_PRIVATE);
        bestStreak = prefs.getInt("best_streak", 0);

        imageBeerFill = findViewById(R.id.imageBeerFill);
        textProgress  = findViewById(R.id.textProgress);
        textStreak    = findViewById(R.id.textStreak);
        textCurrentDate = findViewById(R.id.textCurrentDate);
        textBestStreak  = findViewById(R.id.textBestStreak);
        lottieFoam    = findViewById(R.id.lottieFoam);
        lottieCongrats = findViewById(R.id.lottieCongrats);

        drawerLayout   = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenu        = findViewById(R.id.btnMenu);

        // 햄버거 버튼 클릭 => 사이드 드로어 열기
        btnMenu.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);

            }
        });

        // 사이드 메뉴 아이템 클릭 처리
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_weekly_stats) {
                // 주간 통계 화면으로 이동
                Intent intent = new Intent(MainActivity.this, WeeklyStatsActivity.class);
                startActivity(intent);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });



        View rootLayout = findViewById(R.id.rootLayout);

        // 제스처 감지기 설정
        gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        // 반드시 true를 반환해야 이후 이벤트(onFling 등)가 들어온다
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        float diffX = e2.getX() - e1.getX();
                        float diffY = e2.getY() - e1.getY();

                        if (Math.abs(diffX) > Math.abs(diffY)
                                && Math.abs(diffX) > SWIPE_THRESHOLD
                                && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                            if (diffX > 0) {
                                // 오른쪽 스와이프 → 이전 날
                                changeDate(-1);
                            } else {
                                // 왼쪽 스와이프 → 다음 날
                                changeDate(1);
                            }
                            return true;
                        }
                        return false;
                    }
                });


        // 루트 레이아웃에 터치 전달
        rootLayout.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // 오늘 통계 팝업
        textProgress.setOnClickListener(v -> showTodayStatsDialog());

        if (textBestStreak != null) {
            textBestStreak.setText("🏆 Best: " + bestStreak + " days");
        }

        textCurrentDate.setText(currentDate);

        // 날짜 이동 버튼
        ImageButton btnPrevDate = findViewById(R.id.btnPrevDate);
        ImageButton btnNextDate = findViewById(R.id.btnNextDate);

        btnPrevDate.setOnClickListener(v -> changeDate(-1)); // 하루 전
        btnNextDate.setOnClickListener(v -> changeDate(1));  // 하루 후

        // 진행률 텍스트 눌렀을 때 통계 다이얼로그
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
        today = dateFormat.format(new Date());
        currentDate = today;
        if (textCurrentDate != null) {
            textCurrentDate.setText(currentDate);
        }
        loadTodos();
        updateBeerProgress();
        updateStreak();
    }


    // 오늘 날짜의 할 일 목록을 불러와서 어댑터에 반영
    private void loadTodos() {
        ArrayList<TodoItem> list = dbHelper.getTodosByDate(currentDate);
        adapter.setItems(list);
    }

    // 맥주잔 진행도 + 거품 애니메이션
    private void updateBeerProgress() {
        if (beerClipDrawable == null) return;

        int total = dbHelper.getTotalWeightForDate(currentDate);
        int done  = dbHelper.getDoneWeightForDate(currentDate);

        if (total <= 0) {
            // 맥주 게이지 0으로
            animateBeerLevel(0);
            if (textProgress != null) textProgress.setText("0%");

            // 거품도 반드시 꺼준다
            if (lottieFoam != null) {
                isFull = false;
                lottieFoam.cancelAnimation();
                lottieFoam.setAlpha(0f);
                lottieFoam.setVisibility(View.GONE);
            }

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

                // 1초 후 축하 애니메이션 실행
                new Handler().postDelayed(() -> {
                    if (lottieCongrats != null) {
                        lottieCongrats.setVisibility(View.VISIBLE);
                        lottieCongrats.setAlpha(0f);
                        lottieCongrats.setScaleX(0.8f);
                        lottieCongrats.setScaleY(0.8f);

                        lottieCongrats.playAnimation();

                        lottieCongrats.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(500)
                                .start();

                        // 축하 애니메이션 3초 후 자동 사라짐
                        new Handler().postDelayed(() -> {
                            lottieCongrats.animate()
                                    .alpha(0f)
                                    .setDuration(600)
                                    .withEndAction(() -> {
                                        lottieCongrats.setVisibility(View.GONE);
                                    })
                                    .start();
                        }, 2500); // 3초 뒤 사라짐
                    }

                    Toast.makeText(MainActivity.this,
                            "오늘 할 일 완료! 🎉", Toast.LENGTH_SHORT).show();

                }, 800);

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
                    dbHelper.insertTodo(currentDate, title, weight);
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

        // 최고 스트릭 갱신 체크
        if (streak > bestStreak) {
            bestStreak = streak;
            // SharedPreferences에 저장
            if (prefs != null) {
                prefs.edit()
                        .putInt("best_streak", bestStreak)
                        .apply();
            }

            // UI 갱신
            if (textBestStreak != null) {
                textBestStreak.setText("🏆 Best: " + bestStreak + " days");
            }

            // 신기록일 때만 축하 토스트/메시지
            if (streak > 0) {
                Toast.makeText(this,
                        "🎉 새 기록! " + bestStreak + "일 연속 100% 달성!",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // 기존 기록 유지 시에도 UI는 최신 값 보여주기
            if (textBestStreak != null) {
                textBestStreak.setText("🏆 Best: " + bestStreak + " days");
            }
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
        int totalWeight = dbHelper.getTotalWeightForDate(currentDate);
        int doneWeight  = dbHelper.getDoneWeightForDate(currentDate);
        int totalCount  = dbHelper.getTodoCountByDate(currentDate);
        int doneCount   = dbHelper.getDoneCountByDate(currentDate);

        int percent = 0;
        if (totalWeight > 0) {
            percent = Math.round(doneWeight * 100f / totalWeight);
        }

        String message =
                currentDate + " 기준\n\n" +
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

    private void changeDate(int deltaDays) {
        try {
            Date current = dateFormat.parse(currentDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(current);
            cal.add(Calendar.DAY_OF_YEAR, deltaDays);

            currentDate = dateFormat.format(cal.getTime());
            if (textCurrentDate != null) {
                textCurrentDate.setText(currentDate);
            }

            loadTodos();
            updateBeerProgress();
            // 스트릭은 "오늘 기준 연속 100% 일수"라서 그대로 두고,
            // 원하면 여기서도 updateStreak()를 호출해도 됨
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


     //테스트 데이터
//    private void seedTestData() {
//        dbHelper.insertTodo(today, "C++ 알고리즘 공부", 5);
//        dbHelper.insertTodo(today, "빨래 널기", 1);
//        dbHelper.insertTodo(today, "Flutter UI 작업", 3);
//    }
}
