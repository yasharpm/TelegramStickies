package com.yashoid.telegramstickies.app.ui;

import com.yashoid.mmv.Model;
import com.yashoid.mmv.Target;

abstract public class TargetActivity extends BaseActivity implements Target {

    private Model mModel = null;

    @Override
    public void setModel(Model model) {
        mModel = model;

        onModelChanged(mModel);
    }

    protected Model getModel() {
        return mModel;
    }

    @Override
    public void onFeaturesChanged(String... featureNames) {
        onModelChanged(mModel);
    }

    abstract protected void onModelChanged(Model model);

}
