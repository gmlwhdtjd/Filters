package com.helloworld.bartender;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class OpenLicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String page = getIntent().getStringExtra("page");
        setContentView(R.layout.activity_open_license);
        Toast.makeText(this,page,Toast.LENGTH_LONG).show();
    }
}
