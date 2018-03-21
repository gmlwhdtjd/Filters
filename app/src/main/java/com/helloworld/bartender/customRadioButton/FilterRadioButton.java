package com.helloworld.bartender.customRadioButton;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.helloworld.bartender.R;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

/**
 * Created by wilybear on 2018-03-21.
 */

public class FilterRadioButton extends RelativeLayout implements RadioCheckable {

    private TextView mFilterNameTextView;
    private CircularImageView mFilterImageView;

    public static final int DEFAULT_TEXT_COLOR = Color.TRANSPARENT;

    private String mFilterName;
    private Drawable mFilterImageDrawable;
    private int mFilterImageBorderColor;
    private int mPressedFilterImageBorderColor;
    private int mFilterTextColor;
    private Drawable minitialBackgroundDrawable;
    private int mPressedFilterTextColor;

    private OnClickListener mOnClickListener;
    private OnTouchListener mOnTouchListener;
    private boolean mChecked;
    private ArrayList<OnCheckedChangeListener> mOnCheckedChangeListners = new ArrayList<>();

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
            mPressedFilterImageBorderColor = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_image_border_pressed_color, Color.GRAY);
            mFilterTextColor = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_name_color, Color.WHITE);
            mFilterName = a.getString(R.styleable.FilterRadioButton_FilterRadioButton_filter_name);
            mPressedFilterTextColor = a.getColor(R.styleable.FilterRadioButton_FilterRadioButton_filter_name_pressed_color, Color.GRAY);
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
        mFilterImageView = (CircularImageView) findViewById(R.id.filter_image);
        minitialBackgroundDrawable = getBackground();
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

    private void setCustomTouchListener() {
        super.setOnTouchListener(new TouchListener());
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
    }

    private void onTouchUp(MotionEvent motionEvent) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
    }

    /**
     * Public methods
     **/

    public void setCheckedState() {
        mFilterImageView.setBorderColor(mPressedFilterImageBorderColor);
        mFilterNameTextView.setTextColor(mPressedFilterTextColor);
    }

    public void setNormalState() {
        mFilterImageView.setBorderColor(mFilterImageBorderColor);
        mFilterNameTextView.setTextColor(mFilterTextColor);
    }

    public String getFilterName() {
        return mFilterName;
    }

    public void setFilterName(String filterName) {
        mFilterName = filterName;
    }

    public Drawable getFilterImageDrawable() {
        return mFilterImageDrawable;
    }

    public void setFilterImageDrawable(Drawable filterImageDrawable) {
        mFilterImageDrawable = filterImageDrawable;
    }

    /**
     * Checkable implementation
     **/

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            if (!mOnCheckedChangeListners.isEmpty()) {
                for (int i = 0; i < mOnCheckedChangeListners.size(); i++) {
                    mOnCheckedChangeListners.get(i).OnCheckChanged(this, mChecked);
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
    public void addOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListners.add(onCheckedChangeListener);
    }

    @Override
    public void removeOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        mOnCheckedChangeListners.remove(onCheckedChangeListener);
    }

    /**
     * Inner classes
     **/
    private final class TouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onTouchDown(event);
                    break;
                case MotionEvent.ACTION_UP:
                    onTouchUp(event);
                    break;
            }
            if(mOnTouchListener != null){
                mOnTouchListener.onTouch(v,event);
            }
            return true;
        }
    }


}
