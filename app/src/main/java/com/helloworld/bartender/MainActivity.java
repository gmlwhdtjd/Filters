package com.helloworld.bartender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapturer;
import com.helloworld.bartender.FilterableCamera.FCameraView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int REQ_PICK_CODE=100;

    //첫 실행 판별
    public SharedPreferences prefs;

    //슬라이드 열기/닫기 플래그
    boolean isPageOpen = false;
    //슬라이드 열기 애니메이션
    Animation translateLeftAnim;
    //슬라이드 닫기 애니메이션
    Animation translateRightAnim;
    //슬라이드 레이아웃
    LinearLayout slidingPage01;

    ImageButton button1;
    ImageButton button2;

    private List<String> data;

    int tmp1 = 0;

    int tmp2 = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 카메라 관련 정의
        FCameraView fCameraView = findViewById(R.id.cameraView);
        FCameraCapturer fCameraCapturer = new FCameraCapturer(this);
        final FCamera fCamera = new FCamera(this, getLifecycle(), fCameraView, fCameraCapturer);

        findViewById(R.id.cameraCaptureBtt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fCamera.takePicture();
            }
        });

        findViewById(R.id.cameraSwitchingBtt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fCamera.switchCameraFacing();
            }
        });

        findViewById(R.id.cameraFlashBtt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmp1 = (tmp1 + 1) % 3 ;
                switch (tmp1) {
                    case 0:
                        ((ImageButton)v).setImageResource(R.mipmap.ic_camera_flash_auto);
                        break;
                    case 1:
                        ((ImageButton)v).setImageResource(R.mipmap.ic_camera_flash_off);
                        break;
                    case 2:
                        ((ImageButton)v).setImageResource(R.mipmap.ic_camera_flash_on);
                        break;
                }
            }
        });

        findViewById(R.id.cameraTimerBtt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmp1 = (tmp1 + 1) % 4 ;
                switch (tmp1) {
                    case 0:
                        ((ImageButton)v).setImageResource(R.mipmap.ic_camera_timer_off);
                        break;
                    case 1:
                        ((ImageButton)v).setImageResource(R.mipmap.ic_camera_timer_3);
                        break;
                    case 2:
                        ((ImageButton)v).setImageResource(R.mipmap.ic_camera_timer_5);
                        break;
                    case 3:
                        ((ImageButton)v).setImageResource(R.mipmap.ic_camera_timer_10);
                        break;
                }
            }
        });

        //oreference 정의
        prefs = getSharedPreferences("Prefs",MODE_PRIVATE);
        checkFirstRun();

        data = new ArrayList<String>();
        //data 추가
        data.add("#1");
        data.add("#2");
        data.add("#3");
        data.add("#plus");

        final horizontal_adapter RecyclerAdapter = new horizontal_adapter(data);
        RecyclerAdapter.setItemClick(new horizontal_adapter.ItemClick() {
            @Override
            public void onClick(String str, int position, int lastposition) {
                if(position == lastposition-1){
                   Intent intent=new Intent(MainActivity.this,Filter_making_page.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), position + " " + str, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //리사이클러 뷰
        RecyclerView list = (RecyclerView) findViewById(R.id.filterList);
        list.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        list.setAdapter(RecyclerAdapter);

        //슬라이딩 레이아웃
        //UI
        slidingPage01 = (LinearLayout)findViewById(R.id.slidingPage);
        button1 = (ImageButton)findViewById(R.id.FilmBtt);

        button1.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //닫기
                if(isPageOpen){
                    //애니메이션 시작
                    slidingPage01.startAnimation(translateRightAnim);
                }
                //열기
                else{
                    slidingPage01.setVisibility(View.VISIBLE);
                    slidingPage01.startAnimation(translateLeftAnim);
                }

            }
        });


        //애니메이션
        translateLeftAnim = AnimationUtils.loadAnimation(this, R.anim.translate_left);
        translateRightAnim = AnimationUtils.loadAnimation(this, R.anim.translate_right);

        //애니메이션 리스너 설정
        SlidingPageAnimationListener animationListener = new SlidingPageAnimationListener();
        translateLeftAnim.setAnimationListener(animationListener);
        translateRightAnim.setAnimationListener(animationListener);
    }

    //첫 실행 판독 함수
    public void checkFirstRun(){
    boolean isFirstRun = prefs.getBoolean("isFirstRun",true);
    if(isFirstRun){


        SQLiteDatabase sqliteDB = null;
        //db생성
//
//        try {
//            sqliteDB = SQLiteDatabase.openOrCreateDatabase("filter_list.db", null) ;
//        } catch (SQLiteException e) {
//            e.printStackTrace() ;
//        }
//
//        try {
//            String sqlCreateTbl = "CREATE TABLE IF NOT EXISTS ORDER_T (NO INTEGER, NAME TEXT, ATTRIBUTE TEXT)";
//            sqliteDB.execSQL(sqlCreateTbl);
//            String sqlInsert = "INSERT INTO ORDER_T (NO, NAME) VALUES (1, 'test','sample')";
//            sqliteDB.execSQL(sqlInsert);
//        }catch (SQLiteException e){
//            e.printStackTrace();
//        }
//        final DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
//
//         Log.e("jkjk","created!!");

        Intent guideIntent = new Intent(MainActivity.this,Guide_page.class);
        startActivity(guideIntent);

        prefs.edit().putBoolean("isFirstRun",false).apply();
    }
    }


    //갤러리 이동
    public void onGalleryBttClicked(View v) {
        Intent pickerIntent = new Intent(Intent.ACTION_PICK);

        pickerIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

        pickerIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(pickerIntent, REQ_PICK_CODE);
    }
//
//    //슬라이딩 버튼 닫기 오픈
//    public void onArrowButton1Clicked(View v){
//        //닫기
//        if(isPageOpen){
//            //애니메이션 시작
//            slidingPage01.startAnimation(translateRightAnim);
//        }
//        //열기
//        else{
//            slidingPage01.setVisibility(View.VISIBLE);
//            slidingPage01.startAnimation(translateLeftAnim);
//        }
//    }

    //슬라이딩 페이지 애니메이션 리스너
    private class SlidingPageAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationEnd(Animation animation) {
            //슬라이드 열기->닫기
            if(isPageOpen){
                slidingPage01.setVisibility(View.GONE);

                isPageOpen = false;
            }
            //슬라이드 닫기->열기
            else{
                isPageOpen = true;
            }
        }
        @Override
        public void onAnimationRepeat(Animation animation) {

        }
        @Override
        public void onAnimationStart(Animation animation) {

        }
    }

    //필터 아이콘 클릭 이벤트
    public void onFilterIconClicked(View v){

    }


}
