package com.yashoid.telegramstickies.app.ui.pack;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.yashoid.mmv.Managers;
import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;
import com.yashoid.mmv.Target;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Unavailable;
import com.yashoid.telegramstickies.app.model.ItemList;
import com.yashoid.telegramstickies.app.model.sticker.Sticker;
import com.yashoid.telegramstickies.app.model.stickerpack.StickerPack;
import com.yashoid.telegramstickies.app.ui.TargetViewHolder;

import java.util.List;

public class PackListAdapter extends RecyclerView.Adapter<TargetViewHolder> implements Target {

    private Model mPackList;

    public PackListAdapter(ModelFeatures modelFeatures) {
        Managers.registerTarget(this, modelFeatures);
    }

    @NonNull
    @Override
    public TargetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pack, parent, false);

        Unavailable.markKeepAlpha(view.findViewById(R.id.button_more));

        TargetViewHolder holder = new TargetViewHolder(view) {

            private final Target mStickerTarget = new Target() {

                private Model mSticker = null;

                @Override
                public void setModel(Model model) {
                    mSticker = model;
                    onFeaturesChanged();
                }

                @Override
                public void onFeaturesChanged(String... featureNames) {
                    String sImageUri = mSticker.get(Sticker.IMAGE_FILE);

                    if (sImageUri != null) {
                        Uri imageUri = Uri.parse(sImageUri);

                        getModel().perform(StickerPack.BIND_PREVIEW, itemView.findViewById(R.id.image_preview), imageUri);
                    }
                }

            };

            @Override
            protected void bind(View itemView, Model model) {
                ((TextView) itemView.findViewById(R.id.text_name)).setText((String) model.get(StickerPack.NAME));
                ((TextView) itemView.findViewById(R.id.text_creator)).setText((String) model.get(StickerPack.CREATOR));

                LottieAnimationView preview = itemView.findViewById(R.id.image_preview);

                List<ModelFeatures> stickerList = model.get(StickerPack.ITEMS);

                preview.setImageDrawable(null);

                if (!stickerList.isEmpty()) {
                    ModelFeatures stickerFeatures = stickerList.get(0);
                    Managers.registerTarget(mStickerTarget, stickerFeatures);
                }
            }

        };

        view.setOnClickListener(v -> {
            String packType = holder.getModel().get(StickerPack.TYPE);
            String packId = holder.getModel().get(StickerPack.ID);

            Context context = view.getContext();

            context.startActivity(StickerPackActivity.getIntent(context, packType, packId));
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TargetViewHolder holder, int position) {
        List<ModelFeatures> items = mPackList.get(ItemList.ITEMS);
        ModelFeatures modelFeatures = items.get(position);

        Managers.unregisterTarget(holder);
        Managers.registerTarget(holder, modelFeatures);
    }

    @Override
    public int getItemCount() {
        return mPackList == null ? 0 : ((List<?>) mPackList.get(ItemList.ITEMS)).size();
    }

    @Override
    public void setModel(Model model) {
        mPackList = model;

        notifyDataSetChanged();
    }

    @Override
    public void onFeaturesChanged(String... featureNames) {
        notifyDataSetChanged();
    }

}
