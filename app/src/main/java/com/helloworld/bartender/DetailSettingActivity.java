package com.helloworld.bartender;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DetailSettingActivity extends AppCompatActivity {

    TextView mDetailText;
    TextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pageCode = getIntent().getIntExtra("pageCode", 0);
        setContentView(R.layout.activity_setting_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDetailText = findViewById(R.id.detailText);
        mTitleText = findViewById(R.id.detail_title);

        mDetailText.setMovementMethod(new ScrollingMovementMethod());
        switch (pageCode) {
            case 0:

                mTitleText.setText(getString(R.string.title_open_license));
                mDetailText.setText(R.string.open_license_text);
                getSupportActionBar().setTitle(getString(R.string.title_open_license));
                break;
            case 1:

                mTitleText.setText(getString(R.string.title_terms));
                mDetailText.setText(R.string.terms_service_text);
                getSupportActionBar().setTitle(getString(R.string.title_terms));
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
