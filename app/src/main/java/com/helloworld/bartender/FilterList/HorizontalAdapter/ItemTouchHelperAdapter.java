package com.helloworld.bartender.FilterList.HorizontalAdapter;

/**
 * Created by samer on 2018-03-16.
 */

import android.support.v7.widget.RecyclerView;

public interface ItemTouchHelperAdapter{
    /**
     * Called when an item has been dragged far enough to trigger a move. This is called every time
     * an item is shifted, and <strong>not</strong> at the end of a "drop" event.<br/>
     * <br/>
     * Implementations should call {@link RecyclerView.Adapter#notifyItemMoved(int, int)} after
     * adjusting the underlying data to reflect this move.
     *
     * @param fromPosition The start position of the moved item.
     * @param toPosition   Then resolved position of the moved item.
     *
     * @see RecyclerView#getAdapterPositionFor(RecyclerView.ViewHolder)
     * @see RecyclerView.ViewHolder#getAdapterPosition()
     */
    boolean onItemMove(int fromPosition, int toPosition);

    boolean onItemMoveFinished(int fromPosition, int toPosition);

}
