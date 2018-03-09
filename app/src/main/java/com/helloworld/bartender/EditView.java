package com.helloworld.bartender;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
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

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;

import java.util.HashMap;

/**
 * Created by huijonglee on 2018. 2. 27..
 */

public class EditView extends CoordinatorLayout {

    BottomSheetBehavior bottomSheetBehavior;
    FCameraFilter mFilter;
    DatabaseHelper dbHelper;

    TextView filterNameView;

    OnSaveListener mOnSaveListener;

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

        dbHelper = new DatabaseHelper(getContext());

        // TODO : 변수 세팅

        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        filterNameView = findViewById(R.id.filterNameView);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
        bottomSheetBehavior.setPeekHeight(0); //peek = 0로 하면 첫 화면에서 안보임
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        //bottomSheetBehavior.setLocked(true);

        findViewById(R.id.editCloseBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : Cancel
                changeState();
            }
        });

        findViewById(R.id.editSaveBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO : Save
                changeState();

                //update
                dbHelper.saveFilter(mFilter);

                if (mOnSaveListener != null)
                    mOnSaveListener.onSaved();
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

    public void setOnSaveListener(OnSaveListener onSaveListener) {
        mOnSaveListener = onSaveListener;
    }

    public void setFilter(FCameraFilter filter) {
        mFilter = filter;
        filterNameView.setText(mFilter.getName());

        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.clearAllTabs();

        if (filter instanceof OriginalFilter) {
            FrameLayout tabContent = findViewById(android.R.id.tabcontent);
            tabContent.removeAllViews();
            HashMap<String, LinearLayout> tabs = new HashMap<>();

            for (final OriginalFilter.ValueType valueType : OriginalFilter.ValueType.values()) {

                if (!tabs.containsKey(valueType.getPageName(getContext()))) {
                    LinearLayout tab = new LinearLayout(getContext());
                    tab.setId(View.generateViewId());
                    tab.setGravity(Gravity.CENTER);
                    tab.setOrientation(LinearLayout.VERTICAL);
                    tabContent.addView(tab);

                    TabHost.TabSpec tmpTabSpec = tabHost.newTabSpec("Tab Spec" + valueType.getPageName(getContext()));
                    tmpTabSpec.setContent(tab.getId());
                    tmpTabSpec.setIndicator(valueType.getPageName(getContext()));
                    tabHost.addTab(tmpTabSpec);

                    tabs.put(valueType.getPageName(getContext()), tab);
                }

                NamedSeekBar curSeekBar = new NamedSeekBar(getContext());
                curSeekBar.setText(valueType.getValueName(getContext()));

                switch (valueType) {
                    case RGB_R:
                        curSeekBar.setColor(getResources().getColor(R.color.seekbar_red));
                        curSeekBar.setMax(255);
                        break;
                    case RGB_G:
                        curSeekBar.setColor(getResources().getColor(R.color.seekbar_green));
                        curSeekBar.setMax(255);
                        break;
                    case RGB_B:
                        curSeekBar.setColor(getResources().getColor(R.color.seekbar_blue));
                        curSeekBar.setMax(255);
                        break;
                    default:
                        curSeekBar.setMax(100);
                }
                curSeekBar.setValue(mFilter.getValueWithType(valueType));

                curSeekBar.setOnChangeListener(new NamedSeekBar.OnChangeListener() {

                    @Override
                    public void onValueChanged(int value) {
                        mFilter.setValueWithType(valueType, value);
                    }
                });

                tabs.get(valueType.getPageName(getContext())).addView(curSeekBar);
            }
        }
    }

    public interface OnSaveListener {
        void onSaved();
    }
}
