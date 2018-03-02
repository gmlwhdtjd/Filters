package com.helloworld.bartender;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;

/**
 * Created by huijonglee on 2018. 2. 27..
 */

public class EditView extends CoordinatorLayout {

    BottomSheetBehavior bottomSheetBehavior;
    FCameraFilter mFilter;

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

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));

        //bottomSlider
        //peek = 0로 하면 첫 화면에서 안보임
        bottomSheetBehavior.setPeekHeight(50);

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

        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();

        if (filter instanceof OriginalFilter) {

            FrameLayout tabContent = findViewById(android.R.id.tabcontent);

            for (final OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {

                LinearLayout tab = new LinearLayout(getContext());
                tab.setId(View.generateViewId());
                tabContent.addView(tab);

                TabHost.TabSpec tmpTabSpec = tabHost.newTabSpec("Tab Spec" + valueType.toString());
                tmpTabSpec.setContent(tab.getId());
                tmpTabSpec.setIndicator(valueType.toString());
                tabHost.addTab(tmpTabSpec);

                NamedSeekBar tmpSeekBar = new NamedSeekBar(getContext());
                tmpSeekBar.setText(valueType.toString());
                tmpSeekBar.setValue(mFilter.getValueWithType(valueType));
                tmpSeekBar.setOnChangeListener(new NamedSeekBar.OnChangeListener() {
                    OriginalFilter.ValueType type = valueType;

                    @Override
                    public void onValueChanged(int value) {
                        mFilter.setValueWithType(type, value);
                    }
                });
                tab.addView(tmpSeekBar);
            }
        }
    }
}
