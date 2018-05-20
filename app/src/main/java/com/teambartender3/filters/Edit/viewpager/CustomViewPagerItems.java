package com.teambartender3.filters.Edit.viewpager;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.View;
import com.ogaclejapan.smarttablayout.utils.PagerItems;

/**
 * Created by wilybear on 2018-04-12.
 */

public class CustomViewPagerItems extends PagerItems<CustomViewPagerItem> {

    public CustomViewPagerItems(Context context) {
        super(context);
    }

    public static Creator with(Context context) {
        return new Creator(context);
    }

    public static class Creator {

        private final CustomViewPagerItems items;

        public Creator(Context context) {
            items = new CustomViewPagerItems(context);
        }

        public CustomViewPagerItems.Creator add(@StringRes int title, View view) {
            return add(CustomViewPagerItem.of(items.getContext().getString(title), view));
        }

        public CustomViewPagerItems.Creator add(@StringRes int title, float width, View view) {
            return add(CustomViewPagerItem.of(items.getContext().getString(title), width, view));
        }

        public CustomViewPagerItems.Creator add(CharSequence title, View view){
            return add(CustomViewPagerItem.of(title,view));
        }

        public CustomViewPagerItems.Creator add(CustomViewPagerItem item) {
            items.add(item);
            return this;
        }

        public CustomViewPagerItems create() {
            return items;
        }

    }

}

