package com.teambartender3.filters.Edit;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.teambartender3.filters.R;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by huijonglee on 2018. 2. 26..
 */

public class NamedSeekBar extends LinearLayout {

    TextView mTextView;
    TextView mValueView;

    DiscreteSeekBar mSeekBar;

    String mText;
    int mValue;

    OnChangeListener mOnChangeListener = null;

    public NamedSeekBar(Context context) {
        super(context);
        init(null, 0);
    }

    public NamedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public NamedSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init(attrs, defStyle);
    }

    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
    }

    public void setMax(int value) {
        mSeekBar.setMax(value);
    }

    public void setMin(int value) {
        mSeekBar.setMin(value);
    }

    public void setValue(int value) {
        mValue = value;
        mSeekBar.setProgress(mValue);
        mValueView.setText(String.valueOf(mValue));
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.setOrientation(LinearLayout.VERTICAL);
        this.setGravity(Gravity.CENTER);
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1));

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        this.addView(relativeLayout);

        mTextView = new TextView(getContext());
        relativeLayout.addView(mTextView);

        mValueView = new TextView(getContext());
        mValueView.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        mValueView.setLayoutParams(params);
        relativeLayout.addView(mValueView);

        mSeekBar = new DiscreteSeekBar(getContext());
        mSeekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setColor(getResources().getColor(R.color.main_back));
        this.addView(mSeekBar);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NamedSeekBar, defStyle, 0);

        setText(typedArray.getString(R.styleable.NamedSeekBar_text));
        setValue(typedArray.getInt(R.styleable.NamedSeekBar_value, 0));

        typedArray.recycle();

        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                setValue(value);
                if (mOnChangeListener != null) {
                    mOnChangeListener.onValueChanged(value);
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                setValue(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                setValue(seekBar.getProgress());
            }
        });
    }

    public void setColor(int color) {
        mSeekBar.setScrubberColor(color);
        mSeekBar.setThumbColor(color, color);
    }

    public interface OnChangeListener {
        void onValueChanged(int value);
    }
}
