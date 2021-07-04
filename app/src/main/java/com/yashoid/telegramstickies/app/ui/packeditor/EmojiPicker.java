package com.yashoid.telegramstickies.app.ui.packeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.EmojiList;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.ui.BottomSheetFragment;

import java.util.ArrayList;
import java.util.List;

public class EmojiPicker extends BottomSheetFragment {

    private static final float EMOJI_PADDING = 6;

    public interface OnEmojisSelectedListener {

        void onEmojisSelected(List<String> selectedEmojis);

    }

    private final OnEmojisSelectedListener mListener;

    private int mEmojiPadding;

    private View mButtonDone;

    private List<String> mSelectedEmojis = new ArrayList<>();
    private boolean mDone = false;

    public EmojiPicker(OnEmojisSelectedListener listener) {
        mListener = listener;
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        mEmojiPadding = (int) (getResources().getDisplayMetrics().density * EMOJI_PADDING);

        View view = inflater.inflate(R.layout.emojipicker, null, false);

        mButtonDone = view.findViewById(R.id.button_done);
        mButtonDone.setOnClickListener(v -> {
            mDone = true;
            dismiss();
        });

        RecyclerView listEmoji = view.findViewById(R.id.list_emoji);
        listEmoji.setLayoutManager(new GridLayoutManager(getContext(), 6, RecyclerView.VERTICAL, false));
        listEmoji.setAdapter(new EmojiAdapter(getContext()));

        updateState();

        return view;
    }

    private void updateState() {
        mButtonDone.setBackground(Utils.getThemedDrawable(getContext(), R.drawable.large_button_background, mSelectedEmojis.isEmpty() ? R.color.dark_gray : R.color.red));
        mButtonDone.setEnabled(!mSelectedEmojis.isEmpty());
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (mDone && !mSelectedEmojis.isEmpty()) {
            mListener.onEmojisSelected(mSelectedEmojis);
        }
    }

    private class EmojiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements EmojiList.EmojiListCallback {

        private EmojiList mEmojiList = null;

        private ImageView mSelectedEmoji = null;

        public EmojiAdapter(Context context) {
            EmojiList.get(context, this);
        }

        @Override
        public void onEmojiListReady(EmojiList emojiList) {
            mEmojiList = emojiList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            AppCompatImageView view = new AppCompatImageView(parent.getContext()) {

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY));
                }

            };

            GridLayoutManager.LayoutParams layoutParams = new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = layoutParams.rightMargin = layoutParams.topMargin = layoutParams.bottomMargin = mEmojiPadding;
            view.setLayoutParams(layoutParams);

            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(view) { };

            view.setOnClickListener(v -> {
                String emoji = mEmojiList.getEmojiAt(viewHolder.getBindingAdapterPosition());

                // Singular
                mSelectedEmojis.clear();
                mSelectedEmojis.add(emoji);
                if (mSelectedEmoji != null) {
                    mSelectedEmoji.setImageDrawable(null);
                }
                mSelectedEmoji = view;
                mSelectedEmoji.setImageResource(R.drawable.ic_emoji_selection);

                // Multiple
//                if (mSelectedEmojis.contains(emoji)) {
//                    mSelectedEmojis.remove(emoji);
//                    view.setImageDrawable(null);
//                }
//                else {
//                    mSelectedEmojis.add(emoji);
//                    view.setImageResource(R.drawable.ic_emoji_selection);
//                }

                updateState();
            });

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ImageView view = (ImageView) holder.itemView;

            view.setBackground(mEmojiList.getEmojiDrawable(position));

            if (mSelectedEmojis.contains(mEmojiList.getEmojiAt(position))) {
                view.setImageResource(R.drawable.ic_emoji_selection);
                mSelectedEmoji = view;
            }
            else {
                view.setImageDrawable(null);

                if (mSelectedEmoji == view) {
                    mSelectedEmoji = null;
                }
            }
        }

        @Override
        public int getItemCount() {
            return mEmojiList == null ? 0 : mEmojiList.getCount();
        }

    }

}
