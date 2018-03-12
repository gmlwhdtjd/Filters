package com.helloworld.bartender.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.EditView;
import com.helloworld.bartender.FilterableCamera.FCamera;
import com.helloworld.bartender.FilterableCamera.FCameraCapturer;
import com.helloworld.bartender.FilterableCamera.FCameraView;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.PopUpMenu.Popup;
import com.helloworld.bartender.R;

import java.util.List;


/**
 * Created by 김현식 on 2018-01-29.
 * swiping 추가 implement 밑 그밑 주석
 **/
public class horizontal_adapter extends RecyclerView.Adapter<horizontal_adapter.horizontalViewHolder> {

    private List<FCameraFilter> filterList;
    private Context mContext;
    private RecyclerView mRecyclerV;
    private int lastSelectedPosition = -1;
    private Vibrator vibe;
    private EditView editView;

    //뷰타입 확인
    @Override
    public int getItemViewType(int position) {
        if (position == filterList.size()+1) {
            return R.layout.layout_filter_list_end_btt;
        } else if (position == 0) {
            return R.layout.layout_default_filter_icon;
        } else {
            return R.layout.layout_filter_list_icon;
        }
    }


    //여기다가 필터 아이콘 표시
    public class horizontalViewHolder extends RecyclerView.ViewHolder {
        public RadioButton filterIcon;
        public RadioButton defaultFilterIcon;
        public View layout;
        public ImageButton endBtn;

        public horizontalViewHolder(View itemView, int viewType) {
            super(itemView);
            layout = itemView;
            filterIcon = (RadioButton) itemView.findViewById(R.id.filterIcon);
            endBtn = (ImageButton) itemView.findViewById(R.id.endBtt);
            defaultFilterIcon = (RadioButton) itemView.findViewById(R.id.defaultFilterIcon);
        }
    }

    //item 삭제
    public void remove(int position) {
        try {
            filterList.remove(position);
            notifyItemRemoved(position);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }

    //item 추가
    public void add(int position, FCameraFilter filter) {
        filterList.add(position, filter);
        notifyItemInserted(position);
    }


    //dataset의 종류에 따라 다르다.
    public horizontal_adapter(List<FCameraFilter> filterList, Context context, RecyclerView recyclerView) {
        this.filterList = filterList;
        this.mContext = context;
        this.mRecyclerV = recyclerView;
        vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }


    //뷰를 생성한다.(LayoutManger에 의해 실행)
    public horizontal_adapter.horizontalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //뷰 생성
        View view;
        if (viewType == R.layout.layout_filter_list_icon) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_filter_list_icon, parent, false);
        } else if (viewType == R.layout.layout_default_filter_icon) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_default_filter_icon, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_filter_list_end_btt, parent, false);
        }
        return new horizontalViewHolder(view, viewType);

    }

    //뷰안에 content를 바꾼다.(LayoutManger에 의해 실행)
    public void onBindViewHolder(final horizontalViewHolder holder, int position) {
        final DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        //이곳에서 dataset에서 element를 가져온다
        if (position == filterList.size()+1) {
            holder.endBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FCameraFilter newFilter = new OriginalFilter(mContext, null);
                    lastSelectedPosition = -1;
                    ((MainActivity) mContext).setCameraFilter(newFilter);
                    editView = ((MainActivity) mContext).findViewById(R.id.editView);
                    editView.changeState();
                }
            });
        } else if (position == 0) {
            //default Filter
            final FCameraFilter defaultFilter = new OriginalFilter(mContext, null);
            holder.defaultFilterIcon.setText("Default");
            holder.defaultFilterIcon.setChecked(lastSelectedPosition == position);
            holder.defaultFilterIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastSelectedPosition = holder.getAdapterPosition();
                    ((MainActivity) mContext).setCameraFilter(defaultFilter);
                    notifyDataSetChanged();
                }
            });
        } else {
            final FCameraFilter filter = filterList.get(position-1);
            final Popup popup = new Popup(mContext, filter);
            holder.filterIcon.setText(filter.getName());
            holder.filterIcon.setChecked(lastSelectedPosition == position);
            holder.filterIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastSelectedPosition = holder.getAdapterPosition();
                    //외부 클래스에서 뷰 아이템 참조
                    ((MainActivity) mContext).setCameraFilter(filter);
                    notifyDataSetChanged();
//                    filter.setName("Changed");
//                    dbHelper.saveFilter(filter);
//                    Toast.makeText(horizontal_adapter.this.mContext, filter.getName(), Toast.LENGTH_LONG).show();
                }
            });

            holder.filterIcon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    vibe.vibrate(1000);
                    popup.show(v);
                    return true;
                }
            });
        }


    }

    //뷰안에 dataset의 사이즈를 반환한다.(LayoutManger에 의해 실행)
    public int getItemCount() {
        return filterList.size()+2;
    }

}
