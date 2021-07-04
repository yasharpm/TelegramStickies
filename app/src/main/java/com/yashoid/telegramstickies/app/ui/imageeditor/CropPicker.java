package com.yashoid.telegramstickies.app.ui.imageeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.yashoid.telegramstickies.app.Crop;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Unavailable;
import com.yashoid.telegramstickies.app.ui.BottomSheetFragment;
import com.yashoid.telegramstickies.app.ui.staticstickereditor.ToolButton;
import com.yashoid.telegramstickies.app.ui.widget.BottomSheet;

public class CropPicker extends BottomSheetFragment implements Crop {

    private static final int HEIGHT = 72;

    public interface OnCropChoiceCallback {

        void onCropChoice(int choice);

    }

    private OnCropChoiceCallback mCallback;

    private int mCropChoice = -1;

    public CropPicker(OnCropChoiceCallback callback) {
        setMode(BottomSheet.MODE_WRAPPED);

        mCallback = callback;
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        Context context = inflater.getContext();

        int height = (int) (getResources().getDisplayMetrics().density * HEIGHT);

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.HORIZONTAL);

        ToolButton circularButton = new ToolButton(context);
        circularButton.setImageResId(R.drawable.ic_crop_circle);
        circularButton.setText(R.string.tool_crop_circle);
        circularButton.setOnClickListener(v -> onCropChoiceSelected(CROP_CIRCULAR));
        view.addView(circularButton, new LinearLayout.LayoutParams(0, height, 1));

        ToolButton rectButton = new ToolButton(context);
        rectButton.setImageResId(R.drawable.ic_crop_rect);
        rectButton.setText(R.string.tool_crop_rect);
        rectButton.setOnClickListener(v -> onCropChoiceSelected(CROP_RECT));
        view.addView(rectButton, new LinearLayout.LayoutParams(0, height, 1));

        ToolButton flipVerticalButton = new ToolButton(context);
        flipVerticalButton.setImageResId(R.drawable.ic_flip_vertical);
        flipVerticalButton.setText(R.string.tool_flip_vertical);
        flipVerticalButton.setOnClickListener(v -> onCropChoiceSelected(FLIP_VERTICAL));
        view.addView(flipVerticalButton, new LinearLayout.LayoutParams(0, height, 1));

        ToolButton flipHorizontalButton = new ToolButton(context);
        flipHorizontalButton.setImageResId(R.drawable.ic_flip_horizontal);
        flipHorizontalButton.setText(R.string.tool_flip_horizontal);
        flipHorizontalButton.setOnClickListener(v -> onCropChoiceSelected(FLIP_HORIZONTAL));
        view.addView(flipHorizontalButton, new LinearLayout.LayoutParams(0, height, 1));

        Unavailable.mark(flipVerticalButton);
        Unavailable.mark(flipHorizontalButton);

        return view;
    }

    private void onCropChoiceSelected(int choice) {
        mCropChoice = choice;

        dismiss();
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (mCropChoice != -1 && mCallback != null) {
            mCallback.onCropChoice(mCropChoice);
        }
    }

}
