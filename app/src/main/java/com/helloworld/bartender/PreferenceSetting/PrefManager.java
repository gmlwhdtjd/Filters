package com.helloworld.bartender.PreferenceSetting;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by samer on 2018-03-01.
 */

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "androidhive-welcome";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    private static final String IS_FIRST_IN_FILTERLIST ="isFirstInFilterList";

    private static final String IS_DEFAULT_FILTER_SET = "IsDefaultFilterSet";


    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }


    public void setDefaultFilterSet(boolean isSet){
        editor.putBoolean(IS_DEFAULT_FILTER_SET,isSet);
        editor.commit();
    }

    public boolean isDefaultFilterSet(){
        return pref.getBoolean(IS_DEFAULT_FILTER_SET,false);
    }

    public void setIsFirstInFilterlist(boolean isFirst){
        editor.putBoolean(IS_FIRST_IN_FILTERLIST,isFirst);
        editor.commit();
    }

    public boolean getIsFirstInFilterlist(){
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH,true);
    }

}
