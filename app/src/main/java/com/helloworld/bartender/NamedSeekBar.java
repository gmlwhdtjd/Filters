package com.helloworld.bartender;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by huijonglee on 2018. 2. 26..
 */

public class NamedSeekBar extends LinearLayout {

    TextView mTextView;
    TextView mValueView;

    SeekBar mSeekBar;

    String mText;
    int mValue;

    OnChangeListener mOnChangeListener = null;

    public NamedSeekBar(Context context) {
        super(context);
        initView();
    }

    public NamedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        getAttrs(attrs, 0);
    }

    public NamedSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initView();
        getAttrs(attrs, defStyle);
    }

    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
    }

    public void setValue(int value) {
        mValue = value;
        mSeekBar.setProgress(mValue);
        mValueView.setText(String.valueOf(mValue));
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_named_seekbar, this);
    }

    @Override // inflate가 완료되는 시점에 호출된다.
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTextView = findViewById(R.id.text);
        mValueView = findViewById(R.id.value);
        mSeekBar = findViewById(R.id.seekBar);

        setText(mText);
        setValue(mValue);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setValue(i);
                if (mOnChangeListener != null) {
                    mOnChangeListener.onValueChanged(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                setValue(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setValue(seekBar.getProgress());
            }
        });
    }

    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.NamedSeekBar, defStyle, 0);

        mText = typedArray.getString(R.styleable.NamedSeekBar_text);
        mValue = typedArray.getInteger(R.styleable.NamedSeekBar_value, 0);

        typedArray.recycle();
    }

    public interface OnChangeListener {
        void onValueChanged(int value);
    }
}
