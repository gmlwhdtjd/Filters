package com.helloworld.bartender.Edit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.FilterList.FilterListView;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by huijonglee on 2018. 2. 27..
 */

public class EditView extends CoordinatorLayout {

    private boolean isOpen;
    BottomSheetBehavior bottomSheetBehavior;
    FCameraFilter mFilter;
    DatabaseHelper dbHelper;

    TextView editNameView;

    OnSaveListener mOnSaveListener;
    Queue<Integer> backupValues;

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
        isOpen = false;

        // TODO : 변수 세팅

        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

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

        editNameView = findViewById(R.id.editNameView);
        editNameView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Change Filter Name");

                float dp = getResources().getDisplayMetrics().density;

                FrameLayout changeView = new FrameLayout(getContext());
                changeView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                changeView.setPadding((int) (24 * dp), (int) (5 * dp), (int) (24 * dp), (int) (5 * dp));

                final EditText input = new EditText(getContext());
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(9);
                input.setFilters(FilterArray);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                changeView.addView(input);

                builder.setView(changeView);

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFilter.setName(input.getText().toString());
                        editNameView.setText(mFilter.getName());
                    }
                });
                builder.setNegativeButton("Cancel", null);

                builder.show();
            }
        });

        findViewById(R.id.editCloseBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();

                if (mFilter instanceof RetroFilter) {
                    for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                        mFilter.setValueWithType(valueType, backupValues.poll());
                    }
                }
            }
        });

        findViewById(R.id.editSaveBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();

                //update
                FilterListView filterListView = ((MainActivity) getContext()).findViewById(R.id.filterListView);
                int Id = dbHelper.saveFilter(mFilter);
                if (mFilter.getId() == null) {
                    filterListView.getHorizontalAdapter().addItem(NewFilter(mFilter, Id), filterListView.getHorizontalAdapter().getItemCount() - 1);
                } else {
                    filterListView.getHorizontalAdapter().updateItem(mFilter);
                }
                if (mOnSaveListener != null)
                    mOnSaveListener.onSaved();
            }
        });
    }

    public void changeState() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            isOpen = true;
            backupValues = new LinkedList<>();

            if (mFilter instanceof RetroFilter) {
                for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                    backupValues.add(mFilter.getValueWithType(valueType));
                }
            }
        } else {
            isOpen = false;
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    public void setOnSaveListener(OnSaveListener onSaveListener) {
        mOnSaveListener = onSaveListener;
    }

    public void setFilter(FCameraFilter filter) {
        mFilter = filter;
        editNameView.setText(mFilter.getName());

        TabHost tabHost = findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.clearAllTabs();

        FrameLayout tabContent = findViewById(android.R.id.tabcontent);
        tabContent.removeAllViews();
        HashMap<String, LinearLayout> tabs = new HashMap<>();

        if (mFilter instanceof OriginalFilter) {
            editNameView.setClickable(false);

            LinearLayout tab = new LinearLayout(getContext());
            tab.setId(View.generateViewId());
            tab.setGravity(Gravity.CENTER);
            tab.setOrientation(LinearLayout.VERTICAL);
            tabContent.addView(tab);

            TabHost.TabSpec tmpTabSpec = tabHost.newTabSpec("Tab Spec Original");
            tmpTabSpec.setContent(tab.getId());
            tmpTabSpec.setIndicator("Original");
            tabHost.addTab(tmpTabSpec);

            TextView textView = new TextView(getContext());
            textView.setText(getContext().getString(R.string.OriginalFilter_msg));
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(20);

            tab.addView(textView);
        }
        else if (mFilter instanceof RetroFilter) {
            editNameView.setClickable(true);

            for (final RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {

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
                    case SATURATION:
                        curSeekBar.setMax(50);
                        curSeekBar.setMin(-50);
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

    public FCameraFilter NewFilter(FCameraFilter filter, int Id) {
        FCameraFilter newFilter = null;
        switch (filter.getClass().getSimpleName()) {
            case "RetroFilter":
                newFilter = new RetroFilter(getContext(),Id);
                for(RetroFilter.ValueType valueType : RetroFilter.ValueType.values()){
                    newFilter.setValueWithType(valueType,filter.getValueWithType(valueType));
                }
                newFilter.setName(filter.getName());
                break;
            default:
                break;
        }
        return newFilter;
    }

    public boolean IsOpen() {
        return isOpen;
    }

}
