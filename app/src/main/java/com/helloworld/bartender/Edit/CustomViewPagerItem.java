package com.helloworld.bartender.Edit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.utils.PagerItem;

/**
 * Created by wilybear on 2018-04-12.
 */

public class CustomViewPagerItem extends PagerItem {

    private final View view;

    protected CustomViewPagerItem(CharSequence title, float width, View view) {
        super(title, width);
        this.view = view;
    }

    public static CustomViewPagerItem of(CharSequence title, View view) {
        return of(title, DEFAULT_WIDTH, view);
    }

    public static CustomViewPagerItem of(CharSequence title, float width, View view) {
        return new CustomViewPagerItem(title, width, view);
    }

    public View initiate(LayoutInflater inflater, ViewGroup container) {
        container.addView(view);
        return view;
    }

    public View getView(){
        return view;
    }
}
