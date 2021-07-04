package com.yashoid.telegramstickies.app.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yashoid.telegramstickies.app.ColorList;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.ui.widget.ColorView;

public class ColorPicker extends BottomSheetFragment {

    public interface OnColorSelectedListener {

        void onColorSelected(int color);

    }

    private final OnColorSelectedListener mListener;

    private int mColor = 0;
    private boolean mColorSelected = false;

    public ColorPicker(OnColorSelectedListener listener) {
        mListener = listener;
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        final Context context = inflater.getContext();

        LinearLayout view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.CENTER_HORIZONTAL);

        AppCompatTextView textTitle = new AppCompatTextView(context);
        textTitle.setTextSize(20);
        textTitle.setTextColor(ContextCompat.getColor(context, R.color.default_text));
        textTitle.setText(R.string.colorpicker_title);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.addView(textTitle, params);

        RecyclerView listColor = new RecyclerView(context);
        listColor.setLayoutManager(new GridLayoutManager(context, 5, RecyclerView.VERTICAL, false));
        listColor.setAdapter(new ColorAdapter());
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.addView(listColor, params);

        return view;
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (mColorSelected) {
            mListener.onColorSelected(mColor);
        }
    }

    private class ColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ColorView view = new ColorView(parent.getContext());

            RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) { };

            view.setOnClickListener(v -> {
                mColor = ColorList.COLORS[holder.getBindingAdapterPosition()];
                mColorSelected = true;
                dismiss();
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((ColorView) holder.itemView).setColor(ColorList.COLORS[position]);
        }

        @Override
        public int getItemCount() {
            return ColorList.COLORS.length;
        }

    }
}
