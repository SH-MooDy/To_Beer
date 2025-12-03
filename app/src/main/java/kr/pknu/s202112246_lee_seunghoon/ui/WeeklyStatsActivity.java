package kr.pknu.s202112246_lee_seunghoon.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import kr.pknu.s202112246_lee_seunghoon.R;
import kr.pknu.s202112246_lee_seunghoon.db.TodoDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WeeklyStatsActivity extends AppCompatActivity {

    private TodoDatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private TextView textWeekRange;
    private LinearLayout weekContainer;
    private TextView textWeeklySummary;

    private final String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_stats);

        dbHelper = new TodoDatabaseHelper(this);

        textWeekRange = findViewById(R.id.textWeekRange);
        weekContainer = findViewById(R.id.weekContainer);
        textWeeklySummary = findViewById(R.id.textWeeklySummary);

        buildWeeklyStats();
    }

    private void buildWeeklyStats() {
        Calendar cal = Calendar.getInstance();

        // 이번 주의 "월요일"로 이동 (한국 기준으로 월 시작주)
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int deltaToMonday = (currentDayOfWeek == Calendar.SUNDAY)
                ? -6
                : (Calendar.MONDAY - currentDayOfWeek);
        cal.add(Calendar.DAY_OF_YEAR, deltaToMonday);

        Calendar mondayCal = (Calendar) cal.clone();

        // 주간 범위 텍스트 (월요일 ~ 일요일)
        String startDateStr = dateFormat.format(mondayCal.getTime());
        Calendar sundayCal = (Calendar) mondayCal.clone();
        sundayCal.add(Calendar.DAY_OF_YEAR, 6);
        String endDateStr = dateFormat.format(sundayCal.getTime());
        textWeekRange.setText(startDateStr + " ~ " + endDateStr);

        // 요약용 누적 값
        int sumTotalWeight = 0;
        int sumDoneWeight = 0;
        int sumTotalCount  = 0;
        int sumDoneCount   = 0;

        weekContainer.removeAllViews();

        for (int i = 0; i < 7; i++) {
            Calendar dayCal = (Calendar) mondayCal.clone();
            dayCal.add(Calendar.DAY_OF_YEAR, i);
            String dateStr = dateFormat.format(dayCal.getTime());

            int totalWeight = dbHelper.getTotalWeightForDate(dateStr);
            int doneWeight  = dbHelper.getDoneWeightForDate(dateStr);
            int totalCount  = dbHelper.getTodoCountByDate(dateStr);
            int doneCount   = dbHelper.getDoneCountByDate(dateStr);

            sumTotalWeight += totalWeight;
            sumDoneWeight  += doneWeight;
            sumTotalCount  += totalCount;
            sumDoneCount   += doneCount;

            int percent = 0;
            if (totalWeight > 0) {
                percent = Math.round(doneWeight * 100f / totalWeight);
            }

            // 한 줄짜리 "막대 그래프 느낌" 행 추가
            addDayRow(dayLabels[i], percent, totalWeight, doneWeight, totalCount, doneCount);
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

    private void addDayRow(String dayLabel,
                           int percent,
                           int totalWeight,
                           int doneWeight,
                           int totalCount,
                           int doneCount) {

        // 행 전체 레이아웃
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(4), dp(4), dp(4), dp(4));

        // 첫 줄: "Mon - 75% (완료 2/3개)"
        TextView textInfo = new TextView(this);
        textInfo.setTextColor(0xFFFFFFFF);
        textInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        String info = dayLabel + " - " + percent + "%";
        info += "  (" + doneCount + "/" + totalCount + " tasks, "
                + doneWeight + "/" + totalWeight + " weight)";
        textInfo.setText(info);

        // 두 번째 줄: 간단한 그래픽 막대 (텍스트 기반)
        TextView textBar = new TextView(this);
        textBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        int blocks = percent / 10; // 0~10칸
        if (blocks < 0) blocks = 0;
        if (blocks > 10) blocks = 10;

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < blocks; i++) {
            bar.append("■");
        }
        for (int i = blocks; i < 10; i++) {
            bar.append("□");
        }

        textBar.setText(bar.toString());
        textBar.setTextColor(0xFFFFD54F); // 연한 노란색 느낌

        row.addView(textInfo);
        row.addView(textBar);

        // 구분선
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
}
