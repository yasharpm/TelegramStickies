package com.yashoid.telegramstickies.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.yashoid.telegramstickies.app.R;

public class InfoBottomSheet extends BottomSheetFragment {

    public static boolean mShowing = false;

    @Override
    protected View createView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.info, null, false);

        view.setOnClickListener(v -> {});
        view.findViewById(R.id.background_yashar).setOnClickListener(v -> openLink("https://www.linkedin.com/in/yasharpm/"));
        view.findViewById(R.id.background_diyar).setOnClickListener(v -> openLink("https://www.linkedin.com/in/diyar-karimzadeh-52a249170/"));

        return view;
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void show(FragmentManager fragmentManager, int containerId) {
        super.show(fragmentManager, containerId);

        mShowing = true;
    }

    @Override
    public void dismiss() {
        super.dismiss();

        mShowing = false;
    }

    @Override
    public void onClosed() {
        super.onClosed();

        mShowing = false;
    }

}
