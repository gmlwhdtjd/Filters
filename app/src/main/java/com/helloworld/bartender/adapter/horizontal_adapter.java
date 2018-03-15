package com.helloworld.bartender.adapter;

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
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.PopUpMenu.Popup;
import com.helloworld.bartender.R;

import java.util.Collections;
import java.util.List;


/**
 * Created by 김현식 on 2018-01-29.
 * swiping 추가 implement 밑 그밑 주석
 **/
public class horizontal_adapter extends RecyclerView.Adapter<horizontal_adapter.horizontalViewHolder> implements ItemTouchHelperAdapter {

    private List<FCameraFilter> filterList;
    private Context mContext;
    private RecyclerView mRecyclerV;
    private int lastSelectedPosition = -1;
    private Vibrator vibe;
    private EditView editView;

    //뷰타입 확인
    @Override
    public int getItemViewType(int position) {
        return (position == filterList.size()) ? R.layout.layout_filter_list_end_btt : R.layout.layout_filter_list_icon;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < filterList.size() && toPosition < filterList.size()) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(filterList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(filterList, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }
        return true;
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
        }
    }

    //item 삭제
    public void remove(int position) {

        try {
            filterList.remove(position - 1);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, filterList.size());

        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }
    }

    //item 추가
    public void add(FCameraFilter filter) {
        int position = filterList.size();
        filterList.add(position, filter);
        this.mRecyclerV.scrollToPosition(position);
        notifyItemInserted(position);
    }

    public void update(FCameraFilter filter){
        int position = filterList.indexOf(filter);
        filterList.set(position,filter);
        notifyItemChanged(position);
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
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_filter_list_end_btt, parent, false);
        }
        return new horizontalViewHolder(view, viewType);

    }

    //뷰안에 content를 바꾼다.(LayoutManger에 의해 실행)
    public void onBindViewHolder(final horizontalViewHolder holder, int position) {
        final DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        //이곳에서 dataset에서 element를 가져온다
        if (position == filterList.size()) {
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
        } else {
            final FCameraFilter filter = filterList.get(position);
            final Popup popup = new Popup(mContext, filter, position);
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
        return filterList.size() + 1;
    }

}
