package com.helloworld.bartender;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.adapter.horizontal_adapter;

/**
 * Created by huijonglee on 2018. 3. 5..
 */

public class FilterListView extends CoordinatorLayout {

    private String option = "";

    private BottomSheetBehavior filterListBehavior;
    private RecyclerView filterList;
    private LinearLayoutManager mLayoutManger;
    private ImageButton filterListBtt;

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

        filterList = findViewById(R.id.filterList);
        mLayoutManger = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        filterList.setLayoutManager(mLayoutManger);

        populateRecyclerView(option);

        filterListBtt = findViewById(R.id.filterListBtt);
        filterListBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState();
            }
        });
    }

    public void changeState() {
        if (filterListBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            filterListBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            filterListBtt.setImageResource(R.drawable.ic_up_to_down);
            filterListBtt.setBackgroundResource(R.drawable.ic_down_shadow);
            ((AnimatedVectorDrawable) filterListBtt.getDrawable()).start();
        } else {
            filterListBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            filterListBtt.setImageResource(R.drawable.ic_down_to_up);
            filterListBtt.setBackgroundResource(R.drawable.ic_up_shadow);
            ((AnimatedVectorDrawable) filterListBtt.getDrawable()).start();
        }
    }

    //populate recyclerview
    public void populateRecyclerView(String option) {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        horizontal_adapter adapter = new horizontal_adapter(dbHelper.getFilterList(getContext(), option), getContext(), filterList);
        filterList.setAdapter(adapter);
    }
}
