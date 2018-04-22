package com.helloworld.bartender.FilterList.PopupMenu;

/**
 * Created by samer on 2018-03-31.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.helloworld.bartender.Database.DatabaseHelper;
import com.helloworld.bartender.FilterList.FilterListView;
import com.helloworld.bartender.FilterableCamera.Filters.FCameraFilter;
import com.helloworld.bartender.MainActivity;
import com.helloworld.bartender.R;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class CustomPopup {

    private WindowManager mWindowManager;

    private Context mContext;
    private PopupWindow mWindow;

    private OnPopupItemClickListener onPopupItemClickListener;
    private LinearLayout mPopupSlots;
    private ImageView mDownImageView;
    private LayoutInflater inflater;
    private View mView;
    private DatabaseHelper dbHelper;

    private ShowListener showListener;

    private static boolean mIsPopupMenuOpen;

    private FCameraFilter mSelectedFilter;
    private int mSelectedPosition;

    public CustomPopup(final Context context, int viewResource) {
        mContext = context;
        mWindow = new PopupWindow(context);
        mIsPopupMenuOpen = false;
        mWindowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);


        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(layoutInflater.inflate(viewResource, null));

        mDownImageView = (ImageView) mView.findViewById(R.id.arrow_down);
        mPopupSlots = (LinearLayout) mView.findViewById(R.id.popup_slots);
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dbHelper = new DatabaseHelper(mContext);

        PopupOption option1 = new PopupOption(0, mContext.getString(R.string.delete_filter));
        PopupOption option2 = new PopupOption(1, mContext.getString(R.string.duplicate_filter));
 //       PopupOption option3 = new PopupOption(2, mContext.getString(R.string.share_filter));

        this.addItem(option1);
        this.addSeperator();
        this.addItem(option2);
//        this.addSeperator();
//        this.addItem(option3);

        this.setOnItemClickListener(new OnPopupItemClickListener() {
            @Override
            public void onItemClick(int itemId) {
                final FilterListView filterListView = ((MainActivity) mContext).findViewById(R.id.filterListView);
                switch (itemId) {
                    case 0:
                        final SweetAlertDialog deleteDialog = new SweetAlertDialog(context,SweetAlertDialog.WARNING_TYPE);
                                deleteDialog.setTitleText("Are you sure?")
                                .setContentText("Won't be able to recover this filter")
                                .setConfirmText("Yes, delete it!")
                                .setCancelText("No, cancel")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        deleteDialog.setTitleText("Deleted!")
                                                .setContentText("Your filter has been deleted!")
                                                .setConfirmText("OK")
                                                .setConfirmClickListener(null)
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                                        dbHelper.deleteFilter(mSelectedFilter.getId(), mSelectedPosition);
                                        if (filterListView.getHorizontalAdapter().removeItem(mSelectedPosition)) {
                                            filterListView.getHorizontalAdapter().setLastSelectedPosition(0);
                                        }
                                    }
                                });
                                deleteDialog.show();
                        break;
                    case 1:
                        FCameraFilter pastedFilter = dbHelper.pasteFilter(mSelectedFilter, mSelectedPosition);
                        filterListView.getHorizontalAdapter().addItem(pastedFilter, mSelectedPosition + 1);
                        break;
                    case 2:
                        //Share
                        break;
                }
            }
        });

        mPopupSlots.setSelected(true);
    }

    public CustomPopup(Context context) {
        this(context, R.layout.layout_popup);
    }

    public static boolean isPopupMenuOpen() {
        return mIsPopupMenuOpen;
    }

    public void show(View anchor, FCameraFilter selectedFilter, int selectedPosition) {
        preShow();

        mSelectedFilter = selectedFilter;
        mSelectedPosition = selectedPosition;

        int[] location = new int[2];

        anchor.getLocationOnScreen(location);

        //left,top,right,bottom
        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());

        mView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = mView.getMeasuredHeight();
        int rootWidth = mView.getMeasuredWidth();

        Point size = new Point();
        mWindowManager.getDefaultDisplay().getSize(size);
        final int screenWidth = size.x;
        final int screenHeight = size.y;

        int yPos = anchorRect.top - rootHeight;

        int requestedX;
        requestedX = anchorRect.centerX();

        View arrow = mDownImageView;

        final int arrowWidth = arrow.getMeasuredWidth();

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) arrow
                .getLayoutParams();
        int xPos = 0;

        // ETXTREME RIGHT CLIKED`
        if (anchorRect.left + rootWidth > screenWidth) {
            xPos = (screenWidth - rootWidth);
        }
        // ETXTREME LEFT CLIKED
        else if (anchorRect.left - (rootWidth/2) < 0) {
            xPos = 0;
        }
        // INBETWEEN
        else {
            xPos = (anchorRect.centerX() - (rootWidth / 2));
        }

        param.leftMargin = (requestedX - xPos) - (arrowWidth / 2);

        mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);

        mIsPopupMenuOpen = true;
    }

    private void preShow() {
        if (mView == null)
            throw new IllegalStateException("view undefined");

        if (showListener != null) {
            showListener.onPreShow();
            showListener.onShow();
        }

        mWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        mWindow.setAnimationStyle(R.style.animation_popup);

        mWindow.setContentView(mView);
    }


    public void setContentView(View root) {
        mView = root;
        mWindow.setContentView(root);
    }

    public void setContentView(int layoutResID) {
        LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setContentView(inflator.inflate(layoutResID, null));
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mWindow.setOnDismissListener(listener);
    }

    public void dismiss() {
        mWindow.dismiss();
        mIsPopupMenuOpen = false;
        if (showListener != null) {
            showListener.onDismiss();
        }
    }

    public interface ShowListener {
        void onPreShow();

        void onDismiss();

        void onShow();
    }

    public interface OnPopupItemClickListener {
        void onItemClick(int itemId);
    }

    public void setShowListener(ShowListener showListener) {
        this.showListener = showListener;
    }

    private void addItem(final PopupOption item) {
        TextView tv = (TextView) inflater.inflate(R.layout.layout_popup_item, null);
        tv.setText(item.getOptionName());
        mPopupSlots.addView(tv);
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
                dismiss();
            }
        });
    }

    private void addSeperator() {
        ImageView tmp = new ImageView(mContext);
        tmp.setImageResource(R.drawable.popup_seperator);
        tmp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tmp.setScaleType(ImageView.ScaleType.FIT_XY);
        mPopupSlots.addView(tmp);
    }

    private void setOnItemClickListener(OnPopupItemClickListener onPopupItemClickListener) {
        this.onPopupItemClickListener = onPopupItemClickListener;
    }
}
