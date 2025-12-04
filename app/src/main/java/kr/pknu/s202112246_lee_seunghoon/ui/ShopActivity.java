package kr.pknu.s202112246_lee_seunghoon.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import kr.pknu.s202112246_lee_seunghoon.R;
import kr.pknu.s202112246_lee_seunghoon.adapter.SkinAdapter;
import kr.pknu.s202112246_lee_seunghoon.model.BeerSkin;

public class ShopActivity extends AppCompatActivity {

    private TextView textCoinCount;
    private SharedPreferences prefs;
    private int coinCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        prefs = getSharedPreferences("to_beer_prefs", MODE_PRIVATE);
        coinCount = prefs.getInt("coin_count", 0);

        textCoinCount = findViewById(R.id.textCoinCount);
        textCoinCount.setText("💰 보유 코인: " + coinCount + "개");

        // 코인 치트 (롱클릭) 임시
        textCoinCount.setOnLongClickListener(v -> {
            coinCount += 50;  // 한번에 50개씩 충전
            prefs.edit()
                    .putInt("coin_count", coinCount)
                    .apply();

            textCoinCount.setText("💰 보유 코인: " + coinCount + "개");
            Toast.makeText(this, "디버그: 코인 50개 지급 🪙", Toast.LENGTH_SHORT).show();
            return true;
        });

        // 메인으로 돌아가기 버튼
        ImageButton btnCloseShop = findViewById(R.id.btnCloseShop);
        btnCloseShop.setOnClickListener(v -> finish());

        setupRecyclerView();
    }

    private SkinAdapter adapter;

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerSkins);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SkinAdapter(this, BeerSkin.values(), this::onSkinClicked);
        recyclerView.setAdapter(adapter);
    }

    private void onSkinClicked(BeerSkin skin) {
        boolean purchased = prefs.getBoolean("purchased_" + skin.id, (skin.price == 0));

        if (!purchased) {
            if (coinCount < skin.price) {
                Toast.makeText(this, "코인이 부족합니다!", Toast.LENGTH_SHORT).show();
                return;
            }

            coinCount -= skin.price;
            prefs.edit()
                    .putBoolean("purchased_" + skin.id, true)
                    .putInt("coin_count", coinCount)
                    .apply();

            textCoinCount.setText("보유 코인: " + coinCount + "개");
        }

        prefs.edit()
                .putInt("selected_skin", skin.id)
                .apply();

        Toast.makeText(this, skin.name + " 스킨 선택 완료!", Toast.LENGTH_SHORT).show();

        // 상태 갱신
        if (adapter != null) {
            adapter.refreshState();
        }

        finish();
    }


}
