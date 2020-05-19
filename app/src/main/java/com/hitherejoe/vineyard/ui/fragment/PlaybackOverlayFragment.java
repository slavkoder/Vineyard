package com.hitherejoe.vineyard.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.leanback.app.PlaybackSupportFragment;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.ControlButtonPresenterSelector;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import androidx.leanback.widget.PlaybackControlsRow.RepeatAction;
import androidx.leanback.widget.PlaybackControlsRow.SkipNextAction;
import androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import androidx.leanback.widget.PlaybackControlsRowPresenter;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.BusEvent;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.presenter.CardPresenter;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;


public class PlaybackOverlayFragment extends PlaybackSupportFragment {

    @Inject Bus mEventBus;
    @Inject DataManager mDataManager;

    private static final boolean SHOW_DETAIL = true;
    private static final boolean HIDE_MORE_ACTIONS = false;
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;
    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 240;
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private static final int SIMULATED_BUFFERED_TIME = 10000;
    private static final int CLICK_TRACKING_DELAY = 1000;
    private static final int INITIAL_SPEED = 10000;

    public static final String CUSTOM_ACTION_LOOP = "custom_action_loop";
    public static final String CUSTOM_ACTION_SKIP_VIDEO = "custom_action_skip_video";
    public static final int STATE_LOOPING = 2323;

    private ArrayList<Post> mItems;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private Handler mClickTrackingHandler;
    private PlaybackControlsRow mPlaybackControlsRow;
    private PlayPauseAction mPlayPauseAction;
    private Post mSelectedPost;
    private PreferencesHelper mPreferencesHelper;
    private RepeatAction mRepeatAction;
    private SkipNextAction mSkipNextAction;
    private SkipPreviousAction mSkipPreviousAction;
    private Handler mHandler;
    private Runnable mRunnable;
    private int mFfwRwdSpeed;
    private Timer mClickTrackingTimer;
    private int mClickCount;

    private MediaController mMediaController;
    private MediaController.Callback mMediaControllerCallback = new MediaControllerCallback();
    private int mCurrentPlaybackState;

    private boolean mIsAutoLoopEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        mPreferencesHelper = mDataManager.getPreferencesHelper();
        mIsAutoLoopEnabled = mPreferencesHelper.getShouldAutoLoop();
        mFfwRwdSpeed = INITIAL_SPEED;

        mClickTrackingHandler = new Handler();
        mItems = new ArrayList<>();
        mSelectedPost = getActivity()
                .getIntent().getParcelableExtra(PlaybackActivity.POST);
        mHandler = new Handler();
        mItems = getActivity().getIntent().getParcelableArrayListExtra(PlaybackActivity.POST_LIST);
        if (mItems == null || mSelectedPost == null) {
            throw new IllegalArgumentException(
                    "PlaybackOverlayFragment requires both a Post object and list of posts!");
        }

        setBackgroundType(BACKGROUND_TYPE);
        setFadingEnabled(false);
        setupRows();
    }

    // TODO: There's currently a bug here, so we need to Override both onAttach methods

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setupMediaController();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setupMediaController();
    }

    @Override
    public void onStop() {
        stopProgressAutomation();
        mRowsAdapter = null;
        super.onStop();
    }

    @Override
    public void onDetach() {
        if (mMediaController != null) mMediaController.unregisterCallback(mMediaControllerCallback);
        super.onDetach();
    }

    public void togglePlayback(boolean playPause) {
        if (playPause) {
            mMediaController.getTransportControls().play();
        } else {
            mMediaController.getTransportControls().pause();
        }
    }

    protected void updateVideoImage(String uri) {
        Glide.with(getActivity())
                .load(uri)
                .centerCrop()
                .into(new SimpleTarget<GlideDrawable>(CARD_WIDTH, CARD_HEIGHT) {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                                                GlideAnimation<? super GlideDrawable> glideAnimation) {
                        mPlaybackControlsRow.setImageDrawable(resource);
                        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
                    }
                });
    }

    private void setupMediaController() {
        if (mMediaController == null) {
            mMediaController = getActivity().getMediaController();
            mMediaController.registerCallback(mMediaControllerCallback);
        }
    }

    private void setupRows() {
        ClassPresenterSelector ps = new ClassPresenterSelector();
        PlaybackControlsRowPresenter playbackControlsRowPresenter;

        if (SHOW_DETAIL) {
            playbackControlsRowPresenter =
                    new PlaybackControlsRowPresenter(new DescriptionPresenter());
        } else {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
        }

        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {
                if (action.getId() == mPlayPauseAction.getId()) {
                    togglePlayback(mPlayPauseAction.getIndex() == PlayPauseAction.PLAY);
                } else if (action.getId() == mSkipNextAction.getId()) {
                    next(true);
                } else if (action.getId() == mSkipPreviousAction.getId()) {
                    prev(true);
                } else if (action.getId() == mRepeatAction.getId()) {
                    loopVideos();
                }
                if (action instanceof PlaybackControlsRow.MultiAction) {
                    notifyChanged(action);
                }
            }
        });
        playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS);

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        addPlaybackControlsRow();
        addOtherRows();

        setAdapter(mRowsAdapter);
    }

    private void addPlaybackControlsRow() {
        if (SHOW_DETAIL) {
            mPlaybackControlsRow = new PlaybackControlsRow(mSelectedPost);
        } else {
            mPlaybackControlsRow = new PlaybackControlsRow();
        }
        mRowsAdapter.add(mPlaybackControlsRow);

        updatePlaybackRow();

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

        Activity activity = getActivity();
        mPlayPauseAction = new PlayPauseAction(activity);
        mRepeatAction = new RepeatAction(activity);
        mSkipNextAction = new SkipNextAction(activity);
        mSkipPreviousAction = new SkipPreviousAction(activity);

        mRepeatAction.setIcon(getRepeatDrawable());

        // Add main controls to primary adapter.
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        mPrimaryActionsAdapter.add(mSkipNextAction);

        // Add repeat control to secondary adapter.
        mSecondaryActionsAdapter.add(mRepeatAction);
    }

    private Drawable getRepeatDrawable() {
        int resourceId = mIsAutoLoopEnabled ? R.drawable.ic_repeat_green : R.drawable.ic_repeat_white;
        return ContextCompat.getDrawable(getActivity(), resourceId);
    }

    private void notifyChanged(Action action) {
        int index = mPrimaryActionsAdapter.indexOf(action);
        if (index >= 0) {
            mPrimaryActionsAdapter.notifyArrayItemRangeChanged(index, 1);
        } else {
            index = mSecondaryActionsAdapter.indexOf(action);
            if (index >= 0) mSecondaryActionsAdapter.notifyArrayItemRangeChanged(index, 1);
        }
    }

    private void updatePlaybackRow() {
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
    }

    private void updatePostView(String title, String studio, String cardImageUrl, long duration) {
        Post item = (Post) mPlaybackControlsRow.getItem();
        if (item != null) {
            item.description = title;
            item.username = studio;
        }
        mPlaybackControlsRow.setTotalTime((int) duration);

        if (mRowsAdapter != null) updateVideoImage(cardImageUrl);
    }

    private void addOtherRows() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter(getActivity()));
        listRowAdapter.addAll(0, mItems);
        HeaderItem header = new HeaderItem(0, getResources().getString(R.string.related_posts));
        mRowsAdapter.add(new ListRow(header, listRowAdapter));

    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void startProgressAutomation() {
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    int updatePeriod = getUpdatePeriod();
                    int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
                    int totalTime = mPlaybackControlsRow.getTotalTime();
                    mPlaybackControlsRow.setCurrentTime(currentTime);
                    mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

                    if (totalTime > 0 && totalTime <= currentTime) {
                        stopProgressAutomation();
                        next(false);
                    } else {
                        mHandler.postDelayed(this, updatePeriod);
                    }
                }
            };
            mHandler.postDelayed(mRunnable, getUpdatePeriod());
        }
    }

    private void next(boolean wasSkipPressed) {
        if (wasSkipPressed) {
            mMediaController.getTransportControls().sendCustomAction(CUSTOM_ACTION_SKIP_VIDEO, null);
        }
        mMediaController.getTransportControls().skipToNext();
    }

    private void loopVideos() {
        mIsAutoLoopEnabled = !mIsAutoLoopEnabled;
        mPreferencesHelper.putAutoLoop(mIsAutoLoopEnabled);
        mRepeatAction.setIcon(getRepeatDrawable());
        mEventBus.post(new BusEvent.AutoLoopUpdated());
        Bundle bundle = new Bundle();
        bundle.putBoolean(PlaybackActivity.EXTRA_IS_LOOP_ENABLED, mIsAutoLoopEnabled);
        mMediaController.getTransportControls().sendCustomAction(CUSTOM_ACTION_LOOP, bundle);
    }

    private void prev(boolean wasSkipPressed) {
        if (wasSkipPressed) {
            mMediaController.getTransportControls().sendCustomAction(CUSTOM_ACTION_SKIP_VIDEO, null);
        }
        mMediaController.getTransportControls().skipToPrevious();
    }

    private void fastForward() {
        startClickTrackingTimer();
        mMediaController.getTransportControls().fastForward();
    }

    private void fastRewind() {
        startClickTrackingTimer();
        mMediaController.getTransportControls().rewind();
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
            mRunnable = null;
        }
    }

    private void startClickTrackingTimer() {
        if (mClickTrackingTimer != null) {
            mClickCount++;
            mClickTrackingTimer.cancel();
        } else {
            mClickCount = 0;
            mFfwRwdSpeed = INITIAL_SPEED;
        }
        mClickTrackingTimer = new Timer();
        mClickTrackingTimer.schedule(new UpdateFfwRwdSpeedTask(), CLICK_TRACKING_DELAY);
    }

    static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            viewHolder.getTitle().setText(((Post) item).description);
            viewHolder.getSubtitle().setText(((Post) item).username);
        }
    }

    private class UpdateFfwRwdSpeedTask extends TimerTask {

        @Override
        public void run() {
            mClickTrackingHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mClickCount == 0) {
                        mFfwRwdSpeed = INITIAL_SPEED;
                    } else if (mClickCount == 1) {
                        mFfwRwdSpeed *= 2;
                    } else if (mClickCount >= 2) {
                        mFfwRwdSpeed *= 4;
                    }
                    mClickCount = 0;
                    mClickTrackingTimer = null;
                }
            });
        }
    }

    private class MediaControllerCallback extends MediaController.Callback {

        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            if (state.getState() == PlaybackState.STATE_PLAYING
                    && mCurrentPlaybackState == PlaybackState.STATE_PLAYING) {
                startProgressAutomation();
                setFadingEnabled(true);
            } else if (state.getState() == PlaybackState.STATE_PLAYING) {
                mCurrentPlaybackState = PlaybackState.STATE_PLAYING;
                startProgressAutomation();
                setFadingEnabled(true);
                mPlayPauseAction.setIndex(PlayPauseAction.PAUSE);
                mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PAUSE));
                notifyChanged(mPlayPauseAction);
            } else if (state.getState() == PlaybackState.STATE_PAUSED
                    && mCurrentPlaybackState != PlaybackState.STATE_PAUSED) {
                mCurrentPlaybackState = PlaybackState.STATE_PAUSED;
                stopProgressAutomation();
                setFadingEnabled(false);
                mPlayPauseAction.setIndex(PlayPauseAction.PLAY);
                mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PLAY));
                notifyChanged(mPlayPauseAction);
            } else if (state.getState() == PlaybackState.STATE_SKIPPING_TO_NEXT) {
                mCurrentPlaybackState = PlaybackState.STATE_SKIPPING_TO_NEXT;
                setFadingEnabled(true);
                notifyChanged(mSkipNextAction);
            } else if (state.getState() == PlaybackState.STATE_SKIPPING_TO_PREVIOUS) {
                mCurrentPlaybackState = PlaybackState.STATE_SKIPPING_TO_PREVIOUS;
                startProgressAutomation();
                setFadingEnabled(true);
                notifyChanged(mSkipPreviousAction);
            } else if (( (int) state.getState()) == STATE_LOOPING) {
                mCurrentPlaybackState = STATE_LOOPING;
                startProgressAutomation();
            }

            int currentTime;

            if (state.getState() == PlaybackState.STATE_PAUSED
                    || state.getState() == PlaybackState.STATE_PLAYING) {
                currentTime = mPlaybackControlsRow.getCurrentTime();
            } else {
                currentTime = (int) state.getPosition();
            }
            mPlaybackControlsRow.setCurrentTime(currentTime);
            mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            updatePostView(
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
                    metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI),
                    metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
            );
        }
    }
}