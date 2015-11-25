package com.marcelorocha.MQTTSample.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.marcelorocha.MQTTSample.R;

/**
 * A helper class to draw a line divider on RecyclerView.
 */
public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public SimpleDividerItemDecoration(Context context) {
        mDivider = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
                context.getResources().getDrawable(R.drawable.line_divider, null):
                context.getResources().getDrawable(R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}