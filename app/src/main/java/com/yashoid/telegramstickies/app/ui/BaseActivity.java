package com.yashoid.telegramstickies.app.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

abstract public class BaseActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        BaseFragment fragment = getFocusedFragment();

        if (fragment == null || !fragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    protected BaseFragment getFocusedFragment() {
        Fragment content = getCurrentContent();
        return (content instanceof BaseFragment) ? (BaseFragment) content : null;
    }

    protected Fragment getCurrentContent() {
        int contentLayoutId = getContentLayoutId();

        Fragment fragment = contentLayoutId == 0 ? null : getSupportFragmentManager().findFragmentById(contentLayoutId);

        return (fragment == null || fragment.isDetached()) ? null : fragment;
    }

    abstract protected int getContentLayoutId();

}
