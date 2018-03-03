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

    //recyclerview
    private String option = "";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManger;
    private DatabaseHelper dbHelper;
    private horizontal_adapter adapter;

    // 카메라 관련
    private FCameraView fCameraView;
    private FCameraCapturer fCameraCapturer;
    private FCamera fCamera;

    // 카메라 캡쳐 관련
    private TextView timerTextView;
    private ImageView captureEffectImg;

    // 버튼
    private ImageButton cameraSwitchingBtt;
    private ImageButton cameraFlashBtt;
    private ImageButton cameraTimerBtt;
    private ImageButton settingBtt;

    private ImageButton galleryBtt;
    private ImageButton cameraCaptureBtt;
    private ImageButton editBtt;

    // EditView
    private EditView editView;

    //filterlist slide
    LinearLayout nestedFilterList;
    BottomSheetBehavior filterListBehavior;

    //
    FCameraFilter cameraFilter;

    int tmpColor = 255;

    int tmp1 = 0;

    int timerStatus = 0;
    int timerValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 카메라 관련
        fCameraView = findViewById(R.id.cameraView);
        fCameraCapturer = new FCameraCapturer(this);
        fCamera = new FCamera(this, getLifecycle(), fCameraView, fCameraCapturer);

        // 카메라 캡쳐 관련
        timerTextView = findViewById(R.id.timerNumberText);
        captureEffectImg = findViewById(R.id.captureEffectImg);

        // 버튼
        cameraSwitchingBtt = findViewById(R.id.cameraSwitchingBtt);
        cameraFlashBtt = findViewById(R.id.cameraFlashBtt);
        cameraTimerBtt = findViewById(R.id.cameraTimerBtt);
        settingBtt = findViewById(R.id.settingBtt);

        galleryBtt = findViewById(R.id.galleryBtt);
        cameraCaptureBtt = findViewById(R.id.cameraCaptureBtt);
        editBtt = findViewById(R.id.editBtt);

        // EditView
        editView = findViewById(R.id.editView);

        // 상단 버튼 세팅
        cameraSwitchingBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fCamera.switchCameraFacing();
            }
        });

        cameraFlashBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmp1 = (tmp1 + 1) % 3;
                switch (tmp1) {
                    case 0:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_flash_auto);
                        break;
                    case 1:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_flash_off);
                        break;
                    case 2:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_flash_on);
                        break;
                }
            }
        });

        cameraTimerBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timerStatus = (timerStatus + 1) % 4;
                switch (timerStatus) {
                    case 0:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_off);
                        timerValue = 0;
                        break;
                    case 1:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_3);
                        timerValue = 3;
                        break;
                    case 2:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_5);
                        timerValue = 5;
                        break;
                    case 3:
                        ((ImageButton) v).setImageResource(R.drawable.ic_camera_timer_10);
                        timerValue = 10;
                        break;
                }
            }
        });

        // TODO : settingActivity
        settingBtt.setOnClickListener(null);


        //하단 버튼 세팅
        galleryBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickerIntent = new Intent(Intent.ACTION_PICK);

                pickerIntent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);

                pickerIntent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(pickerIntent, REQ_PICK_CODE);

//              dbHelper.saveFilter(new Item("last",0.5f,0.5f,0.5f,0.5f,0.5f));
            }
        });

        cameraCaptureBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCaptureBtt.setClickable(false);

                new CountDownTimer(timerValue * 1000 - 1, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timerTextView.setText(String.valueOf((millisUntilFinished / 1000) + 1));

                        Animation countDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.count_down_effect);
                        timerTextView.startAnimation(countDown);
                    }

                    @Override
                    public void onFinish() {
                        timerTextView.setText("");

                        fCamera.takePicture();

                        Animation captuer = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.capture_effect);
                        captureEffectImg.startAnimation(captuer);

                        cameraCaptureBtt.setClickable(true);

                        // Todo: 임시적인 효과를 위한 코드 -> 나중에 지우고 다른 부분에서 구현할 필요가 있음
                        ((ImageView) findViewById(R.id.cameraCaptureInnerImg)).setColorFilter(Color.argb(150, tmpColor, tmpColor, tmpColor));
                        tmpColor = (tmpColor + 50) % 255;
                    }
                }.start();
            }
        });

        editBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editView.changeState();
            }
        });

        // 초기 필터 세팅
        cameraFilter = new OriginalFilter(this, 1);

        fCameraView.setFilter(cameraFilter);
        fCameraCapturer.setFilter(cameraFilter);
        ((EditView) findViewById(R.id.editView)).setFilter(cameraFilter);

        //------------------------------------------------------------------------------------------

        //filterList
        nestedFilterList = findViewById(R.id.filter_list);
        filterListBehavior = BottomSheetBehavior.from(nestedFilterList);

        //리사이클러 뷰
        mRecyclerView = (RecyclerView) findViewById(R.id.filterList);
        //  mRecyclerView.setHasFixedSize(true);
        mLayoutManger = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManger);

        populateRecyclerView(option);
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);

//        button1 = (ImageButton) findViewById(R.id.editBtt);
//
//        button1.setOnClickListener(new OnSingleClickListener() {
//            @Override
//            public void onSingleClick(View v) {
//                if(filterListBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
//                    filterListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//                }else{
//                    filterListBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                }
//
//            }
//        });

        filterListBehavior.setPeekHeight(100);
    }


    //populate recyclerview
    private void populateRecyclerView(String option) {
        dbHelper = new DatabaseHelper(this);
        adapter = new horizontal_adapter(dbHelper.getFilterList(this, option), this, mRecyclerView);
        mRecyclerView.setAdapter(adapter);
    }
}

