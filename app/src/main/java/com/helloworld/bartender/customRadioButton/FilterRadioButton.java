package com.helloworld.bartender.customRadioButton;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by wilybear on 2018-03-21.
 */

public class FilterRadioButton extends RelativeLayout implements RadioCheckable {
    public FilterRadioButton(Context context) {
        super(context);
    }

    public FilterRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FilterRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void addOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener) {

    }

    @Override
    public void removeOnCheckChangeListner(OnCheckedChangeListener onCheckedChangeListener) {

    }

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }
}
