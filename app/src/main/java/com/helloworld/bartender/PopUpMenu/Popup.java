package com.helloworld.bartender.PopUpMenu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.FilterListView;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;

/**
 * Created by samer on 2018-03-08.
 */

public class Popup {
    private PopupWindow popupWindow;
    private OnPopupItemClickListener onPopupItemClickListener;
    private LinearLayout rootView;
    private LayoutInflater inflater;
    private FCameraFilter selectedFilter;
    private DatabaseHelper dbHelper;

    public interface OnPopupItemClickListener {
        public abstract void onItemClick(int itemId);
    }


    public Popup(final Context context, final FCameraFilter selectedFilter) {
        super();
        this.selectedFilter = selectedFilter;
        this.popupWindow = new PopupWindow(context);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (LinearLayout) inflater.inflate(R.layout.layout_popup_slot, null);
        dbHelper = new DatabaseHelper(context);

        PopupOption option1 = new PopupOption(0, "삭제");
        PopupOption option2 = new PopupOption(1, "복제");
        PopupOption option3 = new PopupOption(2, "공유");

        this.addItem(option1);
        this.addSeperator();
        this.addItem(option2);
        this.addSeperator();
        this.addItem(option3);

        this.setOnItemClickListener(new OnPopupItemClickListener() {
            @Override
            public void onItemClick(int itemId) {
                FilterListView filterListView=((MainActivity) context).findViewById(R.id.FilterListView);
                switch (itemId) {
                    case 0:
                        //DELETE
                        // TODO: delete시 현재 사용중인 필터를 default필터로 변경
                        dbHelper.deleteFilterRecord(selectedFilter.getId());
                        filterListView.populateRecyclerView(""); //TODO: 정렬이 아닌 리스트에서 삭제로 변경
                        break;
                    case 1:
                        //PASTE
                        //TODO: 복제시 이름 카운트??
                        dbHelper.pasteFilter(selectedFilter);
                        filterListView.populateRecyclerView("");
                        break;
                    case 2:
                        //Share
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void addItem(final PopupOption item) {
        TextView tv = (TextView) inflater.inflate(R.layout.layout_popup_item, null);
        tv.setText(item.getOptionName());
        rootView.addView(tv);
        tv.setOnClickListener(new View.OnClickListener() {
            /*
             * We basically intercept the click on this textview
             * and pass it to the callback interface. So the end
             * user will get this event as a click on an action item
             * And finally close this popup
             */
            @Override
            public void onClick(View v) {
                onPopupItemClickListener.onItemClick(item.getOptionId());
                popupWindow.dismiss();
            }
        });
    }

    public void addSeperator() {
        View seperator = (View) inflater.inflate(R.layout.layout_popup_seperator, null);
        rootView.addView(seperator);
    }


    /*
     * This method calculates the centre of the view and displays the
	 * popup there. By default a popup is shown at (0, 0) with referring
	 * the bottom left corner of the view as origin.
	 */
    public void show(View v) {
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(rootView);
        popupWindow.setFocusable(true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.animation_popup);

        int[] location = new int[2];
        v.getLocationOnScreen(location);
        rootView.measure(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

		/*
		 * Since the anchor position for a popup is the left top of the anchor view,
		 * calculate the x position and y position and override the location manually
		 */
        int xPos = location[0] + v.getWidth() / 2 - rootView.getMeasuredWidth() / 2;
        int yPos = location[1] - rootView.getMeasuredHeight();

        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, xPos, yPos);
    }

    public void setOnItemClickListener(OnPopupItemClickListener onPopupItemClickListener) {
        this.onPopupItemClickListener = onPopupItemClickListener;
    }
}

