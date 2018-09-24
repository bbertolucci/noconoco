package com.bbproject.noconoco.custom.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.RelativeLayout;

public class SquareLayout extends RelativeLayout {

    public SquareLayout(Context pContext) {
        super(pContext);
    }

    public SquareLayout(Context pContext, AttributeSet pAttrs) {
        super(pContext, pAttrs);
    }

    public SquareLayout(Context pContext, AttributeSet pAttrs, int pDefStyle) {
        super(pContext, pAttrs, pDefStyle);
    }

    @Override
    protected void onMeasure(int pWidthMeasureSpec, int pHeightMeasureSpec) {

        int width = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, getResources().getDisplayMetrics())); //100; //MeasureSpec.getSize(widthMeasureSpec);
        int height = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, getResources().getDisplayMetrics())); //100; //MeasureSpec.getSize(heightMeasureSpec);

        double scale = 1.0;
        if (width > (int) ((scale * height) + 0.5)) {
            width = (int) ((scale * height) + 0.5);
        } else {
            height = (int) ((width / scale) + 0.5);
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
    }
}
