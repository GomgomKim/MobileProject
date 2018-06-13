package org.androidtown.sharepic;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;


public class Square extends android.support.v7.widget.AppCompatImageView {

    private String mFit = "normal";

    public Square(Context context) {
        super(context);
    }

    public Square(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Square(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        mFit = context.obtainStyledAttributes(attrs, R.styleable.Square).getString(R.styleable.Square_fit);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(TextUtils.equals(mFit, "width")) { // fit width
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else if(TextUtils.equals(mFit, "height")) { // fit height
            super.onMeasure(heightMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}