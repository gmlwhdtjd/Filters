package com.helloworld.bartender.FilterList.FilterRadioButton;

import android.view.View;
import android.widget.Checkable;

/**
 * Created by wilybear on 2018-03-21.
 */

interface RadioCheckable extends Checkable {
    void addOnCheckChangeListener (OnCheckedChangeListener onCheckedChangeListener);
    void removeOnCheckChangeListener(OnCheckedChangeListener onCheckedChangeListener);

    public static interface OnCheckedChangeListener{
        void OnCheckChanged(View radioGroup, boolean isChecked);
    }

}
