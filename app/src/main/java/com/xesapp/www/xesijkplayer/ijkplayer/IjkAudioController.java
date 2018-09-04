// +----------------------------------------------------------------------
// | Project:   Xabad  
// +----------------------------------------------------------------------
// | CreateTime: 15/11/22  下午3:09
// +----------------------------------------------------------------------
// | Author:     xab(xab@xabad.cn)
// +----------------------------------------------------------------------
// | Description:
// +----------------------------------------------------------------------
package com.xesapp.www.xesijkplayer.ijkplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xesapp.www.xesijkplayer.R;

import java.util.Formatter;
import java.util.Locale;


/**
 * DESC   :
 * AUTHOR : lishaowei
 */
public class IjkAudioController extends LinearLayout implements IAudioController {

    private Context context;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    ImageButton mPauseButton;
    TextView mCurrentTime;
    TextView mEndTime;
    SeekBar mProgress;
    private MediaController.MediaPlayerControl mPlayer;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    View rootView, mAnchor;
    boolean mDragging;
    public boolean testing;
    private boolean isEnable;

    public VideoControllerCallback getCallback() {
        return callback;
    }

    public void setCallback(VideoControllerCallback callback) {
        this.callback = callback;
    }

    private VideoControllerCallback callback;
    //IDanmakuView danmakuView;

    public IjkAudioController(Context context) {
        super(context);
        initView(context);
    }

    public IjkAudioController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        rootView = makeControllerView();
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(rootView, params);
    }


    private View makeControllerView() {
        View controller = LayoutInflater.from(context).inflate(R.layout.audio_controller, null);
        mCurrentTime = controller.findViewById(R.id.mediacontroller_time_current);
        mEndTime = controller.findViewById(R.id.mediacontroller_time_total);
        mPauseButton = controller.findViewById(R.id.mediacontroller_play_pause);
        mProgress = controller.findViewById(R.id.mediacontroller_seekbar);

        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mPauseButton.setOnClickListener(mPauseListener);
        mPauseButton.setEnabled(false);
        isEnable = false;
        setButtonPauseImage();
        mProgress.setMax(1000);
        mProgress.setThumb(context.getResources().getDrawable(isEnable ? R.mipmap.icon_audio_controller_now : R.mipmap.icon_audio_controller_now_gray));
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
        return controller;
    }

    public void setClickAble() {
        mPauseButton.setEnabled(true);
        isEnable = true;

    }

    @Override
    public void setTest(boolean isTest) {
        testing = isTest;
        if (mPauseButton != null) {
            if (!isTest) {
                setButtonPauseImage();
            } else {
                mProgress.setEnabled(false);
                setButtonPlayImage();
            }
        }
    }

    public void overTesting() {
        testing = false;
        mProgress.setEnabled(true);
        mPauseButton.setEnabled(true);
        isEnable = true;
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    @Override
    public void update() {
        updatePausePlay();
    }

    @Override
    public void updateText() {
        setProgress();
    }

    @Override
    public void hide() {
        try {
            mHandler.removeMessages(SHOW_PROGRESS);
            setButtonPlayImage();
            if (mPlayer != null) {
                mPlayer.seekTo(0);
                setProgress();
            }
        } catch (IllegalArgumentException ex) {
        }
    }

    private void updatePausePlay() {
        if (rootView == null || mPauseButton == null || mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            setButtonPauseImage();
        } else {
            setButtonPlayImage();
        }
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null) {
            mEndTime.setText(stringForTime(duration));
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime(position));
        }

        return position;
    }

    private final OnClickListener mPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
        }
    };

    private void doPauseResume() {
        if (mPlayer == null) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            if (callback != null) {
                callback.onPaused();
            }
            setButtonPlayImage();
            //danmakuView.pause();
        } else {
            mPlayer.start();
            if (callback != null) {
                callback.onStarted();
            }
            setButtonPauseImage();
            if (testing) {
                startTesting();
            }
            //danmakuView.resume();
        }
    }

    private void startTesting() {
        if (mProgress != null && mPauseButton != null) {
            mProgress.setEnabled(false);
            mPauseButton.setEnabled(false);
            isEnable = false;
        }
        testing = false;
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser || mPlayer == null) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            mPlayer.seekTo((int) newposition);
            if (mCurrentTime != null) {
                mCurrentTime.setText(stringForTime((int) newposition));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };

    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void setButtonPlayImage() {
        if (mPauseButton != null) {
            mPauseButton.setImageResource(isEnable ? R.mipmap.icon_audio_controller_play : R.mipmap.icon_audio_controller_play_grey);
        }
    }

    private void setButtonPauseImage() {
        if (mPauseButton != null) {
            mPauseButton.setImageResource(isEnable ? R.mipmap.icon_audio_controller_pause : R.mipmap.icon_audio_controller_pause_grey);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            if (mPlayer == null) {
                return;
            }
            switch (msg.what) {
                case FADE_OUT:
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };
}
