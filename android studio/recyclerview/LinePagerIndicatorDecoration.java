package com.example.mybooks.recyclerview;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LinePagerIndicatorDecoration extends RecyclerView.ItemDecoration{
    private final String TAG=this.getClass().getSimpleName();
    private int colorActive = 0xFFFFFFFF;
    private int colorInactive = 0x66FFFFFF;

    private static final float DP = Resources.getSystem().getDisplayMetrics().density;

    /**
     * Height of the space the indicator takes up at the bottom of the view.
     * 뷰 맨 아래에서 표시기가 차지하는 공간의 높이입니다
     */
    private final int mIndicatorHeight = (int) (DP * 16);

    /**
     * Indicator stroke width.
     */
    private final float mIndicatorStrokeWidth = DP * 1/2;

    /**
     * Indicator width.
     */
    private final float mIndicatorItemLength = DP * 16;
    /**
     * Padding between indicators.
     */
    private final float mIndicatorItemPadding = DP * 4;

    /**
     * Some more natural animation interpolation
     */
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private final Paint mPaint = new Paint();

    public LinePagerIndicatorDecoration() {
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mIndicatorStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        int itemCount = parent.getAdapter().getItemCount();

        // center horizontally, calculate width and subtract half from center 가로로 중심을 잡고, 너비를 계산하고 중심에서 반을 뺍니다.
        float totalLength = mIndicatorItemLength * itemCount;
        float paddingBetweenItems = Math.max(0, itemCount - 1) * mIndicatorItemPadding;
        float indicatorTotalWidth = totalLength + paddingBetweenItems;
        float indicatorStartX = (parent.getWidth() - indicatorTotalWidth) / 2F;

        // center vertically in the allotted space 할당된 공간에 수직으로 중심을 잡다
        float indicatorPosY = parent.getHeight() - mIndicatorHeight / 2F;

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount);


        // find active page (which should be highlighted) 활성 페이지 찾기(강조되어야 함) 활성 페이지 찾기(강조되어야 함)
        LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
        int activePosition = layoutManager.findFirstVisibleItemPosition();
        if (activePosition == RecyclerView.NO_POSITION) {
            return;
        }

        // find offset of active page (if the user is scrolling) 활성 페이지의 오프셋 찾기(사용자가 스크롤 중인 경우)
        final View activeChild = layoutManager.findViewByPosition(activePosition);
        int left = activeChild.getLeft();
        int width = activeChild.getWidth();

        // on swipe the active item will be positioned from [-width, 0] 스와이프 시 활성 항목은 [-width, 0]에서 위치하게 됩니다.
        // interpolate offset for smooth animation 부드러운 애니메이션을 위해 간격띄우기 보간
        float progress = mInterpolator.getInterpolation(left * -1 / (float) width);

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress, itemCount);
    }

    private void drawInactiveIndicators(Canvas c, float indicatorStartX, float indicatorPosY, int itemCount) { Log.e(TAG, "itemCount:"+itemCount);
        mPaint.setColor(colorInactive);

        // width of item indicator including padding 패딩을 포함한 indicator 폭
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;

        float start = indicatorStartX;
        for (int i = 0; i < itemCount; i++) {
            // draw the line for every item
            c.drawLine(start, indicatorPosY, start + mIndicatorItemLength, indicatorPosY, mPaint);
            start += itemWidth;
        }

    }

    private void drawHighlights(Canvas c, float indicatorStartX, float indicatorPosY,
                                int highlightPosition, float progress, int itemCount) {
        mPaint.setColor(colorActive);

        // width of item indicator including padding
        final float itemWidth = mIndicatorItemLength + mIndicatorItemPadding;

        if (progress == 0F) {
            // no swipe, draw a normal indicator
            float highlightStart = indicatorStartX + itemWidth * highlightPosition;
            c.drawLine(highlightStart, indicatorPosY,
                    highlightStart + mIndicatorItemLength, indicatorPosY, mPaint);
        } else {
            float highlightStart = indicatorStartX + itemWidth * highlightPosition;
            // calculate partial highlight
            float partialLength = mIndicatorItemLength * progress;

            // draw the cut off highlight
            c.drawLine(highlightStart + partialLength, indicatorPosY,
                    highlightStart + mIndicatorItemLength, indicatorPosY, mPaint);

            // draw the highlight overlapping to the next item as well
            if (highlightPosition < itemCount - 1) {
                highlightStart += itemWidth;
                c.drawLine(highlightStart, indicatorPosY,
                        highlightStart + partialLength, indicatorPosY, mPaint);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = mIndicatorHeight;
    }
}