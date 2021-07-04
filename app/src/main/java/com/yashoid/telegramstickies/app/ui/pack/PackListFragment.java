package com.yashoid.telegramstickies.app.ui.pack;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.ui.BaseFragment;
import com.yashoid.telegramstickies.app.ui.PackInformationActivity;

abstract public class PackListFragment extends BaseFragment {

    private final int mThemeColorId;
    private final int mImageResId;
    private final int mCreateTextResId;
    private final int mInformationTitleTextResId;
    private final String mStickerType;
    private final Class<? extends RecyclerView.Adapter<?>> mAdapterClass;

    private final ActivityResultLauncher<Object> mPackInformationResultHandler;

    private RecyclerView.Adapter<?> mAdapter;

    public PackListFragment(int themeColorId, int imageResId, int createTextResId,
                            int informationTitleTextResId, String stickerType,
                            Class<? extends RecyclerView.Adapter<?>> adapterClass) {
        mThemeColorId = themeColorId;
        mImageResId = imageResId;
        mCreateTextResId = createTextResId;
        mInformationTitleTextResId = informationTitleTextResId;
        mStickerType = stickerType;
        mAdapterClass = adapterClass;

        mPackInformationResultHandler = registerForActivityResult(new ActivityResultContract<Object, String>() {

            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object input) {
                return PackInformationActivity.getIntent(context, mInformationTitleTextResId, mThemeColorId, mStickerType);
            }

            @Override
            public String parseResult(int resultCode, @Nullable Intent intent) {
                if (resultCode == Activity.RESULT_OK) {
                    return intent.getStringExtra(PackInformationActivity.EXTRA_PACK_ID);
                }

                return null;
            }

        }, result -> {
            if (result != null) {
                onNewPackCreated(result);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_packlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((ImageView) view.findViewById(R.id.image_onboarding)).setImageResource(mImageResId);
        ((TextView) view.findViewById(R.id.text_onboarding_title)).setTextColor(ContextCompat.getColor(getContext(), mThemeColorId));

        TextView buttonCreate = view.findViewById(R.id.button_create);

        buttonCreate.setBackground(Utils.getThemedDrawable(getContext(), R.drawable.large_button_background, mThemeColorId));
        buttonCreate.setText(mCreateTextResId);
        buttonCreate.setOnClickListener(this::onCreateClicked);

        RecyclerView listPack = view.findViewById(R.id.list_pack);
        listPack.setLayoutManager(new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false));

        try {
            mAdapter = mAdapterClass.newInstance();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate adapter.", t);
        }

        listPack.setAdapter(mAdapter);

        mAdapter.registerAdapterDataObserver(mDataObserver);

        updateState();
    }

    private final RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onChanged() {
            updateState();
        }

    };

    private void updateState() {
        int onBoardingVisibility = mAdapter.getItemCount() == 0 ? View.VISIBLE : View.INVISIBLE;

        getView().findViewById(R.id.image_onboarding).setVisibility(onBoardingVisibility);
        getView().findViewById(R.id.text_onboarding_title).setVisibility(onBoardingVisibility);
        getView().findViewById(R.id.text_onboarding).setVisibility(onBoardingVisibility);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mAdapter.unregisterAdapterDataObserver(mDataObserver);
        mAdapter = null;
    }

    private void onCreateClicked(View v) {
        mPackInformationResultHandler.launch(null);
    }

    abstract protected void onNewPackCreated(String id);

}
