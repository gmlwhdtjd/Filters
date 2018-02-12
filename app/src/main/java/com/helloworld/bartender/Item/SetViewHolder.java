package com.helloworld.bartender.Item;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.helloworld.bartender.R;

/**
 * Created by 김현식 on 2018-02-09.
 */

public class SetViewHolder extends RecyclerView.ViewHolder {

    public Button txt_name;
    public TextView txt_attribute;

    public SetViewHolder(View itemView) {
        super(itemView);
        txt_name=(Button)itemView.findViewById(R.id.making_filterIcon);
        txt_attribute =(TextView)itemView.findViewById(R.id.making_attribute);
    }
}
