package ninja.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import ninja.lbs.filechooser.R;

/**
 * SimpleDividerDecoration for recycle view adapter
 */
public class SimpleDividerDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int extendLeft;
    private int extendRight;

    public SimpleDividerDecoration(Context context) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider);
        extendLeft = context.getResources().getDimensionPixelOffset(R.dimen.avatar_frame_size);
        extendRight = context.getResources().getDimensionPixelOffset(R.dimen.space8);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft() * 2 + extendLeft;
        int right = parent.getWidth() - parent.getPaddingRight() - extendRight;

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