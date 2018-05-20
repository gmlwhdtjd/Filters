package com.teambartender3.filters.FilterList.PopupMenu;

/**
 * Created by samer on 2018-03-08.
 */

public class PopupOption {

    private int optionId;
    private String option;

    public PopupOption(int optionId, String option){
        super();
        this.optionId = optionId;
        this.option = option;
    }

    public int getOptionId() {
        return optionId;
    }

    public String getOptionName() {
        return option;
    }
}
