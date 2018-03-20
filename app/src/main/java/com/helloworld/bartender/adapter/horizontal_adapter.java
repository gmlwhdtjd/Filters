package com.helloworld.bartender.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Vibrator;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.EditView;
import com.helloworld.bartender.FilterListView;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.OriginalFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.PopUpMenu.Popup;
import com.helloworld.bartender.R;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Created by 김현식 on 2018-01-29.
 * swiping 추가 implement 밑 그밑 주석
 **/
public class horizontal_adapter extends RecyclerView.Adapter<horizontal_adapter.horizontalViewHolder> implements ItemTouchHelperAdapter {

    private List<FCameraFilter> filterList;
    private Context mContext;
    private RecyclerView mRecyclerV;
    private int lastSelectedPosition = 0;
    private Vibrator vibe;
    private EditView editView;
    private Popup popup;
    private Animation anim;
    private Set<horizontalViewHolder> mBoundViewHolders = new HashSet<>();

    //뷰타입 확인
    @Override
    public int getItemViewType(int position) {
        return (position == filterList.size()) ? R.layout.layout_filter_list_end_btt : R.layout.layout_filter_list_icon;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (popup.isPopupMenuOpen()) {
            popup.dismiss();
        }

        if (fromPosition < filterList.size() && toPosition < filterList.size() && toPosition != 0 && fromPosition != 0) {
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

    @Override
    public boolean onItemMoveFinished(int fromPosition, int toPosition) {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        if (fromPosition < filterList.size() && toPosition < filterList.size() && toPosition != 0 && fromPosition != 0) {
            dbHelper.chagePositionByDrag(fromPosition, toPosition);
        } else {
            if (fromPosition >= filterList.size()) {
                dbHelper.chagePositionByDrag(fromPosition - 1, toPosition);
            } else if (toPosition >= filterList.size()) {
                dbHelper.chagePositionByDrag(fromPosition, toPosition - 1);
            } else if (toPosition == 0) {
                dbHelper.chagePositionByDrag(fromPosition, toPosition + 1);
            }
        }
        return true;
    }


    //여기다가 필터 아이콘 표시
    public class horizontalViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public RadioButton filterIcon;
        public View layout;
        public ImageButton endBtn;
        public TextView filterName;

        public horizontalViewHolder(final View itemView, int viewType) {
            super(itemView);
            layout = itemView;
            filterIcon = (RadioButton) itemView.findViewById(R.id.filterIcon);
            endBtn = (ImageButton) itemView.findViewById(R.id.endBtt);
            popup = new Popup(mContext);
            filterName = (TextView) itemView.findViewById(R.id.filterName);

//            //get bitmap of the image
//            Bitmap imageBitmap= BitmapFactory.decodeResource(mContext.getResources(),  R.drawable.sample_image);
//            imageBitmap = Bitmap.createScaledBitmap(imageBitmap,100,100,true);
//            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(mContext.getResources(), imageBitmap);
//
//            //setting radius
//            roundedBitmapDrawable.setCornerRadius(50.0f);
//            roundedBitmapDrawable.setAntiAlias(true);
        }

        //item 이 move 했을때
        @Override
        public void onItemSelected(int position) {
            FilterListView filterListView = ((MainActivity) mContext).findViewById(R.id.FilterListView);
            startAnimationsOnItems(position);
        }

        //item 의 이동이 끝났을 떄
        @Override
        public void onItemClear(int position) {
            FilterListView filterListView = ((MainActivity) mContext).findViewById(R.id.FilterListView);
            stopAnimationsOnItems(position);
        }
    }

    //item 삭제
    public boolean remove(int position) {
        try {
            filterList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, filterList.size());

        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }

        return lastSelectedPosition == position;
    }

    //item 추가
    public void add(FCameraFilter filter, int position) {
        filterList.add(position, filter);
        this.mRecyclerV.scrollToPosition(position);
        notifyItemInserted(position);
    }

    public void update(FCameraFilter filter) {
        int position = filterList.indexOf(filter);
        filterList.set(position, filter);
        notifyItemChanged(position);
    }


    //dataset의 종류에 따라 다르다.
    public horizontal_adapter(List<FCameraFilter> filterList, Context context, RecyclerView recyclerView) {
        this.filterList = filterList;
        this.mContext = context;
        this.mRecyclerV = recyclerView;
        vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        anim = AnimationUtils.loadAnimation(mContext, R.anim.shake);
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
        if (holder.getAdapterPosition() == filterList.size()) {
            holder.endBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FCameraFilter newFilter = new OriginalFilter(mContext, null);
                    lastSelectedPosition = -1;
                    ((MainActivity) mContext).setCameraFilter(newFilter);
                    editView = ((MainActivity) mContext).findViewById(R.id.editView);
                    editView.changeState();
                    //TODO:새로 필터 생성 후 그 필터를 select되게 한다.
                }
            });
        } else {
            final FCameraFilter filter = filterList.get(holder.getAdapterPosition());
            holder.filterName.setText(filter.getName());
            holder.filterIcon.setChecked(lastSelectedPosition == holder.getAdapterPosition());
            holder.filterIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lastSelectedPosition = holder.getAdapterPosition();
                    //외부 클래스에서 뷰 아이템 참조
                    ((MainActivity) mContext).setCameraFilter(filter);
                    notifyDataSetChanged();
                }
            });

            if (holder.getAdapterPosition() != 0) {
                mBoundViewHolders.add((horizontalViewHolder) holder);
                holder.filterIcon.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        vibe.vibrate(50);
                        popup.show(v, filter, holder.getAdapterPosition());
                        return true;
                    }
                });
            }
        }
    }

    public void startAnimationsOnItems(int position) {
        for (horizontalViewHolder holder : mBoundViewHolders) {
            if (holder.getAdapterPosition() != position && holder.getAdapterPosition() != 0) {
                holder.filterIcon.startAnimation(anim);
            }
        }
    }

    public void stopAnimationsOnItems(int position) {
        for (horizontalViewHolder holder : mBoundViewHolders) {
            if (holder.getAdapterPosition() != position && holder.getAdapterPosition() != 0) {
                holder.filterIcon.clearAnimation();
            }
        }
    }

    public void setLastSelectedPosition(int position) {
        final FCameraFilter filter = filterList.get(position);
        ((MainActivity) mContext).setCameraFilter(filter);
        lastSelectedPosition = position;
        notifyDataSetChanged();
    }


    //뷰안에 dataset의 사이즈를 반환한다.(LayoutManger에 의해 실행)
    public int getItemCount() {
        return filterList.size() + 1;
    }

}
