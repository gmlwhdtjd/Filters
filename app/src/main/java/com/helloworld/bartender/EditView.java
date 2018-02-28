package com.helloworld.bartender;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;

import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;

/**
 * Created by huijonglee on 2018. 2. 27..
 */

public class EditView extends CoordinatorLayout {
    public EditView(Context context) {
        super(context);
        init(null, 0);
    }

    public EditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_edit_view, this);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NamedSeekBar, defStyle, 0);

        // Todo: 변수 세팅

        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //bottomslide
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));

        //bottomSlider
        //peek = 0로 하면 첫 화면에서 안보임
        bottomSheetBehavior.setPeekHeight(50);

        //bottom slide event handling
        //slide를 내려서 상태가 바뀔때
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

    }

    OriginalFilter originalFilter;
    public void setFilter(FCameraFilter filter) {
        originalFilter  = (OriginalFilter) filter;

        //seekBar
        NamedSeekBar seekBarBlur = findViewById(R.id.SeekBarBlur);
        seekBarBlur.setValue(originalFilter.getValueWithType(OriginalFilter.ValueType.BLUR));
        seekBarBlur.setOnChangeListener(new NamedSeekBar.OnChangeListener() {
            @Override
            public void onValueChanged(int value) {
                originalFilter.setValueWithType(OriginalFilter.ValueType.BLUR, value);
            }
        });

        NamedSeekBar seekBarFocus = findViewById(R.id.SeekBarFocus);
        seekBarFocus.setValue(originalFilter.getValueWithType(OriginalFilter.ValueType.FOCUS));
        seekBarFocus.setOnChangeListener(new NamedSeekBar.OnChangeListener() {
            @Override
            public void onValueChanged(int value) {
                originalFilter.setValueWithType(OriginalFilter.ValueType.FOCUS, value);
            }
        });

        NamedSeekBar seekBarAberation = findViewById(R.id.SeekBarAberation);
        seekBarAberation.setValue(originalFilter.getValueWithType(OriginalFilter.ValueType.ABERRATION));
        seekBarAberation.setOnChangeListener(new NamedSeekBar.OnChangeListener() {
            @Override
            public void onValueChanged(int value) {
                originalFilter.setValueWithType(OriginalFilter.ValueType.ABERRATION, value);
            }
        });

        NamedSeekBar seekBarNoiseSize = findViewById(R.id.SeekBarNoiseSize);
        seekBarNoiseSize.setValue(originalFilter.getValueWithType(OriginalFilter.ValueType.NOISE_SIZE));
        seekBarNoiseSize.setOnChangeListener(new NamedSeekBar.OnChangeListener() {
            @Override
            public void onValueChanged(int value) {
                originalFilter.setValueWithType(OriginalFilter.ValueType.NOISE_SIZE, value);
            }
        });

        NamedSeekBar seekBarNoiseIntesity = findViewById(R.id.SeekBarNoiseIntesity);
        seekBarNoiseIntesity.setValue(originalFilter.getValueWithType(OriginalFilter.ValueType.NOISE_INTENSITY));
        seekBarNoiseIntesity.setOnChangeListener(new NamedSeekBar.OnChangeListener() {
            @Override
            public void onValueChanged(int value) {
                originalFilter.setValueWithType(OriginalFilter.ValueType.NOISE_INTENSITY, value);
            }
        });

        //탭 메뉴
        TabHost tabHost1 = (TabHost) findViewById(R.id.tabHost);
        tabHost1.setup();
        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
        TabHost.TabSpec ts1 = tabHost1.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.tab1);
        ts1.setIndicator("Blur");
        tabHost1.addTab(ts1);
        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        TabHost.TabSpec ts2 = tabHost1.newTabSpec("Tab Spec 1");
        ts2.setContent(R.id.tab2);
        ts2.setIndicator("Focus");
        tabHost1.addTab(ts2);
        // 세 번째 Tab. (탭 표시 텍스트:"TAB 3"), (페이지 뷰:"content3")
        TabHost.TabSpec ts3 = tabHost1.newTabSpec("Tab Spec 3");
        ts3.setContent(R.id.tab3);
        ts3.setIndicator("Aberation");
        tabHost1.addTab(ts3);

        TabHost.TabSpec ts4 = tabHost1.newTabSpec("Tab Spec 4");
        ts4.setContent(R.id.tab4);
        ts4.setIndicator("NoiseSize");
        tabHost1.addTab(ts4);

        TabHost.TabSpec ts5 = tabHost1.newTabSpec("Tab Spec 5");
        ts5.setContent(R.id.tab5);
        ts5.setIndicator("NoiseIntensity");
        tabHost1.addTab(ts5);
    }
}
