package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;

import com.hitherejoe.vineyard.data.model.Option;
import com.hitherejoe.vineyard.ui.presenter.IconItemPresenter;

public class OptionsAdapter extends ArrayObjectAdapter {

    private IconItemPresenter mOptionsItemPresenter;

    public OptionsAdapter(Context context) {
        mOptionsItemPresenter = new IconItemPresenter();
        setPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                return mOptionsItemPresenter;
            }
        });
    }

    public void addOption(Option option) {
        add(option);
    }

    public void updateOption(Option option) {
        Option first = (Option) get(0);
        first.value = option.value;
        notifyChanged();
    }
}
