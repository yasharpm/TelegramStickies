package com.yashoid.telegramstickies.app.ui.pack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yashoid.mmv.Managers;
import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;
import com.yashoid.mmv.SingleShotTarget;
import com.yashoid.telegramstickies.app.EmojiList;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Unavailable;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.model.sticker.Sticker;
import com.yashoid.telegramstickies.app.model.stickerpack.AnimatedStickerPack;
import com.yashoid.telegramstickies.app.model.stickerpack.StaticStickerPack;
import com.yashoid.telegramstickies.app.model.stickerpack.StickerPack;
import com.yashoid.telegramstickies.app.ui.TargetActivity;
import com.yashoid.telegramstickies.app.ui.TargetViewHolder;
import com.yashoid.telegramstickies.app.ui.animatedstickereditor.AnimatedStickerEditorActivity;
import com.yashoid.telegramstickies.app.ui.staticstickereditor.StickerEditorActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class StickerPackActivity extends TargetActivity {

    private static final String EXTRA_PACK_TYPE = "pack_type";
    private static final String EXTRA_PACK_ID = "pack_id";

    private static final String CREATE_STICKER_PACK_ACTION = "org.telegram.messenger.CREATE_STICKER_PACK";
    private static final String CREATE_STICKER_PACK_EMOJIS_EXTRA = "STICKER_EMOJIS";
    private static final String CREATE_STICKER_PACK_IMPORTER_EXTRA = "IMPORTER";

    private static final String EXPORT_PATH = "shared";

    public static Intent getIntent(Context context, String packType, String packId) {
        Intent intent = new Intent(context, StickerPackActivity.class);

        intent.putExtra(EXTRA_PACK_TYPE, packType);
        intent.putExtra(EXTRA_PACK_ID, packId);

        return intent;
    }

    private ActivityResultLauncher<String[]> mEditStickerLauncher;

    private TextView mTextTitle;

    private StickerAdapter mAdapter;

    private ModelFeatures mPendingNewSticker = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stickerpack);

        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_new).setOnClickListener(this::newSticker);
        findViewById(R.id.button_export).setOnClickListener(this::export);

        mTextTitle = findViewById(R.id.text_title);

        RecyclerView listSticker = findViewById(R.id.list_sticker);
        listSticker.setLayoutManager(new GridLayoutManager(this, 2, RecyclerView.VERTICAL, false));

        mAdapter = new StickerAdapter();
        listSticker.setAdapter(mAdapter);

        String packType = getIntent().getStringExtra(EXTRA_PACK_TYPE);
        String packId = getIntent().getStringExtra(EXTRA_PACK_ID);

        mEditStickerLauncher = registerForActivityResult(
                StaticStickerPack.TYPE_STATIC_STICKER_PACK.equals(packType) ?
                        StickerEditorActivity.getLaunchContract() :
                        AnimatedStickerEditorActivity.getLaunchContract(),
                stickerId -> {
            if (stickerId == null) {
                return;
            }

            ModelFeatures stickerFeatures = new ModelFeatures.Builder()
                    .add(Sticker.TYPE, Sticker.TYPE_STICKER)
                    .add(Sticker.ID, stickerId)
                    .build();

            Model pack = getModel();

            if (pack != null) {
                pack.perform(StickerPack.ADD, stickerFeatures);
                pack.cache(true);
            }
            else {
                mPendingNewSticker = stickerFeatures;
            }
        });

        ModelFeatures packFeatures = new ModelFeatures.Builder()
                .add(StickerPack.TYPE, packType)
                .add(StickerPack.ID, packId)
                .build();
        Managers.registerTarget(this, packFeatures);

        Unavailable.mark(findViewById(R.id.button_options));
    }

    private void newSticker(View v) {
        mEditStickerLauncher.launch(new String[] { getModel().get(StickerPack.ID), null });
    }

    private void export(View v) {
        List<ModelFeatures> items = getModel().get(StickerPack.ITEMS);

        ArrayList<Uri> uris = new ArrayList<>(items.size());
        ArrayList<String> emojis = new ArrayList<>(items.size());

        takeStickerInfo(items, 0, uris, emojis);
    }

    private void takeStickerInfo(List<ModelFeatures> items, int index, ArrayList<Uri> uris, ArrayList<String> emojis) {
        if (index >= items.size()) {
            export(uris, emojis);
            return;
        }

        SingleShotTarget.get(items.get(index), model -> {
            // TODO Info will be empty is list is too long. This code should wait for the update to arrive from the cache.

            List<String> emojiList = model.get(Sticker.EMOJIS);

            if (emojiList == null) {
                takeStickerInfo(items, index + 1, uris, emojis);
                return;
            }

            String emoji = emojiList.get(0);
            String sUri = model.get(Sticker.IMAGE_FILE);

            Uri uri = Uri.parse(sUri);

            String packType = getIntent().getStringExtra(EXTRA_PACK_TYPE);

            if (AnimatedStickerPack.TYPE_ANIMATED_STICKER_PACK.equals(packType)) {
                try {
                    InputStream input = getContentResolver().openInputStream(uri);

                    File outFile = Utils.someFile(StickerPackActivity.this, "tgs");
                    OutputStream output = new GZIPOutputStream(new FileOutputStream(outFile));

                    Utils.copy(input, output);

                    input.close();
                    output.close();

                    uri = Uri.fromFile(outFile);
                } catch (IOException e) {
                    uri = null;
                }
            }

            if (uri != null) {
                uris.add(uri);
                emojis.add(EmojiList.getCharacters(emoji));
            }

            takeStickerInfo(items, index + 1, uris, emojis);
        });
    }

    private void export(ArrayList<Uri> stickers, ArrayList<String> emojis) {
        Intent intent = new Intent(CREATE_STICKER_PACK_ACTION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(CREATE_STICKER_PACK_IMPORTER_EXTRA, getPackageName()); // extra for statistic purposes
        intent.setType("*/*");

        intent.putExtra(Intent.EXTRA_STREAM, stickers); // containing URIs to the stickers
        intent.putExtra(CREATE_STICKER_PACK_EMOJIS_EXTRA, emojis); // with relevant emojis for every sticker you importing.

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.stickerpack_impossible_export, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setModel(Model model) {
        super.setModel(model);

        if (mPendingNewSticker != null) {
            model.perform(StickerPack.ADD, mPendingNewSticker);
            model.cache(true);

            mPendingNewSticker = null;
        }
    }

    @Override
    protected void onModelChanged(Model model) {
        if (model != null) {
            mTextTitle.setText((String) model.get(StickerPack.NAME));
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected int getContentLayoutId() {
        return R.id.overlay;
    }

    private class StickerAdapter extends RecyclerView.Adapter<TargetViewHolder> {

        @NonNull
        @Override
        public TargetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sticker, parent, false);

            TargetViewHolder holder = new TargetViewHolder(view) {

                @Override
                protected void bind(View itemView, Model sticker) {
                    if (sticker == null) {
                        return;
                    }

                    List<String> emojis = sticker.get(Sticker.EMOJIS);

                    if (emojis != null && !emojis.isEmpty()) {
                        String emoji = emojis.get(0);

                        EmojiList.get(itemView.getContext(), emojiList -> ((ImageView) itemView.findViewById(R.id.image_emoji)).setImageDrawable(emojiList.getEmojiDrawable(emoji)));
                    }
                    else {
                        ((ImageView) itemView.findViewById(R.id.image_emoji)).setImageDrawable(null);
                    }

                    String sUri = sticker.get(Sticker.IMAGE_FILE);

                    if (sUri != null) {
                        Uri uri = Uri.parse(sUri);
                        StickerPackActivity.this.getModel().perform(StickerPack.BIND_PREVIEW, itemView.findViewById(R.id.image_preview), uri);
                    }
                }

            };

            Unavailable.markKeepAlpha(view);

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull TargetViewHolder holder, int position) {
            Managers.unregisterTarget(holder);

            ArrayList<ModelFeatures> items = getModel().get(StickerPack.ITEMS);

            Managers.registerTarget(holder, items.get(position));
        }

        @Override
        public int getItemCount() {
            Model pack = getModel();

            if (pack == null) {
                return 0;
            }

            ArrayList<?> items = pack.get(StickerPack.ITEMS);

            return items == null ? 0 : items.size();
        }

    }

}
