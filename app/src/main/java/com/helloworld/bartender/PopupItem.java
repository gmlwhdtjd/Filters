package com.helloworld.bartender;

/**
 * Created by samer on 2018-03-08.
 */

public class PopupItem {


    private int optionId;
    private String optionName;

    public PopupItem(int itemId, String itemName){
        super();
        this.optionId = itemId;
        this.optionName = itemName;
    }

    public int getOptionId() {
        return optionId;
    }

    public String getOptionName() {
        return optionName;
    }
}
