package com.yashoid.telegramstickies.app;

import android.view.View;
import android.widget.Toast;

public class Unavailable {

    public static void mark(View view) {
        view.setAlpha(0.5f);
        view.setOnClickListener(v -> Toast.makeText(view.getContext(), R.string.not_available, Toast.LENGTH_SHORT).show());
    }

    public static void markKeepAlpha(View view) {
        view.setOnClickListener(v -> Toast.makeText(view.getContext(), R.string.not_available, Toast.LENGTH_SHORT).show());
    }

}
