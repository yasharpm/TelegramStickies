package com.yashoid.telegramstickies.app.ui.staticstickereditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.yashoid.telegramstickies.app.R;

public class ToolButton extends LinearLayout {

    private static final int IMAGE_SIZE = 30;
    private static final int MARGIN = 2;
    private static final int TEXT_SIZE = 12;

    private AppCompatImageView mImage;
    private AppCompatTextView mText;

    public ToolButton(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ToolButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ToolButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        mImage = new AppCompatImageView(context);
        mText = new AppCompatTextView(context);

        int imageSize = (int) (getResources().getDisplayMetrics().density * IMAGE_SIZE);

        LayoutParams params = new LayoutParams(imageSize, imageSize);
        params.bottomMargin = (int) (getResources().getDisplayMetrics().density * MARGIN);
        addView(mImage, params);

        mText.setTextSize(TEXT_SIZE);
        addView(mText, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToolButton, defStyleAttr, 0);

        if (a.hasValue(R.styleable.ToolButton_android_src)) {
            setImageResId(a.getResourceId(R.styleable.ToolButton_android_src, 0));
        }

        if (a.hasValue(R.styleable.ToolButton_android_text)) {
            mText.setText(a.getText(R.styleable.ToolButton_android_text));
        }

        a.recycle();
    }

    public void setImageResId(int imageResId) {
        mImage.setImageResource(imageResId);
    }

    public void setText(int textResId) {
        mText.setText(textResId);
    }

}
