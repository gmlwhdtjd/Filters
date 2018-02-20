package com.helloworld.bartender.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.helloworld.bartender.Item.Item;
import com.helloworld.bartender.R;

import java.util.List;


/**
 * Created by 김현식 on 2018-01-29.
 * swiping 추가 implement 밑 그밑 주석
 **/
public class horizontal_adapter extends RecyclerView.Adapter<horizontal_adapter.horizontalViewHolder> {

    //아이템 리스트
    private List<String> items;

    private List<Item> filters;
    private Context mContext;
    private RecyclerView mRecyclerV;

    //필터 attribute

    //아이템 클릭 시 실행될 함수
    private ItemClick itemClick;

    public interface ItemClick {
        public void onClick(String str, int position, int Lastposition);
        // public void onLongClick(String str, int position, int Lastpostion);
    }

    //아이템 클릭 시 실행될 함수
    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }


    //여기다가 필터 아이콘 표시
    public class horizontalViewHolder extends RecyclerView.ViewHolder {
        public ImageButton filterIcon;
        public TextView filterName;
        public View layout;

        public horizontalViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            filterIcon = (ImageButton) itemView.findViewById(R.id.filterIcon);
            filterName = (TextView) itemView.findViewById(R.id.filterName);
        }
//
//        @Override
//
//        public void onClick(View v) {
//            Log.d("te","Clicked");
//            delete(getAdapterPosition());
//            try {
//                if (getAdapterPosition() == items.size()) {
//                    Intent intent = new Intent(MainActivity.class, Filter_making_page.class);
//                    startActivity(intent);
//                }
//            }
//            catch (IndexOutOfBoundsException e){
//                e.printStackTrace();
//            }
//        }
//
//
//        @Override
//
//        public boolean onLongClick(View v) {
//
//            add(getAdapterPosition(), items.get(getAdapterPosition()));
//
//            return true;
//
//        }
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
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.filter_icon_layout, parent, false);
        return new horizontalViewHolder(view);
    }

    //뷰안에 content를 바꾼다.(LayoutManger에 의해 실행)
    public void onBindViewHolder(horizontalViewHolder holder, int position) {
        //이곳에서 dataset에서 element를 가져온다

        if (position == filters.size()) {
            holder.filterIcon.setImageResource(R.mipmap.ic_edit);
        }
        final Item filter = filters.get(position);
        holder.filterName.setText(filter.getFilter_name());

        position = position % items.size();
        final String str = items.get(position);
        final int finalPosition1 = position;

        holder.filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClick != null) {
                    itemClick.onClick(str, finalPosition1, items.size());
                    String s = String.valueOf(items.size());
                    Log.v("x", s);
                }
            }
        });


    }

    //뷰안에 dataset의 사이즈를 반환한다.(LayoutManger에 의해 실행)
    public int getItemCount() {
        return filters.size();
    }

}
