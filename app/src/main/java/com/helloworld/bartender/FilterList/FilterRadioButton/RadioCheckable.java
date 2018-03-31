package com.helloworld.bartender.FilterList.FilterRadioButton;

import android.view.View;
import android.widget.Checkable;

/**
 * Created by wilybear on 2018-03-21.
 */

interface RadioCheckable extends Checkable {
    interface OnCheckedChangeListener{
        void OnCheckChanged(View radioGroup, boolean isChecked);
    }

}
