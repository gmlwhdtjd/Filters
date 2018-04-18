package com.helloworld.bartender;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class DetailSettingActivity extends AppCompatActivity {

    TextView mDetailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pageCode = getIntent().getIntExtra("pageCode", 0);
        setContentView(R.layout.activity_setting_detail);
        Toast.makeText(this, String.valueOf(pageCode), Toast.LENGTH_LONG).show();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDetailText = findViewById(R.id.detailText);

        switch (pageCode) {
            case 0:
                mDetailText.setText(R.string.apache_license);
                getSupportActionBar().setTitle(getString(R.string.title_open_license));
                break;
            case 1:
                mDetailText.setText("This Is Case 2");
                getSupportActionBar().setTitle(getString(R.string.title_terms));
                break;
            case 2:
                mDetailText.setText("This is Case 3");
                getSupportActionBar().setTitle(getString(R.string.title_privacy));
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
