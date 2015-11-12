package com.tencent.research.test.swipedeletelisttest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * 侧滑删除的listview
 * Created by niuniuzhang on 15/11/11.
 */
public class SwipeDeleteListView extends ListView implements View.OnTouchListener, GestureDetector.OnGestureListener {

    private GestureDetector gestureDetector;
    private View deleteButton;
    private ViewGroup itemLayout;
    private boolean isDeleteShown;
    private int selectedItem;

    public SwipeDeleteListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        doInit();
    }

    public SwipeDeleteListView(Context context) {
        super(context);
        doInit();
    }

    private void doInit() {
        gestureDetector = new GestureDetector(getContext(), this);
        isDeleteShown = false;
        setOnTouchListener(this);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (!isDeleteShown) {
            selectedItem = pointToPosition((int)e.getX(), (int)e.getY());
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!isDeleteShown && Math.abs(velocityX) > Math.abs(velocityY)) {
            deleteButton = LayoutInflater.from(getContext()).inflate(R.layout.delete_button_layout, null);
            deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemLayout.removeView(deleteButton);
                    deleteButton = null;
                    isDeleteShown = false;
                    Toast.makeText(getContext(), "Delete Clicked", Toast.LENGTH_SHORT).show();
                }
            });
            itemLayout = (ViewGroup) getChildAt(selectedItem - getFirstVisiblePosition());
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            itemLayout.addView(deleteButton, params);
            isDeleteShown = true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isDeleteShown) {
            itemLayout.removeView(deleteButton);
            deleteButton = null;
            isDeleteShown = false;
            return false;
        } else {
            return gestureDetector.onTouchEvent(event);
        }
    }
}
