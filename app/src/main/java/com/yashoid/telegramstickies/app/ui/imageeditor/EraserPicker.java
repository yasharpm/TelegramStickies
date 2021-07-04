package com.yashoid.telegramstickies.app.ui.imageeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.yashoid.sequencelayout.SequenceLayout;
import com.yashoid.sequencelayout.Span;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.ui.BottomSheetFragment;
import com.yashoid.telegramstickies.app.ui.widget.BottomSheet;
import com.yashoid.telegramstickies.app.ui.widget.SeekBar;

public class EraserPicker extends BottomSheetFragment {

    public interface OnEraserSelectedCallback {

        void onEraserSelected(int size, float opacity);

    }

    private OnEraserSelectedCallback mCallback;

    private Span mPreviewWidthSpan;
    private Span mPreviewHeightSpan;

    private View mPreview;
    private TextView mTextSize;
    private TextView mTextOpacity;

    private int mSize = 60;
    private float mOpacity = 1;

    private boolean mSet = false;

    public EraserPicker(int size, float opacity, OnEraserSelectedCallback callback) {
        setMode(BottomSheet.MODE_WRAPPED);

        mCallback = callback;

        if (size >= 0) {
            mSize = size;
        }

        if (opacity >= 0) {
            mOpacity = opacity;
        }
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        SequenceLayout view = (SequenceLayout) inflater.inflate(R.layout.eraserpicker, null, false);

        mPreviewWidthSpan = view.findSequenceById("h").getSpans().get(1);
        mPreviewHeightSpan = view.findSequenceById("v").getSpans().get(1);

        mPreview = view.findViewById(R.id.preview);
        mTextSize = view.findViewById(R.id.text_size);
        mTextOpacity = view.findViewById(R.id.text_opacity);

        view.findViewById(R.id.button_cancel).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.button_set).setOnClickListener(v -> {
            mSet = true;
            dismiss();
        });

        SeekBar seekSize = view.findViewById(R.id.seek_size);
        SeekBar seekOpacity = view.findViewById(R.id.seek_opacity);

        seekSize.setOnSeekChangedListener((v, value) -> updateSize(value));
        seekOpacity.setOnSeekChangedListener((v, value) -> updateOpacity(value / 100f));

        seekSize.setValue(mSize);
        seekOpacity.setValue((int) (mOpacity * 100));

        updateSize(mSize);
        updateOpacity(mOpacity);

        mPreview.setBackground(Utils.createOvalShape(ContextCompat.getColor(getContext(), R.color.default_text)));

        return view;
    }

    private void updateSize(int size) {
        mSize = size;

        mPreviewWidthSpan.size = mSize;
        mPreviewHeightSpan.size = mSize;
        mPreview.requestLayout();

        mTextSize.setText(getString(R.string.eraserpicker_size) + " " + mSize + "px");
    }

    private void updateOpacity(float opacity) {
        mOpacity = opacity;

        mPreview.setAlpha(mOpacity);

        mTextOpacity.setText(getString(R.string.eraserpicker_opacity) + " " + (int) (mOpacity * 100) + "%");
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (mSet && mCallback != null) {
            mCallback.onEraserSelected(mSize, mOpacity);
        }
    }

}
