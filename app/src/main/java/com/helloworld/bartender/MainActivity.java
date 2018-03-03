package com.helloworld.bartender;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import android.support.design.widget.BottomSheetBehavior;

import android.os.CountDownTimer;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapturer;
import com.helloworld.bartender.FilterableCamera.FCameraView;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.adapter.horizontal_adapter;


//TODO: back 키 이벤트 처리하기, 필터값 수정,삭제,저장,적용, 필터 아이콘 클릭시 체크 유지

//TODO:

public class MainActivity extends AppCompatActivity {

    public SharedPreferences prefs;
    private int REQ_PICK_CODE = 100;

    //bottom slide
    FrameLayout bottomSheet;
    BottomSheetBehavior bottomSheetBehavior;

    //filterlist slide
    LinearLayout nestedFilterList;
    BottomSheetBehavior filterListBehavior;

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

    //recyclerview
    private String option = "";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManger;
    private DatabaseHelper dbHelper;
    private horizontal_adapter adapter;

    FCameraFilter cameraFilter;
    
    int tmpColor = 255;

    int tmp1 = 0;

    int timerStatus = 0;
    int timerValue = 0;
    TextView timerTextView;

    ImageView captureEffectImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captureEffectImageView = findViewById(R.id.captureEffectImg);
        timerTextView = findViewById(R.id.timerNumberText);

        cameraFilter = new OriginalFilter(this, 1);
        final OriginalFilter originalFilter = (OriginalFilter) cameraFilter;

        // 카메라 관련 정의
        final FCameraView fCameraView = findViewById(R.id.cameraView);
        fCameraView.setFilter(cameraFilter);

        final FCameraCapturer fCameraCapturer = new FCameraCapturer(this);
        fCameraCapturer.setFilter(cameraFilter);

        final FCamera fCamera = new FCamera(this, getLifecycle(), fCameraView, fCameraCapturer);

        findViewById(R.id.cameraCaptureBtt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.cameraCaptureBtt).setClickable(false);

                new CountDownTimer(timerValue * 1000 - 1, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timerTextView.setText( String.valueOf((millisUntilFinished/1000) + 1));

                        Animation countDown = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.count_down_effect);
                        timerTextView.startAnimation(countDown);
                    }

                    @Override
                    public void onFinish() {
                        timerTextView.setText("");

                        fCamera.takePicture();

                        Animation captuer = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.capture_effect);
                        captureEffectImageView.startAnimation(captuer);

                        findViewById(R.id.cameraCaptureBtt).setClickable(true);

                        // Todo: 임시적인 효과를 위한 코드 -> 나중에 지우고 다른 부분에서 구현할 필요가 있음
                        ((ImageView)findViewById(R.id.cameraCaptureInnerImg)).setColorFilter(Color.argb(150, tmpColor, tmpColor, tmpColor));
                        tmpColor = (tmpColor + 50) % 255;
                    }
                }.start();
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
                        ((ImageButton)v).setImageResource(R.drawable.ic_camera_flash_auto);
                        break;
                    case 1:
                        ((ImageButton)v).setImageResource(R.drawable.ic_camera_flash_off);
                        break;
                    case 2:
                        ((ImageButton)v).setImageResource(R.drawable.ic_camera_flash_on);
                        break;
                }
            }
        });

        findViewById(R.id.cameraTimerBtt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerStatus = (timerStatus + 1) % 4 ;
                switch (timerStatus) {
                    case 0:
                        ((ImageButton)v).setImageResource(R.drawable.ic_camera_timer_off);
                        timerValue = 0;
                        break;
                    case 1:
                        ((ImageButton)v).setImageResource(R.drawable.ic_camera_timer_3);
                        timerValue = 3;
                        break;
                    case 2:
                        ((ImageButton)v).setImageResource(R.drawable.ic_camera_timer_5);
                        timerValue = 5;
                        break;
                    case 3:
                        ((ImageButton)v).setImageResource(R.drawable.ic_camera_timer_10);
                        timerValue = 10;
                        break;
                }
            }
        });


        ((EditView) findViewById(R.id.editView)).setFilter(originalFilter);

        //filterList
        nestedFilterList = findViewById(R.id.filter_list);
        filterListBehavior = BottomSheetBehavior.from(nestedFilterList);

        //리사이클러 뷰
        mRecyclerView= (RecyclerView) findViewById(R.id.filterList);
      //  mRecyclerView.setHasFixedSize(true);
        mLayoutManger = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mRecyclerView.setLayoutManager(mLayoutManger);

        populateRecyclerView(option);
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

        button1 = (ImageButton) findViewById(R.id.FilmBtt);

        button1.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(filterListBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                    filterListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else{
                    filterListBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

            }
        });

        filterListBehavior.setPeekHeight(100);
    }



    //populate recyclerview
    private void populateRecyclerView(String option){
        dbHelper = new DatabaseHelper(this);
        adapter = new horizontal_adapter(dbHelper.getFilterList(this,option),this,mRecyclerView);
        mRecyclerView.setAdapter(adapter);
    }

    //갤러리 이동
    public void onGalleryBttClicked(View v) {
        Intent pickerIntent = new Intent(Intent.ACTION_PICK);

        pickerIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

        pickerIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(pickerIntent, REQ_PICK_CODE);

//        dbHelper.saveFilter(new Item("last",0.5f,0.5f,0.5f,0.5f,0.5f));
    }

    public void onEndBtnClicked(View v) {
    }
}

