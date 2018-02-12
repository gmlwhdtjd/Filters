package com.helloworld.bartender.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.helloworld.bartender.Item.Item;
import com.helloworld.bartender.Item.SetViewHolder;
import com.helloworld.bartender.OnTapListener;
import com.helloworld.bartender.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by 김현식 on 2018-02-09.
 */

public class FilterListAdapter_making extends RecyclerView.Adapter<SetViewHolder> {

    private Activity activity;
    List<Item> items= Collections.emptyList();
    private OnTapListener OnTapListener;

    public FilterListAdapter_making(Activity activity, List<Item> items){
        this.activity =activity;
        this.items =items;
    }


    @Override
    public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.making_filter_icon_layout,parent,false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SetViewHolder holder, final int position) {
        holder.txt_name.setText(items.get(position).getFilter_name());
        holder.txt_attribute.setText(items.get(position).getAttributes());
        holder.itemView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(OnTapListener !=null){
                    OnTapListener.OnTapView(position);
                }
            }

        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnTapListener(OnTapListener onTapListener){
        this.OnTapListener=onTapListener;
    }
}
