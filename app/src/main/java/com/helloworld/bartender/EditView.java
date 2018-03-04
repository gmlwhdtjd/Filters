package com.helloworld.bartender;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;

/**
 * Created by huijonglee on 2018. 2. 27..
 */

public class EditView extends CoordinatorLayout {

    BottomSheetBehavior bottomSheetBehavior;
    FCameraFilter mFilter;

    TextView filterNameView;

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

        // TODO : 변수 세팅

        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        filterNameView = findViewById(R.id.filterNameView);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setPeekHeight(0); //peek = 0로 하면 첫 화면에서 안보임

        findViewById(R.id.editCloseBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();
            }
        });

        findViewById(R.id.editSaveBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : Save
            }
        });
    }

    public void changeState() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    public void setFilter(FCameraFilter filter) {
        mFilter = filter;
        filterNameView.setText(mFilter.getName());

        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        // TODO : 페이지에 따라 nameSeekbar 추가해야함
        if (filter instanceof OriginalFilter) {

            FrameLayout tabContent = findViewById(android.R.id.tabcontent);

            for (final OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {

                LinearLayout tab = new LinearLayout(getContext());
                tab.setId(View.generateViewId());
                tab.setGravity(Gravity.CENTER);
                tabContent.addView(tab);

                TabHost.TabSpec tmpTabSpec = tabHost.newTabSpec("Tab Spec" + valueType.toString());
                tmpTabSpec.setContent(tab.getId());
                tmpTabSpec.setIndicator(valueType.toString());
                tabHost.addTab(tmpTabSpec);

                NamedSeekBar tmpSeekBar = new NamedSeekBar(getContext());
                tmpSeekBar.setText(valueType.toString());
                tmpSeekBar.setValue(mFilter.getValueWithType(valueType));
                tmpSeekBar.setOnChangeListener(new NamedSeekBar.OnChangeListener() {

                    @Override
                    public void onValueChanged(int value) {
                        mFilter.setValueWithType(valueType, value);
                    }
                });
                tab.addView(tmpSeekBar);
            }
        }
    }
}
