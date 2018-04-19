package com.helloworld.bartender.FilterList.HorizontalAdapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageButton;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.Edit.EditView;
import com.helloworld.bartender.FilterList.PopupMenu.CustomPopup;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.FilterableCamera.Filters.RetroFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;
import com.helloworld.bartender.FilterList.FilterRadioButton.FilterRadioButton;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by 김현식 on 2018-01-29.
 * swiping 추가 implement 밑 그밑 주석
 **/
public class horizontal_adapter extends RecyclerView.Adapter<horizontal_adapter.horizontalViewHolder> implements ItemTouchHelperAdapter {

    private List<FCameraFilter> mFilterList;
    private Context mContext;
    private RecyclerView mRecyclerV;
    private int lastSelectedPosition = 0;
    private Vibrator vibe;
    private EditView mEditView;
    private CustomPopup mCustomPopup;
    private Animation anim;
    private Set<horizontalViewHolder> mBoundViewHolders = new HashSet<>();

    public int getItemViewType(int position) {
        return (position == mFilterList.size()) ? R.layout.layout_filter_list_end_btt : R.layout.layout_filter_list_icon;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        if (CustomPopup.isPopupMenuOpen()) {
            mCustomPopup.dismiss();
        }

        if (fromPosition < mFilterList.size() && toPosition < mFilterList.size() && toPosition != 0 && fromPosition != 0) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mFilterList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mFilterList, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }

        return true;
    }

    @Override
    public boolean onItemMoveFinished(int fromPosition, int toPosition) {
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        if (fromPosition < mFilterList.size() && toPosition < mFilterList.size() && toPosition != 0 && fromPosition != 0) {
            dbHelper.changePositionByDrag(fromPosition, toPosition);
        } else {
            if (fromPosition >= mFilterList.size()) {
                dbHelper.changePositionByDrag(fromPosition - 1, toPosition);
            } else if (toPosition >= mFilterList.size()) {
                dbHelper.changePositionByDrag(fromPosition, toPosition - 1);
            } else if (toPosition == 0) {
                dbHelper.changePositionByDrag(fromPosition, toPosition + 1);
            }
        }
        return true;
    }

    public class horizontalViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private FilterRadioButton mFilterRadioBtn;
        public View layout;
        private ImageButton mEndBtn;

        private horizontalViewHolder(final View itemView, int viewType) {
            super(itemView);
            layout = itemView;
            mFilterRadioBtn = (FilterRadioButton) itemView.findViewById(R.id.filterIcon);
            mEndBtn = (ImageButton) itemView.findViewById(R.id.endBtt);
            mCustomPopup = new CustomPopup(mContext);
        }

        @Override
        public void onItemSelected(int position) {
            startAnimationsOnItems(position);
        }

        @Override
        public void onItemClear(int position) {
            stopAnimationsOnItems(position);
        }
    }

    public boolean removeItem(int position) {
        try {
            mFilterList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFilterList.size());
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        }

        return lastSelectedPosition == position;
    }

    //item 추가
    public void addItem(FCameraFilter filter, int position) {
        mFilterList.add(position, filter);
        this.mRecyclerV.scrollToPosition(position);
        notifyItemInserted(position);
        setLastSelectedPosition(position);
    }

    public void updateItem(FCameraFilter filter) {
        int position = mFilterList.indexOf(filter);
        mFilterList.set(position, filter);
        notifyItemChanged(position);
    }

    public horizontal_adapter(List<FCameraFilter> filterList, Context context, RecyclerView recyclerView) {
        mFilterList = filterList;
        mContext = context;
        mRecyclerV = recyclerView;
        vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        anim = AnimationUtils.loadAnimation(mContext, R.anim.shake);
    }

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


    public void onBindViewHolder(final horizontalViewHolder holder, int position) {
        final DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        if (holder.getAdapterPosition() == mFilterList.size()) {
            holder.mEndBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    * 만약 필터 종류를 업데이트 한다면 이곳에서 필터 종류를 분류 시켜줘야 합니다.
                    * */
                    FCameraFilter newFilter = new RetroFilter(mContext, null);
                    lastSelectedPosition = -1;
                    ((MainActivity) mContext).setCameraFilter(newFilter);
                    mEditView = ((MainActivity) mContext).findViewById(R.id.editView);
                    mEditView.changeState();
                }
            });
            holder.mEndBtn.setOnTouchListener(((MainActivity) mContext).OnTouchEffectListener);
        } else {
            final FCameraFilter filter = mFilterList.get(holder.getAdapterPosition());
            holder.mFilterRadioBtn.setFilterImageDrawable(dbHelper.getFilterIconImage(filter.getId()));
            holder.mFilterRadioBtn.setFilterName(filter.getName());
            holder.mFilterRadioBtn.setChecked(lastSelectedPosition == holder.getAdapterPosition());
            holder.mFilterRadioBtn.setOnClickListener(new View.OnClickListener() {
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
                holder.mFilterRadioBtn.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        vibe.vibrate(50);
                        mCustomPopup.show(v, filter, holder.getAdapterPosition());
                        return true;
                    }
                });
            }
        }
    }

    private void startAnimationsOnItems(int position) {
        for (horizontalViewHolder holder : mBoundViewHolders) {
            if (holder.getAdapterPosition() != position && holder.getAdapterPosition() != 0) {
                CircleImageView v = holder.mFilterRadioBtn.getCircleImageView();
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v,
                        "scaleX", 0.85f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v,
                        "scaleY", 0.85f);
                scaleUpX.setDuration(150);
                scaleUpY.setDuration(150);

                AnimatorSet scaleDown = new AnimatorSet();
                scaleDown.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleDown.play(scaleUpX).with(scaleUpY);
                scaleDown.start();
            }
        }
    }

    private void stopAnimationsOnItems(int position) {
        for (horizontalViewHolder holder : mBoundViewHolders) {
            if (holder.getAdapterPosition() != position && holder.getAdapterPosition() != 0) {
                CircleImageView v = holder.mFilterRadioBtn.getCircleImageView();
                ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(v,
                        "scaleX", 1.0f);
                ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(v,
                        "scaleY", 1.0f);
                scaleUpX.setDuration(150);
                scaleUpY.setDuration(150);

                AnimatorSet scaleUp = new AnimatorSet();
                scaleUp.play(scaleUpX).with(scaleUpY);
                scaleUp.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleUp.start();
            }
        }
    }

    public void setLastSelectedPosition(int position) {
        final FCameraFilter filter = mFilterList.get(position);
        ((MainActivity) mContext).setCameraFilter(filter);
        lastSelectedPosition = position;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return mFilterList.size() + 1;
    }

    public FCameraFilter getDefaultFilter(){
        return mFilterList.get(0);
    }

    public boolean isPopupMenuOpen(){
        return mCustomPopup.isPopupMenuOpen();
    }

    public void dismissPopup(){
        mCustomPopup.dismiss();
    }
}
