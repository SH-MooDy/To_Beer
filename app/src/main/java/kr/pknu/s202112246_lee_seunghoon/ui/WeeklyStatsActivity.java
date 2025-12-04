package kr.pknu.s202112246_lee_seunghoon.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import kr.pknu.s202112246_lee_seunghoon.R;
import kr.pknu.s202112246_lee_seunghoon.db.TodoDatabaseHelper;

public class WeeklyStatsActivity extends AppCompatActivity {

    private TodoDatabaseHelper dbHelper;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private TextView textWeekRange;
    private LinearLayout weekContainer;
    private TextView textWeeklySummary;

    // 스와이프용
    private Calendar currentWeek; // 현재 보고 있는 주의 월요일
    private Calendar todayCal;    // 오늘
    private GestureDetector gestureDetector;
    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private final String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_stats);

        // DB & 뷰 초기화
        dbHelper = new TodoDatabaseHelper(this);
        textWeekRange = findViewById(R.id.textWeekRange);
        weekContainer = findViewById(R.id.weekContainer);
        textWeeklySummary = findViewById(R.id.textWeeklySummary);

        // 오늘
        todayCal = Calendar.getInstance();

        // 현재 주의 월요일로 초기화
        currentWeek = Calendar.getInstance();
        currentWeek.setFirstDayOfWeek(Calendar.MONDAY);
        currentWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // 닫기 버튼
        ImageButton btnCloseWeekly = findViewById(R.id.btnCloseWeekly);
        btnCloseWeekly.setOnClickListener(v -> finish());

        // 스와이프 제스처 설정
        setupGesture();

        // 첫 주간 통계 로딩
        loadWeeklyStats();
    }

    // 스와이프 제스처 세팅
    private void setupGesture() {
        gestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true; // 이거 있어야 onFling이 잘 들어옴
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {

                        float diffX = e2.getX() - e1.getX();
                        float diffY = e2.getY() - e1.getY();

                        if (Math.abs(diffX) > Math.abs(diffY) &&
                                Math.abs(diffX) > SWIPE_THRESHOLD &&
                                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                            if (diffX > 0) {
                                changeWeek(-1);
                            } else {
                                changeWeek(+1);
                            }
                            return true;
                        }
                        return false;
                    }
                });
    }


    // 주 변경 (delta: -1 이전 주, +1 다음 주)
    private void changeWeek(int delta) {
        Calendar newWeek = (Calendar) currentWeek.clone();
        newWeek.add(Calendar.WEEK_OF_YEAR, delta);

        // 아직 오지 않은 주로는 못 가게 막기
        Calendar thisWeekMonday = (Calendar) todayCal.clone();
        thisWeekMonday.setFirstDayOfWeek(Calendar.MONDAY);
        thisWeekMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        if (newWeek.after(thisWeekMonday)) {
            Toast.makeText(this, "아직 오지 않은 주입니다!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentWeek = newWeek;
        loadWeeklyStats();
    }

    // 현재 currentWeek(월요일 기준)를 화면에 반영
    private void loadWeeklyStats() {
        // currentWeek = 보고 있는 주의 월요일
        Calendar mondayCal = (Calendar) currentWeek.clone();
        Calendar sundayCal = (Calendar) currentWeek.clone();
        sundayCal.add(Calendar.DAY_OF_YEAR, 6);

        String startDateStr = dateFormat.format(mondayCal.getTime());
        String endDateStr = dateFormat.format(sundayCal.getTime());
        textWeekRange.setText(startDateStr + " ~ " + endDateStr);

        // 주간 합계용
        int sumTotalWeight = 0;
        int sumDoneWeight = 0;
        int sumTotalCount = 0;
        int sumDoneCount = 0;

        weekContainer.removeAllViews();

        for (int i = 0; i < 7; i++) {
            Calendar dayCal = (Calendar) mondayCal.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, i);
            String dateStr = dateFormat.format(dayCal.getTime());

            int totalWeight = dbHelper.getTotalWeightForDate(dateStr);
            int doneWeight = dbHelper.getDoneWeightForDate(dateStr);
            int totalCount = dbHelper.getTodoCountByDate(dateStr);
            int doneCount = dbHelper.getDoneCountByDate(dateStr);

            sumTotalWeight += totalWeight;
            sumDoneWeight += doneWeight;
            sumTotalCount += totalCount;
            sumDoneCount += doneCount;

            int percent = 0;
            if (totalWeight > 0) {
                percent = Math.round(doneWeight * 100f / totalWeight);
            }

            addDayRow(dayLabels[i], percent,
                    totalWeight, doneWeight,
                    totalCount, doneCount);
        }

        int weeklyPercent = 0;
        if (sumTotalWeight > 0) {
            weeklyPercent = Math.round(sumDoneWeight * 100f / sumTotalWeight);
        }

        String summary = "이번 주 요약\n"
                + "- 전체 할 일: " + sumTotalCount + "개\n"
                + "- 완료한 할 일: " + sumDoneCount + "개\n"
                + "- 총 목표 weight: " + sumTotalWeight + "\n"
                + "- 완료 weight: " + sumDoneWeight + "\n"
                + "- 주간 달성률: " + weeklyPercent + "%";

        textWeeklySummary.setText(summary);
    }

    // 하루 행 + 막대 표시
    private void addDayRow(String dayLabel,
                           int percent,
                           int totalWeight,
                           int doneWeight,
                           int totalCount,
                           int doneCount) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(4), dp(4), dp(4), dp(4));

        TextView textInfo = new TextView(this);
        textInfo.setTextColor(0xFFFFFFFF);
        textInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        String info = dayLabel + " - " + percent + "%";
        info += "  (" + doneCount + "/" + totalCount + " tasks, "
                + doneWeight + "/" + totalWeight + " weight)";
        textInfo.setText(info);

        TextView textBar = new TextView(this);
        textBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        int blocks = percent / 10; // 0~10칸
        if (blocks < 0) blocks = 0;
        if (blocks > 10) blocks = 10;

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < blocks; i++) bar.append("■");
        for (int i = blocks; i < 10; i++) bar.append("□");

        textBar.setText(bar.toString());
        textBar.setTextColor(0xFFFFD54F); // 노란 막대 느낌

        row.addView(textInfo);
        row.addView(textBar);

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1));
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(0x40FFFFFF);

        weekContainer.addView(row);
        weekContainer.addView(divider);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 제스처 디텍터에게 모든 터치 이벤트를 먼저 전달
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        // 나머지 기본 터치 동작(스크롤, 클릭 등)은 그대로 유지
        return super.dispatchTouchEvent(ev);
    }

}
