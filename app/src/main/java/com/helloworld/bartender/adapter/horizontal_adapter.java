package com.helloworld.bartender.adapter;

import android.content.Context;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.helloworld.bartender.Item.Item;
import com.helloworld.bartender.R;

import java.util.List;


/**
 * Created by 김현식 on 2018-01-29.
 * swiping 추가 implement 밑 그밑 주석
 **/
public class horizontal_adapter extends RecyclerView.Adapter<horizontal_adapter.horizontalViewHolder> {

    private List<Item> filters;
    private Context mContext;
    private RecyclerView mRecyclerV;

    private int lastSelectedPosition = -1;

    //필터 attribute

    //뷰타입 확인
    @Override
    public int getItemViewType(int position) {
        return (position == filters.size()) ? R.layout.layout_end_list_btn : R.layout.layout_filter_icon;
    }


    //여기다가 필터 아이콘 표시
    public class horizontalViewHolder extends RecyclerView.ViewHolder {
        public RadioButton filterIcon;
        public TextView filterName;
        public View layout;
        public ImageButton endBtn;

        public horizontalViewHolder(View itemView,int viewType) {
            super(itemView);
            layout = itemView;
            filterIcon = (RadioButton) itemView.findViewById(R.id.filterIcon);
            endBtn = (ImageButton) itemView.findViewById(R.id.endBtn);

            if (viewType == R.layout.layout_filter_icon) {
                filterIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastSelectedPosition = getAdapterPosition();
                        notifyDataSetChanged();
                        Toast.makeText(horizontal_adapter.this.mContext, filterIcon.getText(), Toast.LENGTH_LONG).show();

                    }
                });

                filterIcon.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(horizontal_adapter.this.mContext, "Long Clicjked", Toast.LENGTH_LONG).show();
                        return true;
                    }
                });
            }

        }
    }

    //item 삭제
    public void remove(int position) {
        try {
            filters.remove(position);
            notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }

    //item 추가
    public void add(int position, Item filter) {
        filters.add(position, filter);
        notifyItemInserted(position);
    }


    //dataset의 종류에 따라 다르다.
    public horizontal_adapter(List<Item> filters, Context context, RecyclerView recyclerView) {
        this.filters = filters;
        this.mContext = context;
        this.mRecyclerV = recyclerView;
    }


    //뷰를 생성한다.(LayoutManger에 의해 실행)
    public horizontal_adapter.horizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //뷰 생성
        View view;
        if (viewType == R.layout.layout_filter_icon) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_filter_icon, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_end_list_btn, parent, false);
        }
        return new horizontalViewHolder(view,viewType);
    }

    //뷰안에 content를 바꾼다.(LayoutManger에 의해 실행)
    public void onBindViewHolder(horizontalViewHolder holder, int position) {
        //이곳에서 dataset에서 element를 가져온다
        if (position == filters.size()) {
        } else {
            Item filter = filters.get(position);
            holder.filterIcon.setText(filter.getFilter_name());
            holder.filterIcon.setChecked(lastSelectedPosition == position);
        }


    }

    //뷰안에 dataset의 사이즈를 반환한다.(LayoutManger에 의해 실행)
    public int getItemCount() {
        return filters.size() + 1;
    }

}
