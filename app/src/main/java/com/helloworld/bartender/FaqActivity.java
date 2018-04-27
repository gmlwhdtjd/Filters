package com.helloworld.bartender;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.github.aakira.expandablelayout.Utils;
import com.helloworld.bartender.SettingConponents.FaqListView.FaqRecyclerAdapter;
import com.helloworld.bartender.SettingConponents.FaqListView.QuestionModel;

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,linearLayoutManager.getOrientation()));
        recyclerView.setLayoutManager(linearLayoutManager);


        //이곳에 question 추가
        final List<QuestionModel> data = new ArrayList<>();
        data.add(new QuestionModel("광각 일반 렌즈가 둘 다 있는 스마트폰을 쓰는데 광각에서 일반렌즈로 어떻게 바꾸나요?"
                ,"광각, 일반 카메라를 다 지원하고 있습니다.\n광각모드에서 상단의 카메라전환 아이콘을 한번만 누르시면 일반모드로 촬영가능하십니다.",
                Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)));
        data.add(new QuestionModel(
                "question 질문"
                ,"this place is for answer of question 여기다가 답변이 들어갑니다 \n 띄어쓰기",
                Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)));
        data.add(new QuestionModel(
                "question 질문"
                ,"this place is for answer of question 여기다가 답변이 들어갑니다 \n 띄어쓰기",
                Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)));

        data.add(new QuestionModel(
                "question 질문"
                ,"this place is for answer of question 여기다가 답변이 들어갑니다 \n 띄어쓰기",
                Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)));

        data.add(new QuestionModel(
                "question 질문"
                ,"this place is for answer of question 여기다가 답변이 들어갑니다 \n 띄어쓰기",
                Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)));


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
