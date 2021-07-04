package com.yashoid.telegramstickies.app.ui;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yashoid.mmv.Model;
import com.yashoid.mmv.Target;

abstract public class TargetViewHolder extends RecyclerView.ViewHolder implements Target {

    private Model mModel = null;

    public TargetViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void setModel(Model model) {
        mModel = model;

        bind(itemView, mModel);
    }

    public Model getModel() {
        return mModel;
    }

    @Override
    public void onFeaturesChanged(String... featureNames) {
        bind(itemView, mModel);
    }

    abstract protected void bind(View itemView, Model model);

}
