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
import android.widget.ImageView;
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
public class IjkMediaController extends LinearLayout implements IMediaController {

    private Context context;
    private static final int sDefaultTimeout = 3000;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    ImageButton mPauseButton;
    TextView mTime;
    SeekBar mProgress;
    ImageView ivFull;
    private MediaController.MediaPlayerControl mPlayer;
    StringBuilder mFormatBuilder;
    Formatter mFormatter;
    View rootView, mAnchor;
    boolean mShowing, mDragging;
    private boolean fullscreen = false;
    private boolean mInstantSeeking = false;
    private long mDuration;

    public VideoControllerCallback getCallback() {
        return callback;
    }

    public void setCallback(VideoControllerCallback callback) {
        this.callback = callback;
    }

    VideoControllerCallback callback;

    public IjkMediaController(Context context) {
        super(context);
        initView(context);
    }

    public IjkMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        rootView = makeControllerView();
        rootView.setVisibility(GONE);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(rootView, params);
    }


    private View makeControllerView() {
        View controller = LayoutInflater.from(context).inflate(R.layout.videoview_controller, null);
        mPauseButton = controller.findViewById(R.id.mediacontroller_play_pause);
        mTime = controller.findViewById(R.id.mediacontroller_time);
        mProgress = controller.findViewById(R.id.mediacontroller_seekbar);
        ivFull = controller.findViewById(R.id.iv_full);


        ivFull.setImageResource(R.mipmap.icon_video_controller_zoom);
        mProgress.setOnSeekBarChangeListener(mSeekListener);
        mPauseButton.setOnClickListener(mPauseListener);
        ivFull.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivFull != null) {
                    if (fullscreen) {
                        setSmallScreen();
                    } else {
                        setFullScreen();
                    }
                }

            }
        });
        return controller;
    }


    @Override
    public boolean isShowing() {
        return mShowing;
    }

    @Override
    public void setAnchorView(View view) {
        mAnchor = view;
        mProgress.setMax(1000);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {
        mPlayer = player;
    }

    @Override
    public void show(int timeout) {
        if (!mShowing && mAnchor != null) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            rootView.setVisibility(VISIBLE);
            mShowing = true;
        }
        updatePausePlay();
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            Message msg = mHandler.obtainMessage(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }

    private void updatePausePlay() {
        if (rootView == null || mPauseButton == null) return;

        if (mPlayer.isPlaying()) {
            setButtonPauseImage();
        } else {
            setButtonPlayImage();
        }
    }

    @Override
    public void show() {
        show(sDefaultTimeout);
    }

    @Override
    public void setProgressZero() {
        if (mPlayer != null) {
            mPlayer.seekTo(0);
        }
    }

    @Override
    public void hide() {
        if (mAnchor == null) return;

        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                rootView.setVisibility(GONE);
            } catch (IllegalArgumentException ex) {
            }
            mShowing = false;
        }
    }

    @Override
    public void showOnce(View view) {
    }

    private int setProgress() {
        if (mPlayer == null || mDragging) {
            return 0;
        }
        long position = mPlayer.getCurrentPosition();
        long duration = mPlayer.getDuration();
        long leftTime = duration - position;

        mDuration = duration;
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                if (leftTime < 1000) {
                    mProgress.setProgress(1000);
                } else {
                    mProgress.setProgress((int) pos);
                }
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (leftTime < 1000) {
            setTime(duration, duration);
        } else {
            setTime(position, duration);
        }

        return (int) position;
    }

    private final OnClickListener mPauseListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private void doPauseResume() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            if (callback != null) callback.onPaused();
            setButtonPlayImage();
            //danmakuView.pause();
        } else {
            mPlayer.start();
            if (callback != null) callback.onStarted();
            setButtonPauseImage();
            //danmakuView.resume();
        }
//        updatePausePlay();
    }

    private void setButtonPlayImage() {
        if (mPauseButton != null) {
            mPauseButton.setImageResource(R.mipmap.icon_video_controller_play);
        }
    }

    private void setButtonPauseImage() {
        if (mPauseButton != null) {
            mPauseButton.setImageResource(R.mipmap.icon_video_controller_pause);
        }
    }

    public void setFullScreen() {
        fullscreen = true;
        ivFull.setImageResource(R.mipmap.icon_video_controller_shrink);
    }

    public void setSmallScreen() {
        fullscreen = false;
        ivFull.setImageResource(R.mipmap.icon_video_controller_zoom);
    }

    public void changeScreen(int action) {
        // 缩小
        if (action == 0 && fullscreen) {
            setSmallScreen();
        }
        // 放大
//        else if (action == 1 && !fullscreen) {
//            setFullScreen();
//        }
    }

    private final SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

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
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }

            long duration = mPlayer.getDuration();
            long newposition = (duration * progress) / 1000L;
            //            mPlayer.seekTo((int) newposition);
            setTime(newposition, duration);
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mPlayer.seekTo((int) (mDuration * bar.getProgress()) / 1000);
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);
            mHandler.removeMessages(SHOW_PROGRESS);
            mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
        }
    };

    /**
     * 混音时要求滑动设置的点为20毫秒的离散点上，如果刚好不是，我们默认计算设置为最近的下一个20ms离散点
     *
     * @param position 当前滑动停止的位置
     * @return 计算出的最接近下一个离散点
     */
    private long convertOffsetTime(long position) {
        return (position + (20 - position % 20)) / 1000;
    }

    private void setTime(long position, long duration) {
        if (mTime != null) {
            mTime.setText(stringForTime((int) position) + "/" + stringForTime((int) duration));
        }
    }

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

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if (!mDragging && mShowing && mPlayer.isPlaying()) {
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    /**
     * 设置全屏按钮显示隐藏
     * @param visible
     */
    public void setIvFullVisible(boolean visible) {
        if (visible) {
            ivFull.setVisibility(VISIBLE);
        } else {
            ivFull.setVisibility(INVISIBLE);
        }
    }
}
