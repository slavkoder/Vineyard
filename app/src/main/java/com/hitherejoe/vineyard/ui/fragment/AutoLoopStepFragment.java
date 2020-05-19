package com.hitherejoe.vineyard.ui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.BusEvent;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.GuidedStepActivity;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

public class AutoLoopStepFragment extends GuidedStepFragment {

    @Inject Bus mEventBus;
    @Inject DataManager mDataManager;
    private PreferencesHelper mPreferencesHelper;

    private static final int ENABLED = 0;
    private static final int DISABLED = 1;
    private static final int OPTION_CHECK_SET_ID = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        mPreferencesHelper =
                VineyardApplication.get(getActivity()).getComponent().preferencesHelper();
        updateActions();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public int onProvideTheme() {
        return R.style.Theme_Example_Leanback_GuidedStep_First;
    }

    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.guided_step_auto_loop_title);
        String description = getString(R.string.guided_step_auto_loop_description);
        Drawable icon = getActivity().getDrawable(R.drawable.lopp);
        return new GuidanceStylist.Guidance(title, description, "", icon);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        if (getActivity() instanceof GuidedStepActivity) {
            addCheckedAction(actions, ENABLED,
                    getResources().getString(R.string.guided_step_auto_loop_enabled),
                    getResources().getString(R.string.guided_step_auto_loop_enabled_description),
                    false);
            addCheckedAction(actions, DISABLED,
                    getResources().getString(R.string.guided_step_auto_loop_disabled),
                    getResources().getString(R.string.guided_step_auto_loop_disabled_description),
                    false);
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action != null) {
            mPreferencesHelper.putAutoLoop(action.getId() == ENABLED);
            mEventBus.post(new BusEvent.AutoLoopUpdated());
            getActivity().finish();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private void updateActions() {
        boolean shouldAutoLoop = mPreferencesHelper.getShouldAutoLoop();
        List<GuidedAction> actions = getActions();
        for (int i = 0; i < actions.size(); i++) {
            GuidedAction action = actions.get(i);
            action.setChecked((action.getId() == ENABLED) == shouldAutoLoop);
        }
    }

    private static void addCheckedAction(List<GuidedAction> actions, long id,
                                         String title, String desc, boolean checked) {
        GuidedAction guidedAction = new GuidedAction.Builder()
                .id(id)
                .title(title)
                .description(desc)
                .checkSetId(OPTION_CHECK_SET_ID)
                .build();
        guidedAction.setChecked(checked);
        actions.add(guidedAction);
    }

}