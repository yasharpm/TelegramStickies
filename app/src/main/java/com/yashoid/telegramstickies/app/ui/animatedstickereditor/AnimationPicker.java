package com.yashoid.telegramstickies.app.ui.animatedstickereditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.yashoid.telegramstickies.app.AnimationList;
import com.yashoid.telegramstickies.app.ui.BottomSheetFragment;
import com.yashoid.telegramstickies.app.ui.widget.SquareLottieView;

import java.io.IOException;

public class AnimationPicker extends BottomSheetFragment {

    private static final float ANIMATION_MARGIN = 14;

    public interface OnAnimationSelectedListener {

        void onAnimationSelected(String animation);

    }

    private OnAnimationSelectedListener mListener;

    private AnimationList mAnimationList;

    private String mSelectedAnimation = null;

    public AnimationPicker(OnAnimationSelectedListener listener) {
        mListener = listener;
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        final Context context = inflater.getContext();

        RecyclerView view = new RecyclerView(context);
        view.setLayoutManager(new GridLayoutManager(context, 3, RecyclerView.VERTICAL, false));
        view.setAdapter(new AnimationAdapter(context));

        return view;
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (mSelectedAnimation != null) {
            mListener.onAnimationSelected(mSelectedAnimation);
        }
    }

    private class AnimationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public AnimationAdapter(Context context) {
            AnimationList.get(context, animationList -> {
                mAnimationList = animationList;
                notifyDataSetChanged();
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LottieAnimationView view = new SquareLottieView(parent.getContext());
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = params.rightMargin = params.topMargin = params.bottomMargin = (int) (parent.getContext().getResources().getDisplayMetrics().density * ANIMATION_MARGIN);
            view.setLayoutParams(params);

            view.setRepeatMode(LottieDrawable.RESTART);
            view.setRepeatCount(LottieDrawable.INFINITE);

            RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) { };

            view.setOnClickListener(v -> {
                mSelectedAnimation = mAnimationList.getAnimationAt(holder.getBindingAdapterPosition());
                dismiss();
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            LottieAnimationView view = (LottieAnimationView) holder.itemView;

            try {
                String json = mAnimationList.getAnimationJSON(position);
                view.setAnimationFromJson(json, mAnimationList.getAnimationAt(position));
                view.playAnimation();
            } catch (IOException e) {
                view.setImageDrawable(null);
            }
        }

        @Override
        public int getItemCount() {
            return mAnimationList == null ? 0 : mAnimationList.getCount();
        }

    }

}
