package com.yashoid.telegramstickies.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.yashoid.telegramstickies.app.ui.widget.BottomSheet;

abstract public class BottomSheetFragment extends BaseFragment implements BottomSheet.OnOpenCloseListener {

    private int mMode = BottomSheet.MODE_FULL_SCREEN;

    private BottomSheet mBottomSheet;

    private boolean mOpened = false;
    private boolean mDismissed = false;

    public void setMode(int mode) {
        mMode = mode;
    }

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = new BottomSheet(inflater.getContext());

        view.addView(createView(inflater));

        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    abstract protected View createView(LayoutInflater inflater);

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBottomSheet = (BottomSheet) view;
        mBottomSheet.setMode(mMode);
        mBottomSheet.setOnOpenCloseListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mOpened) {
            mOpened = true;
            mBottomSheet.open();
        }
    }

    @Override
    public void onOpened() {

    }

    @Override
    public void onClosed() {
        mDismissed = true;

        getParentFragmentManager()
                .beginTransaction()
                .remove(BottomSheetFragment.this)
                .detach(BottomSheetFragment.this)
                .commit();
    }

    public void show(FragmentManager fragmentManager, int containerId) {
        fragmentManager
                .beginTransaction()
                .add(containerId, this)
                .commit();
    }

    public void dismiss() {
        if (mDismissed) {
            return;
        }

        mDismissed = true;
        mBottomSheet.close();
    }

    @Override
    public boolean onBackPressed() {
        dismiss();
        return true;
    }

}
