package com.helloworld.bartender;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

/**
 * Created by 김현식 on 2018-01-29.
 * swiping 추가 implement 밑 그밑 주석
 **/
public class horizontal_adapter extends RecyclerView.Adapter<horizontal_adapter.horizontalViewHolder> {

    //아이템 리스트
    private List<String> items;

    //필터 attribute

    //아이템 클릭 시 실행될 함수
    private ItemClick itemClick;
    public interface ItemClick {
        public void onClick(String str, int position, int Lastposition);
    }

    //아이템 클릭 시 실행될 함수
    public void setItemClick(ItemClick itemClick) {
        this.itemClick = itemClick;
    }



    public class horizontalViewHolder extends RecyclerView.ViewHolder {
        Button btt;
        Button backBtt;

        public horizontalViewHolder(View itemView) {
            super(itemView);
            btt = itemView.findViewById(R.id.filterIcon);
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

        //item 삭제
        public void delete(int position) {

            try {

                items.remove(position);

                notifyItemRemoved(position);

            } catch (IndexOutOfBoundsException ex) {

                ex.printStackTrace();

            }

        }

        //item 추가
        public void add(int position, String infoData) {

            items.add(position, infoData);

            notifyItemInserted(position);

        }

    }



    //dataset의 종류에 따라 다르다.
    public horizontal_adapter(List<String> items) {
        this.items = items;
    }


    //뷰를 생성한다.(LayoutManger에 의해 실행)
    public horizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //뷰 생성
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.filter_icon_layout, parent, false);
        return new horizontalViewHolder(view);
    }

    //뷰안에 content를 바꾼다.(LayoutManger에 의해 실행)
    public void onBindViewHolder(horizontalViewHolder holder, int position) {
        //이곳에서 dataset에서 element를 가져온다

        //element를 뷰의 content안에 넣는다.
        holder.btt.setText(items.get(position).toString());

        position = position%items.size();
        final String str = items.get(position);
        final int finalPosition1 = position;

        holder.btt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClick != null){
                    itemClick.onClick(str, finalPosition1,items.size());
                    String s= String.valueOf(items.size());
                    Log.v("x",s);
                }
            }
        });

    }

    //뷰안에 dataset의 사이즈를 반환한다.(LayoutManger에 의해 실행)
    public int getItemCount() {
        return items.size();
    }

}
