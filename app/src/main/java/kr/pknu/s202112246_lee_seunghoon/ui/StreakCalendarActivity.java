package kr.pknu.s202112246_lee_seunghoon.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import kr.pknu.s202112246_lee_seunghoon.R;

public class StreakCalendarActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private CalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_calendar);

        prefs = getSharedPreferences("to_beer_prefs", MODE_PRIVATE);

        calendarView = findViewById(R.id.calendarView);

        ImageButton btnClose = findViewById(R.id.btnCloseStreak);
        btnClose.setOnClickListener(v -> finish());

        markClearedDays();
    }

    /** cleared_yyyy-MM-dd 가 true 인 날짜에 맥주 아이콘 표시 */
    private void markClearedDays() {
        List<EventDay> events = new ArrayList<>();

        Calendar cal = Calendar.getInstance();

        // 최근 1년만 뒤로 훑으면서 체크
        for (int i = 0; i < 365; i++) {
            String dateStr = dateFormat.format(cal.getTime());
            boolean cleared = prefs.getBoolean("cleared_" + dateStr, false);

            if (cleared) {
                Calendar day = Calendar.getInstance();
                day.setTime(cal.getTime());

                events.add(new EventDay(day, R.drawable.mascot));
            }

            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        calendarView.setEvents(events);
    }
}
