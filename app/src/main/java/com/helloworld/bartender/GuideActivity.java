package com.helloworld.bartender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class GuideActivity extends AppCompatActivity {
    //첫 실행 판별
    public SharedPreferences prefs;
    public View guideView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        //oreference 정의
        guideView = findViewById(R.id.mainView);
        guideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent guideIntent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(guideIntent);
            }
        });
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
        checkFirstRun();
    }

    public void checkFirstRun() {
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        if (!isFirstRun) {
            Intent guideIntent = new Intent(GuideActivity.this, MainActivity.class);
            startActivity(guideIntent);
            finish();
        }
    }


}
