package com.helloworld.bartender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapturer;
import com.helloworld.bartender.FilterableCamera.FCameraRenderer;
import com.helloworld.bartender.FilterableCamera.FCameraView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int REQ_PICK_CODE = 100;

    //필터 속성값들
    private SeekBar sbBlur;
    private SeekBar sbFocus;
    private SeekBar sbAberation;
    private SeekBar sbNoiseSize;
    private SeekBar sbNoiseIntensity;

    private TextView txtBlur;
    private TextView txtFocus;
    private TextView txtAberation;
    private TextView txtNoiseSize;
    private TextView txtNoiseIntensity;

    public float BlurVal;
    public float FocusVal;
    public float AberationVal;
    public float NoiseSizeVal;
    public float NoiseIntensityVal;

    //bottom slide
    NestedScrollView bottomSheet;
    BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout bottomLinear;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //seekBar
        sbBlur = (SeekBar) findViewById(R.id.sbBlur);
        sbFocus = (SeekBar) findViewById(R.id.sbFocus);
        sbAberation = (SeekBar) findViewById(R.id.sbAberation);
        sbNoiseSize = (SeekBar) findViewById(R.id.sbNoiseSize);
        sbNoiseIntensity = (SeekBar) findViewById(R.id.sbNoiseIntensity);

        txtBlur = (TextView) findViewById(R.id.blurVal);
        txtFocus = (TextView) findViewById(R.id.focusVal);
        txtAberation = (TextView) findViewById(R.id.aberationVal);
        txtNoiseSize = (TextView) findViewById(R.id.noiseSizeVal);
        txtNoiseIntensity = (TextView) findViewById(R.id.noiseIntensityVal);

        sbBlur.setProgress(Math.round(FCameraRenderer.FilterVar.getBlur() * 100));
        txtBlur.setText(Float.toString(FCameraRenderer.FilterVar.getBlur()));

        sbFocus.setProgress(Math.round(FCameraRenderer.FilterVar.getFocus() * 100));
        txtFocus.setText(Float.toString(FCameraRenderer.FilterVar.getFocus()));

        sbAberation.setProgress(Math.round(FCameraRenderer.FilterVar.getAberration() * 100));
        txtAberation.setText(Float.toString(FCameraRenderer.FilterVar.getAberration()));

        sbNoiseSize.setProgress(Math.round(FCameraRenderer.FilterVar.getNoiseSize() * 100 - 25));
        txtNoiseSize.setText(Float.toString(FCameraRenderer.FilterVar.getNoiseSize()));

        sbNoiseIntensity.setProgress(Math.round(FCameraRenderer.FilterVar.getNoiseIntensity() * 100));
        txtNoiseIntensity.setText(Float.toString(FCameraRenderer.FilterVar.getNoiseIntensity()));

        sbBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                BlurVal = (float) seekBar.getProgress() / 100;
                FCameraRenderer.FilterVar.setBlur(BlurVal);
                update(txtBlur, BlurVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                BlurVal = (float) seekBar.getProgress() / 100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                BlurVal = (float) seekBar.getProgress() / 100;
            }
        });

        sbFocus.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FocusVal = (float) seekBar.getProgress() / 100;
                FCameraRenderer.FilterVar.setFocus(FocusVal);
                update(txtFocus, FocusVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                FocusVal = (float) seekBar.getProgress() / 100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FocusVal = (float) seekBar.getProgress() / 100;
            }
        });

        sbAberation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                AberationVal = (float) seekBar.getProgress() / 100;
                FCameraRenderer.FilterVar.setAberration(AberationVal);
                update(txtAberation, AberationVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                AberationVal = (float) seekBar.getProgress() / 100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AberationVal = (float) seekBar.getProgress() / 100;
            }
        });

        sbNoiseSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                NoiseSizeVal = (float) (seekBar.getProgress() + 25) / 100;
                FCameraRenderer.FilterVar.setNoiseSize(NoiseSizeVal);
                update(txtNoiseSize, NoiseSizeVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                NoiseSizeVal = (float) (seekBar.getProgress() + 25) / 100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                NoiseSizeVal = (float) (seekBar.getProgress() + 25) / 100;
            }
        });

        sbNoiseIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                NoiseIntensityVal = (float) seekBar.getProgress() / 100;
                FCameraRenderer.FilterVar.setNoiseIntensity(NoiseIntensityVal);
                update(txtNoiseIntensity, NoiseIntensityVal);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                NoiseIntensityVal = (float) seekBar.getProgress() / 100;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                NoiseIntensityVal = (float) seekBar.getProgress() / 100;
            }
        });

        //bottomslide
        bottomSheet = (NestedScrollView) findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomLinear = (LinearLayout) findViewById(R.id.bottomLinear);

        FCameraView fCameraView = findViewById(R.id.cameraView);
        FCameraCapturer fCameraCapturer = new FCameraCapturer(this);
        final FCamera fCamera = new FCamera(this, getLifecycle(), fCameraView, fCameraCapturer);

        findViewById(R.id.cameraBtt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fCamera.takePicture();
            }
        });

        findViewById(R.id.imgBtt1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fCamera.setCameraFacing(FCamera.CameraFacing.Front);
            }
        });

        findViewById(R.id.imgBtt2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fCamera.setCameraFacing(FCamera.CameraFacing.Back);
            }
        });

        //oreference 정의
        prefs = getSharedPreferences("Prefs", MODE_PRIVATE);
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
                if (position == lastposition - 1) {
//                    Intent intent = new Intent(MainActivity.this, BottomPanelUPTest.class);
//                    startActivity(intent);
                    //meaning bottomsheet state
                    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        bottomLinear.setVisibility(View.VISIBLE);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), position + " " + str, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //리사이클러 뷰
        RecyclerView list = (RecyclerView) findViewById(R.id.filterList);
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        list.setAdapter(RecyclerAdapter);

        //슬라이딩 레이아웃
        //UI
        slidingPage01 = (LinearLayout) findViewById(R.id.slidingPage);
        button1 = (ImageButton) findViewById(R.id.FilmBtt);

        button1.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                //닫기
                if (isPageOpen) {
                    //애니메이션 시작
                    slidingPage01.startAnimation(translateRightAnim);
                }
                //열기
                else {
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

        //bottomSlider
        //peek = 0로 하면 첫 화면에서 안보임
        bottomSheetBehavior.setPeekHeight(50);

        //탭 메뉴
        TabHost tabHost1 = (TabHost) findViewById(R.id.tabHost1);
        tabHost1.setup();
        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
        TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.content1);
        ts1.setIndicator("Blur");
        tabHost1.addTab(ts1);
        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 2");
        ts2.setContent(R.id.content2);
        ts2.setIndicator("Focus");
        tabHost1.addTab(ts2);
        // 세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
        TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3");
        ts3.setContent(R.id.content3);
        ts3.setIndicator("Aberation");
        tabHost1.addTab(ts3);

        TabHost.TabSpec ts4 = tabHost1.newTabSpec("Tab Spec 4");
        ts4.setContent(R.id.content4);
        ts4.setIndicator("NoiseSize");
        tabHost1.addTab(ts4);

        TabHost.TabSpec ts5 = tabHost1.newTabSpec("Tab Spec 5");
        ts5.setContent(R.id.content5);
        ts5.setIndicator("NoiseIntensity");
        tabHost1.addTab(ts5);

        //bottom slide event handling
        //slide를 내려서 상태가 바뀔때
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
              //      bottomLinear.setVisibility(View.INVISIBLE);
                    if (isPageOpen) {
                        slidingPage01.startAnimation(translateRightAnim);
                    }
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomLinear.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


    }

    //첫 실행 판독 함수
    public void checkFirstRun() {
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        if (isFirstRun) {


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

            Intent guideIntent = new Intent(MainActivity.this, Guide_page.class);
            startActivity(guideIntent);

            prefs.edit().putBoolean("isFirstRun", false).apply();
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
            if (isPageOpen) {
                slidingPage01.setVisibility(View.GONE);

                isPageOpen = false;
            }
            //슬라이드 닫기->열기
            else {
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
//
//    //필터 아이콘 클릭 이벤트
//    public void onFilterIconClicked(View v) {
//
//
//    }

    public void update(TextView txt, float num) {
        txt.setText(new StringBuilder().append(num));

    }


}

