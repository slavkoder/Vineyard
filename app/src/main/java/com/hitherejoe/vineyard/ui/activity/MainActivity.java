package com.hitherejoe.vineyard.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.leanback.app.ErrorFragment;
import androidx.leanback.app.ErrorSupportFragment;

import android.view.View;
import android.widget.FrameLayout;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.ui.fragment.MainFragment;
import com.hitherejoe.vineyard.util.NetworkUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @Bind(R.id.frame_container)
    FrameLayout mFragmentContainer;

    private Fragment mBrowseFragment;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);

        mFragmentContainer = findViewById(R.id.frame_container);

        if (NetworkUtil.isNetworkConnected(this)) {
            mBrowseFragment = MainFragment.newInstance();
        } else {
            mBrowseFragment = buildErrorFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(mFragmentContainer.getId(), mBrowseFragment).commit();
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    public boolean isFragmentActive() {
        return mBrowseFragment instanceof MainFragment &&
                mBrowseFragment.isAdded() &&
                !mBrowseFragment.isDetached() &&
                !mBrowseFragment.isRemoving() &&
                !((MainFragment) mBrowseFragment).isStopping();
    }

    private ErrorSupportFragment buildErrorFragment() {
        ErrorSupportFragment errorFragment = new ErrorSupportFragment();
        errorFragment.setTitle(getString(R.string.text_error_oops_title));
        errorFragment.setMessage(getString(R.string.error_message_network_needed_app));
        errorFragment.setButtonText(getString(R.string.text_close));
        errorFragment.setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return errorFragment;
    }

}
