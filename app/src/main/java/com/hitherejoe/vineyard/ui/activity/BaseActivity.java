package com.hitherejoe.vineyard.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.injection.component.ActivityComponent;
import com.hitherejoe.vineyard.injection.component.DaggerActivityComponent;
import com.hitherejoe.vineyard.injection.module.ActivityModule;

public class BaseActivity extends FragmentActivity {

    private ActivityComponent mActivityComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ActivityComponent getActivityComponent() {
        if (mActivityComponent == null) {
            mActivityComponent = DaggerActivityComponent.builder()
                    .activityModule(new ActivityModule(this))
                    .applicationComponent(VineyardApplication.get(this).getComponent())
                    .build();
        }
        return mActivityComponent;
    }

}
