package com.helloworld.bartender.Edit;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.Edit.viewpager.CustomViewPagerItem;
import com.helloworld.bartender.Edit.viewpager.CustomViewPagerItemAdapter;
import com.helloworld.bartender.Edit.viewpager.CustomViewPagerItems;
import com.helloworld.bartender.FilterList.FilterListView;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cn.pedant.SweetAlert.SweetAlertDialog;
import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.ShapeType;

/**
 * Created by huijonglee on 2018. 2. 27..
 */

public class EditView extends CoordinatorLayout {

    private boolean isOpen;

    private List<NamedSeekBar> namedSeekBars;
    BottomSheetBehavior bottomSheetBehavior;
    FCameraFilter mFilter;
    DatabaseHelper dbHelper;

    private TextView editNameView;
    private ViewPager viewPager;
    private SmartTabLayout viewPagerTab;

    private OnSaveListener mOnSaveListener;
    private Queue<Integer> backupValues;
    private String backupName;


    private boolean filterListViewWasOpen = false;

    private final static String EDIT_FIRST_INTRO = "firstIntro";
    private final static String EDIT_SECOND_INTRO = "secondIntro";
    private final static String EDIT_THIRD_INTRO = "thirdIntro";
    private final static String EDIT_FORTH_INTRO = "forthIntro";


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

        try {
            dbHelper = new DatabaseHelper(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

                final EditText input = new EditText(getContext());
                InputFilter[] FilterArray = new InputFilter[1];
                FilterArray[0] = new InputFilter.LengthFilter(9);
                input.setFilters(FilterArray);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                float dp = getResources().getDisplayMetrics().density;
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(getContext().getString(R.string.edit_name_popup_title))
                        .setConfirmText(getContext().getString(R.string.edit_name_popup_confirm))
                        .setCancelText(getContext().getString(R.string.edit_name_popup_cancel))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                if (input.getText().toString().replace(" ", "").equals("")) {
                                    sweetAlertDialog.dismissWithAnimation();
                                } else {
                                    mFilter.setName(input.getText().toString());
                                    editNameView.setText(mFilter.getName());
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            }
                        });
                dialog.show();
                LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.loading);
                linearLayout.setPadding((int) (24 * dp), (int) (24 * dp), (int) (24 * dp), (int) (24 * dp));
                int index = linearLayout.indexOfChild(linearLayout.findViewById(R.id.content_text));
                linearLayout.addView(input, index + 1);
            }
        });

        findViewById(R.id.editCloseBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        findViewById(R.id.editSaveBtt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();

                //update
                if (!(mFilter instanceof OriginalFilter)) {
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
            }
        });
    }

    public void changeState() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            isOpen = true;
            backupValues = new LinkedList<>();

            FilterListView filterListView = ((MainActivity) getContext()).findViewById(R.id.filterListView);
            if (filterListView.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                filterListView.changeState();
                filterListViewWasOpen = true;
            } else
                filterListViewWasOpen = false;

            if (mFilter instanceof RetroFilter) {
                for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                    backupValues.add(mFilter.getValueWithType(valueType));
                }

                //intro
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) getContext()).showIntro(editNameView, EDIT_FIRST_INTRO, getContext().getString(R.string.edit_first), Focus.NORMAL, materialIntroListener, ShapeType.CIRCLE);
                    }
                }, 400);
            }

        } else {
            isOpen = false;
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (filterListViewWasOpen) {
                FilterListView filterListView = ((MainActivity) getContext()).findViewById(R.id.filterListView);
                filterListView.changeState();
                filterListViewWasOpen = false;
            }
        }
    }

    public void setOnSaveListener(OnSaveListener onSaveListener) {
        mOnSaveListener = onSaveListener;
    }

    public void setFilter(FCameraFilter filter) {
        mFilter = filter;
        editNameView.setText(mFilter.getName());

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);

        backupName = mFilter.getName();

        namedSeekBars = new LinkedList<>();
        HashMap<String, LinearLayout> tabs = new HashMap<>();
        CustomViewPagerItems tabItems = new CustomViewPagerItems(getContext()).with(getContext()).create();

        if (mFilter instanceof OriginalFilter) {
            editNameView.setClickable(false);
            LinearLayout tab = new LinearLayout(getContext());
            tab.setId(View.generateViewId());
            tab.setGravity(Gravity.CENTER);
            tab.setOrientation(LinearLayout.VERTICAL);
//            tabContent.addView(tab);

            TextView textView = new TextView(getContext());
            textView.setText(getContext().getString(R.string.OriginalFilter_msg));
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(20);
            tab.addView(textView);
            tabItems.add(CustomViewPagerItem.of("Original", tab));
            viewPagerTab.setDistributeEvenly(true);
        } else if (mFilter instanceof RetroFilter) {
            editNameView.setClickable(true);
            viewPagerTab.setDistributeEvenly(false);
            for (final RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {

                if (!tabs.containsKey(valueType.getPageName(getContext()))) {
                    LinearLayout tab = new LinearLayout(getContext());
                    tab.setId(View.generateViewId());
                    tab.setGravity(Gravity.CENTER);
                    tab.setOrientation(LinearLayout.VERTICAL);
                    tabItems.add(CustomViewPagerItem.of(valueType.getPageName(getContext()), tab));
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
                    case BRIGHTNESS:
                        curSeekBar.setMax(50);
                        curSeekBar.setMin(-50);
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


                namedSeekBars.add(curSeekBar);
                tabs.get(valueType.getPageName(getContext())).addView(curSeekBar);
            }
        }

        CustomViewPagerItemAdapter adapter = new CustomViewPagerItemAdapter(tabItems);
        viewPager.setAdapter(adapter);
        viewPagerTab.setViewPager(viewPager);

    }

    public interface OnSaveListener {
        void onSaved();
    }

    public FCameraFilter NewFilter(FCameraFilter filter, int Id) {
        FCameraFilter newFilter = null;
        switch (filter.getClass().getSimpleName()) {
            case "RetroFilter":
                newFilter = new RetroFilter(getContext(), Id);
                for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                    newFilter.setValueWithType(valueType, filter.getValueWithType(valueType));
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

    public void close() {
        changeState();
        int i = 0;
        if (mFilter instanceof RetroFilter) {
            for (RetroFilter.ValueType valueType : RetroFilter.ValueType.values()) {
                mFilter.setValueWithType(valueType, backupValues.poll());
                namedSeekBars.get(i).setValue(mFilter.getValueWithType(valueType));
                mFilter.setName(backupName);
                i++;
            }
        }

        //id가 null인 필터는 임시 필터
        if(mFilter.getId()==null){
            FilterListView filterListView = ((MainActivity) getContext()).findViewById(R.id.filterListView);
            filterListView.getHorizontalAdapter().setLastSelectedPosition(0);
            filterListView.getFilterList().smoothScrollToPosition(0);
        }
    }

    MaterialIntroListener materialIntroListener = new MaterialIntroListener() {
        @Override
        public void onUserClicked(String id) {
            switch (id) {
                case EDIT_FIRST_INTRO:
                    ((MainActivity) getContext()).showIntro(viewPager, EDIT_SECOND_INTRO, getContext().getString(R.string.edit_second), Focus.NORMAL, this, ShapeType.RECTANGLE);
                    break;
                case EDIT_SECOND_INTRO:
                    ((MainActivity) getContext()).showIntro(viewPagerTab, EDIT_THIRD_INTRO, getContext().getString(R.string.edit_third), Focus.NORMAL, this, ShapeType.RECTANGLE);
                    break;
                case EDIT_THIRD_INTRO:
                    ((MainActivity) getContext()).showIntro(findViewById(R.id.editSaveBtt), EDIT_FORTH_INTRO, getContext().getString(R.string.edit_forth), Focus.NORMAL, this, ShapeType.CIRCLE);
                    break;

            }
        }
    };

}
