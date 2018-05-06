package com.helloworld.bartender.FilterList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageButton;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;
import com.helloworld.bartender.FilterList.HorizontalAdapter.ItemTouchHelperCallback;
import com.helloworld.bartender.FilterList.HorizontalAdapter.horizontal_adapter;

import android.os.Handler;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;

/**
 * Created by huijonglee on 2018. 3. 5..
 */

public class FilterListView extends CoordinatorLayout {

    private String option = "";

    private ItemTouchHelper mItemTouchHelper;
    private BottomSheetBehavior filterListBehavior;
    private RecyclerView filterList;
    private LinearLayoutManager mLayoutManger;
    private ImageButton filterListBtt;
    private horizontal_adapter adapter;
    private SnapHelper snapHelper;

    private static final String FILTERLIST_FIRST_INTRO = "filterlist_first_intro";

    public FilterListView(Context context) {
        super(context);
        init(null, 0);
    }

    public FilterListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FilterListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_filter_list_view, this);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NamedSeekBar, defStyle, 0);

        // TODO : 변수 세팅

        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //filterList
        filterListBehavior = BottomSheetBehavior.from(findViewById(R.id.filterListLayout));

        filterList = (RecyclerView) findViewById(R.id.filterList);
        mLayoutManger = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        filterList.setLayoutManager(mLayoutManger);
        int resId = R.anim.layout_filter_list_slide;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        filterList.setLayoutAnimation(animation);
        filterList.setHasFixedSize(true);
        populateRecyclerView(option);

        snapHelper = new GravitySnapHelper(Gravity.START);
        snapHelper.attachToRecyclerView(filterList);



        filterListBtt = findViewById(R.id.filterListBtt);
        filterListBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();
            }
        });

        filterListBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {

                    filterListBtt.setImageResource(R.drawable.ic_up_to_down);
                    filterListBtt.setBackgroundResource(R.drawable.ic_down_shadow);
                    ((Animatable) filterListBtt.getDrawable()).start();
                    filterList.smoothScrollToPosition(adapter.getItemCount());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity)getContext()).showIntro(filterList.getChildAt(filterList.getChildCount() - 1), FILTERLIST_FIRST_INTRO, "You can make your own filter by clicking plus button.", Focus.NORMAL,null, ShapeType.CIRCLE);
                       }
                    }, 300);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    filterListBtt.setImageResource(R.drawable.ic_down_to_up);
                    filterListBtt.setBackgroundResource(R.drawable.ic_up_shadow);
                    ((Animatable) filterListBtt.getDrawable()).start();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }


    public int getState() {
       return filterListBehavior.getState();
    }

    public void changeState() {
        if (filterListBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            filterListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//            filterListBtt.setImageResource(R.drawable.ic_up_to_down);
//            filterListBtt.setBackgroundResource(R.drawable.ic_down_shadow);
//            ((AnimatedVectorDrawable) filterListBtt.getDrawable()).start();
        } else {
            filterListBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//            filterListBtt.setImageResource(R.drawable.ic_down_to_up);
//            filterListBtt.setBackgroundResource(R.drawable.ic_up_shadow);
//            ((AnimatedVectorDrawable) filterListBtt.getDrawable()).start();
        }
    }

    //populate recyclerview
    public void populateRecyclerView(String option) {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        adapter = new horizontal_adapter(dbHelper.getFilterList(option), getContext(), filterList, mLayoutManger);
        filterList.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(filterList);
    }

    public horizontal_adapter getHorizontalAdapter() {
        return adapter;
    }

    public View getFilterListBtt(){
        return filterListBtt;
    }

}
