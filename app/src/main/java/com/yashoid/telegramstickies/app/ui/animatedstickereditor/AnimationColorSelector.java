package com.yashoid.telegramstickies.app.ui.animatedstickereditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.yashoid.telegramstickies.StickerColor;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.ui.BottomSheetFragment;
import com.yashoid.telegramstickies.app.ui.widget.BottomSheet;

import java.util.Set;

public class AnimationColorSelector extends BottomSheetFragment {

    private static final float HORIZONTAL_MARGIN = 24;
    private static final float COLOR_SIZE = 48;
    private static final float COLOR_MARGIN = 12;
    private static final float BOTTOM_MARGIN = 24;

    public interface OnAnimationColorSelectedListener {

        void onAnimationColorSelected(StickerColor color);

    }

    private final OnAnimationColorSelectedListener mListener;

    private final Set<StickerColor> mColors;

    private StickerColor mColor = null;

    public AnimationColorSelector(Set<StickerColor> colors, OnAnimationColorSelectedListener listener) {
        setMode(BottomSheet.MODE_WRAPPED);

        mListener = listener;
        mColors = colors;
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        final Context context = inflater.getContext();
        final float density = context.getResources().getDisplayMetrics().density;

        final int horizontalMargin = (int) (density * HORIZONTAL_MARGIN);
        final int colorSize = (int) (density * COLOR_SIZE);
        final int colorMargin = (int) (density * COLOR_MARGIN);
        final int bottomMargin = (int) (density * BOTTOM_MARGIN);

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);

        AppCompatTextView textTitle = new AppCompatTextView(context);
        textTitle.setTextSize(14);
        textTitle.setTextColor(ContextCompat.getColor(context, R.color.default_text));
        textTitle.setText(R.string.animationcolorselector_title);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = params.rightMargin = horizontalMargin;
        view.addView(textTitle, params);

        HorizontalScrollView scrollView = new HorizontalScrollView(context);
        LinearLayout colorContainer = new LinearLayout(context);
        colorContainer.setOrientation(LinearLayout.HORIZONTAL);

        colorContainer.addView(new View(context), horizontalMargin, colorSize);

        for (StickerColor color: mColors) {
            View colorView = new View(context);
            colorView.setBackground(Utils.createOvalShape(color.intValue()));
            params = new LinearLayout.LayoutParams(colorSize, colorSize);
            params.rightMargin = colorMargin;
            colorContainer.addView(colorView, params);

            colorView.setOnClickListener(v -> {
                mColor = color;
                dismiss();
            });
        }

        scrollView.addView(colorContainer, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = colorMargin;
        params.bottomMargin = bottomMargin;
        view.addView(scrollView, params);

        return view;
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (mColor != null) {
            mListener.onAnimationColorSelected(mColor);
        }
    }

}
