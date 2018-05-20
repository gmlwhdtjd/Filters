package com.teambartender3.filters.FilterList.FilterRadioButton;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teambartender3.filters.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by wilybear on 2018-03-21.
 */

public class FilterRadioButton extends RelativeLayout implements RadioCheckable, GestureDetector.OnGestureListener {

    private TextView mFilterNameTextView;
    private CircleImageView mFilterImageView;

    public static final int DEFAULT_TEXT_COLOR = Color.TRANSPARENT;

    private String mFilterName;
    private Drawable mFilterImageDrawable;
    private int mFilterImageBorderColor;
    private int mCheckedImageFilter;
    private int mFilterTextColor;
    private int mCheckedFilterTextColor;
    private int mCheckedFilterImageBorderColor;

    private OnLongClickListener mOnLongClickListener;
    private OnClickListener mOnClickListener;
    private OnTouchListener mOnTouchListener;
    private boolean mChecked;
    private ArrayList<OnCheckedChangeListener> mOnCheckedChangeListeners = new ArrayList<>();
    private GestureDetector mDetector;

    /**
     * Constructor
     **/

    public FilterRadioButton(Context context) {
        super(context);
        setupView();
    }

    public FilterRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(attrs);
        setupView();
    }

    public FilterRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttributes(attrs);
        setupView();
    }

    public FilterRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        parseAttributes(attrs);
        setupView();
    }

    /**
     * Init & Inflate methods
     **/

    private void parseAttributes(AttributeSet attrs) {
        //style 지정
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FilterRadioButton, 0, 0);
        Resources resources = getContext().getResources();

        try {
            mFilterImageDrawable = a.getDrawable(R.styleable.FilterRadioButton_FilterRadioButton_filter_image);
            mFilterImageBorderColor = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_image_border_color, Color.WHITE);
            mCheckedImageFilter = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_image_filter_checked_color, Color.WHITE);
            mFilterTextColor = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_name_color, Color.LTGRAY);
            mFilterName = a.getString(R.styleable.FilterRadioButton_FilterRadioButton_filter_name);
            mCheckedFilterImageBorderColor = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_name_checked_color, Color.WHITE);
            mCheckedFilterTextColor = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_image_border_checked_color, Color.WHITE);
        } finally {
            a.recycle();
        }
    }

    // Template method
    private void setupView() {
        inflateView();
        bindView();
        setCustomTouchListener();
    }

    protected void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_filter_radio_button, this, true);
        mFilterNameTextView = (TextView) findViewById(R.id.filter_name);
        mFilterImageView = (CircleImageView) findViewById(R.id.filter_image);
    }

    protected void bindView() {
        if (mFilterTextColor != DEFAULT_TEXT_COLOR) {
            mFilterNameTextView.setTextColor(mFilterTextColor);
        }
        mFilterNameTextView.setText(mFilterName);
        mFilterImageView.setBorderColor(mFilterImageBorderColor);
        mFilterImageView.setImageDrawable(mFilterImageDrawable);
    }


    /**
     * Overriding default behavior
     **/


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = l;
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        mOnLongClickListener = l;
    }

    private void setCustomTouchListener() {
        super.setOnTouchListener(new TouchListener());
        mDetector = new GestureDetector(getContext(), new GestureListener());
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    public OnTouchListener getOnTouchListener() {
        return mOnTouchListener;
    }

    private void onTouchDown(MotionEvent motionEvent) {
        setChecked(true);
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
    }

    private void onLongClick(MotionEvent motionEvent) {
        if (mOnLongClickListener != null) {
            mOnLongClickListener.onLongClick(this);
        }
    }

    private void onTouchUp(MotionEvent motionEvent) {

    }

    /**
     * Public methods
     **/

    public void setCheckedState() {
        mFilterImageView.setBorderColor(mCheckedFilterImageBorderColor);
        mFilterNameTextView.setTextColor(mCheckedFilterTextColor);
        ColorFilter cf = new PorterDuffColorFilter(mCheckedImageFilter, PorterDuff.Mode.SRC_ATOP);
        mFilterImageView.setColorFilter(cf);
    }

    public void setNormalState() {
        mFilterImageView.setBorderColor(mFilterImageBorderColor);
        mFilterNameTextView.setTextColor(mFilterTextColor);
        mFilterImageView.clearColorFilter();
    }


    public String getFilterName() {
        return mFilterName;
    }

    public void setFilterName(String filterName) {
        mFilterName = filterName;
        mFilterNameTextView.setText(mFilterName);
    }

    public Drawable getFilterImageDrawable() {
        return mFilterImageDrawable;
    }

    public CircleImageView getCircleImageView(){return mFilterImageView; }

    public void setFilterImageDrawable(Drawable filterImageDrawable) {
        mFilterImageDrawable = filterImageDrawable;
        mFilterImageView.setImageDrawable(mFilterImageDrawable);
    }

    /**
     * Checkable implementation
     **/

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            if (!mOnCheckedChangeListeners.isEmpty()) {
                for (int i = 0; i < mOnCheckedChangeListeners.size(); i++) {
                    mOnCheckedChangeListeners.get(i).OnCheckChanged(this, mChecked);
                }
            }
            if (mChecked) {
                setCheckedState();
            } else {
                setNormalState();
            }
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (mOnLongClickListener != null) {
            mOnLongClickListener.onLongClick(this);
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    /**
     * Inner classes
     **/
    private final class TouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            return mDetector.onTouchEvent(event);
        }
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onTouchDown(e);
            return true;
        }
        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick(e);
        }


    }


}
