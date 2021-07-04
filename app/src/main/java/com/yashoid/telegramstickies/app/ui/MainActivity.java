package com.yashoid.telegramstickies.app.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.StickadoApplication;
import com.yashoid.telegramstickies.app.ui.pack.AnimatedPackListFragment;
import com.yashoid.telegramstickies.app.ui.pack.StaticPackListFragment;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mImageStatic;
    private TextView mTextStatic;
    private ImageView mImageAnimated;
    private TextView mTextAnimated;

    private Class<? extends Fragment> mContentClass = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageStatic = findViewById(R.id.image_static);
        mTextStatic = findViewById(R.id.text_static);
        mImageAnimated = findViewById(R.id.image_animated);
        mTextAnimated = findViewById(R.id.text_animated);

        findViewById(R.id.button_static).setOnClickListener(this);
        findViewById(R.id.button_animated).setOnClickListener(this);
        findViewById(R.id.button_info).setOnClickListener(v -> new InfoBottomSheet().show(getSupportFragmentManager(), R.id.overlay));

        toggleStatic();

        File logFile = StickadoApplication.getLogFile(this);

        if (logFile.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle("Report bug")
                    .setMessage("There is a bug report for the recent crash. Share it with developers?")
                    .setCancelable(false)
                    .setPositiveButton("Share", (dialog, which) -> {
                        try {
                            Scanner scanner = new Scanner(new FileInputStream(logFile));

                            StringBuilder sb = new StringBuilder();

                            while (scanner.hasNextLine()) {
                                sb.append(scanner.nextLine());
                            }

                            scanner.close();

                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
                            startActivity(shareIntent);

                            logFile.delete();
                            dialog.dismiss();
                        } catch (Throwable t) {
                            logFile.delete();
                            dialog.dismiss();
                        }
                    })
                    .setNeutralButton("Close", (dialog, which) -> {
                        logFile.delete();
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_static) {
            toggleStatic();
        }
        else if (v.getId() == R.id.button_animated) {
            toggleAnimated();
        }
    }

    private void toggleStatic() {
        mImageStatic.setImageResource(R.drawable.ic_static_selected);
        mTextStatic.setTextColor(ContextCompat.getColor(this, R.color.default_text));
        mImageAnimated.setImageResource(R.drawable.ic_animated_unselected);
        mTextAnimated.setTextColor(ContextCompat.getColor(this, R.color.passive_text));

        switchContent(StaticPackListFragment.class);
    }

    private void toggleAnimated() {
        mImageStatic.setImageResource(R.drawable.ic_static_unselected);
        mTextStatic.setTextColor(ContextCompat.getColor(this, R.color.passive_text));
        mImageAnimated.setImageResource(R.drawable.ic_animated_selected);
        mTextAnimated.setTextColor(ContextCompat.getColor(this, R.color.default_text));

        switchContent(AnimatedPackListFragment.class);
    }

    private void switchContent(Class<? extends Fragment> fragmentClass) {
        if (fragmentClass == mContentClass) {
            return;
        }

        mContentClass = fragmentClass;

        try {
            Fragment currentContent = getCurrentContent();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (currentContent != null) {
                transaction
                        .remove(currentContent)
                        .detach(currentContent);
            }

            transaction.add(R.id.content, fragmentClass.newInstance()).commit();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate content fragment.", t);
        }
    }

    @Override
    protected int getContentLayoutId() {
        return InfoBottomSheet.mShowing ? R.id.overlay : R.id.content;
    }

}
