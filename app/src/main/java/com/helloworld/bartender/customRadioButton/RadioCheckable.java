package com.helloworld.bartender.customRadioButton;

import android.view.View;
import android.widget.Checkable;
import android.widget.RadioGroup;

/**
 * Created by wilybear on 2018-03-21.
 */

interface RadioCheckable extends Checkable {
    void addOnCheckChangeListener (OnCheckedChangeListener onCheckedChangeListener);
    void removeOnCheckChangeListner(OnCheckedChangeListener onCheckedChangeListener);

    public static interface OnCheckedChangeListener{
        void OnCheckChanged(View radioGroup, boolean isChecked);
    }

}
