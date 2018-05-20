package com.teambartender3.filters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.github.aakira.expandablelayout.Utils;
import com.teambartender3.filters.SettingConponents.FaqListView.FaqRecyclerAdapter;
import com.teambartender3.filters.SettingConponents.FaqListView.QuestionModel;

import java.util.ArrayList;
import java.util.List;

public class FaqActivity extends AppCompatActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, FaqActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        getSupportActionBar().setTitle(getString(R.string.title_faq));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, linearLayoutManager.getOrientation()));
        recyclerView.setLayoutManager(linearLayoutManager);


        //이곳에 question 추가
        final List<QuestionModel> data = new ArrayList<>();
        final String[] questions = getApplicationContext().getResources().getStringArray(R.array.Questions);
        final String[] answers = getApplicationContext().getResources().getStringArray(R.array.Answer);

        for (int i = 0; i < questions.length; i++) {
            data.add(new QuestionModel(questions[i], answers[i], Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)));
        }
        recyclerView.setAdapter(new FaqRecyclerAdapter(data));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
