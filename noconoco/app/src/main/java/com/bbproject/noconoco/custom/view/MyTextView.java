package com.bbproject.noconoco.custom.view;

import android.content.Context;
import android.util.AttributeSet;

public class MyTextView extends android.support.v7.widget.AppCompatTextView {

    public MyTextView(Context pContext, AttributeSet pAttrs, int pDefStyle) {
        super(pContext, pAttrs, pDefStyle);
        rotate();
    }

    public MyTextView(Context pContext, AttributeSet pAttrs) {
        super(pContext, pAttrs);
        rotate();
    }

    public MyTextView(Context pContext) {
        super(pContext);
        rotate();
    }

    private void rotate() {
        setSelected(true);
    }

}