package com.zhongsou.souyue.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.zhongsou.souyue.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @description: 视频播放过程，容纳MediaPlayer 但是重点： 初始化页面时  不初始化MediaPlayer
 * MediaPlayer 实例化过程是在  用户点击播放的时候，并且保存MediaPlayer
 * 整个视图 是对外界的交互，有关的就是在 播放停止或者是 全屏缩放的时候与外界有关联，
 * 回调是VideoPlayCallbackImpl
 * @auther: qubian
 * @data: 2016/3/7.
 */
public class ZSVideoView extends RelativeLayout implements
        TextureView.SurfaceTextureListener {
    private final int TIME_SHOW_CONTROLLER = 5000;
    private final int TIME_UPDATE_PLAY_TIME = 1000;

    private final int MSG_HIDE_CONTROLLER = 10;
    private final int MSG_UPDATE_PLAY_TIME = 11;
    private ZSVideoMediaController.PageType mCurrPageType = ZSVideoMediaController.PageType.SHRINK;// 当前是横屏还是竖屏

    private Context mContext;
    private TextureView mSuperVideoView;
    private ZSVideoMediaController mMediaController;
    private Timer mUpdateTimer;
    private TimerTask mTimerTask;
    private VideoPlayCallbackImpl mVideoPlayCallback;

    private View mProgressBarView;
    private View mCloseBtnView;

    private String videourl;
    private Surface mSurface = null;
    private MediaPlayer mPlayer;
    private int progressSec = 0;
    /**
     * 处理时间 进度条，和 controller的隐藏和显示
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_UPDATE_PLAY_TIME) {
                updatePlayTime();
                updatePlayProgress();
            } else if (msg.what == MSG_HIDE_CONTROLLER) {
                showOrHideController();
            }
        }
    };


    /**
     * 视频上 有关闭按钮的事件
     */
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.video_close_view) {
                mVideoPlayCallback.onCloseVideo();
            }
        }
    };
    /**
     * view 上的 touch  事件 主要是处理 controller 的显示与隐藏
     */
    private View.OnTouchListener mOnTouchVideoListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                showOrHideController();
            }
            return mCurrPageType == ZSVideoMediaController.PageType.EXPAND ? true
                    : false;
        }
    };

    /**
     * 对 controller 事件的回调
     */
    private ZSVideoMediaController.MediaControlImpl mMediaControl = new ZSVideoMediaController.MediaControlImpl() {
        @Override
        public void alwaysShowController() {
            ZSVideoView.this.alwaysShowController();
        }

        @Override
        public void onPlayTurn() {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    pausePlay();
                } else {
                    startPlayVideo();
                }
            }

        }

        @Override
        public void onPageTurn() {
            mVideoPlayCallback.onSwitchPageType();
        }

        @Override
        public void onProgressTurn(ZSVideoMediaController.ProgressState state,
                                   int progress) {
            if (state.equals(ZSVideoMediaController.ProgressState.START)) {
                mHandler.removeMessages(MSG_HIDE_CONTROLLER);
            } else if (state.equals(ZSVideoMediaController.ProgressState.STOP)) {
                resetHideTimer();
            } else {
                if (mPlayer != null) {
                    int duration =0;
                    try {
                        duration =  mPlayer.getDuration();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    int time = progress * duration / 100;
                    mPlayer.seekTo(time);
                    updatePlayTime();
                }
            }
        }
    };
    private MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.i("OnInfoListener", mp.isPlaying() + ",what " + what
                    + ",extra " + extra);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    if (mProgressBarView.getVisibility() == View.VISIBLE) {
                        mProgressBarView.setVisibility(View.GONE);
                        mCloseBtnView.setVisibility(VISIBLE);
                    }
                    return true;
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    if (mProgressBarView.getVisibility() == View.GONE) {
                        mProgressBarView
                                .setBackgroundResource(android.R.color.transparent);
                        mProgressBarView.setVisibility(View.VISIBLE);
                        mCloseBtnView.setVisibility(VISIBLE);
                    }
                    return true;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    if (mProgressBarView.getVisibility() == View.VISIBLE) {
                        mProgressBarView.setVisibility(View.GONE);
                        mCloseBtnView.setVisibility(VISIBLE);
                    }
                    return true;
            }
            return false;
        }
    };
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.i("OnPreparedListener", "mp-" + mediaPlayer.isPlaying());
            mPlayer.start();
            resetHideTimer();
            resetUpdateTimer();
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            stopUpdateTimer();
            stopHideTimer();
            int duration =0;
            try {
                duration =  mPlayer.getDuration();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            mMediaController.playFinish(duration);
            mVideoPlayCallback.onPlayFinish();
            Log.e("mOnCompletionListener", "video OnCompletion");
        }
    };
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            progressSec = percent;

        }
    };
    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (getWindowToken() != null) {
                Resources r = getContext().getResources();
                int messageId;
                if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                    messageId = R.string.video_erro;
                } else {
                    messageId = R.string.video_erro;
                }
                Log.e("mErrorListener", r.getString(messageId));
                mVideoPlayCallback.onErrorCallBack();
            }
            return true;
        }
    };

    public TextureView getSuperVideoView() {
        return mSuperVideoView;
    }

    public void setPageType(ZSVideoMediaController.PageType pageType) {
        mMediaController.setPageType(pageType);
        mCurrPageType = pageType;
    }

    public void setVideoPlayCallback(VideoPlayCallbackImpl videoPlayCallback) {
        mVideoPlayCallback = videoPlayCallback;
    }

    public void pausePlay() {
        if(mPlayer!=null)
        {
            mPlayer.pause();
        }
        mMediaController.setPlayState(ZSVideoMediaController.PlayState.PAUSE);
//        stopHideTimer();
    }

    public void stopPlay() {
        pausePlay();
        stopUpdateTimer();
        mMediaController.setPlayState(ZSVideoMediaController.PlayState.STOP);
    }

    public void resume() {
        startPlayVideo();
    }

    public void close() {
        mMediaController.setPlayState(ZSVideoMediaController.PlayState.PAUSE);
        stopHideTimer();
        stopUpdateTimer();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        mSuperVideoView.setVisibility(GONE);
    }

    public ZSVideoView(Context context) {
        super(context);
        initView(context);
    }

    public ZSVideoView(Context context, AttributeSet attrs,
                       int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public ZSVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(Context context) {
        mContext = context;
        View.inflate(context, R.layout.super_video_view, this);
        mSuperVideoView = (TextureView) findViewById(R.id.video_view);
        mSuperVideoView.setScaleX(1.00001f);
        mMediaController = (ZSVideoMediaController) findViewById(R.id.videoController);
        mProgressBarView = findViewById(R.id.progressbar);
        mCloseBtnView = findViewById(R.id.video_close_view);

        mMediaController.setMediaControl(mMediaControl);
        mSuperVideoView.setOnTouchListener(mOnTouchVideoListener);

        mCloseBtnView.setVisibility(INVISIBLE);
        mCloseBtnView.setOnClickListener(mOnClickListener);
        mProgressBarView.setVisibility(VISIBLE);

        mSuperVideoView.setSurfaceTextureListener(this);
        this.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 拦截
                return true;
            }
        });
        mMediaController.setVisibility(GONE);
    }

    /***
     * 加载并开始播放视频
     *
     * @param videoUrl
     */
    public void loadAndPlay(MediaPlayer player, String videoUrl, int seekTime,
                            boolean isfull) {
        videourl = videoUrl;
        mProgressBarView.setVisibility(VISIBLE);
        mSuperVideoView.setVisibility(VISIBLE);
        if (seekTime == 0) {
            mProgressBarView.setBackgroundResource(android.R.color.black);
        } else {
            mProgressBarView.setBackgroundResource(android.R.color.transparent);
        }
        if (TextUtils.isEmpty(videoUrl)) {
            Log.e("TAG", "videoUrl should not be null");
            return;
        }
        mPlayer = player;
        if (isfull) {
            startPlayVideo();
            mProgressBarView.setVisibility(View.GONE);
        } else {
            play(videoUrl);
        }
        startPlayVideo(seekTime);
    }

    private void play(String url) {
        try {
            mPlayer.setOnCompletionListener(mOnCompletionListener);
            mPlayer.setOnPreparedListener(mOnPreparedListener);
            mPlayer.setOnInfoListener(mInfoListener);
            mPlayer.setOnErrorListener(mErrorListener);
            mPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mPlayer.setSurface(mSurface);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setScreenOnWhilePlaying(true);
            mPlayer.setDataSource(url);
            mPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放视频 should called after loadVideo()
     */
    private void startPlayVideo(int seekTime) {
        if (seekTime > 0) {
            mPlayer.seekTo(seekTime);
        }
        mMediaController.setPlayState(ZSVideoMediaController.PlayState.PLAY);
        requestLayout();
        invalidate();
    }

    /**
     * 播放视频 should called after loadVideo()
     */
    private void startPlayVideo() {
        if(mPlayer!=null)
        {
            try {
                mPlayer.start();
                resetHideTimer();
                resetUpdateTimer();
                mMediaController.setPlayState(ZSVideoMediaController.PlayState.PLAY);
                requestLayout();
                invalidate();
            }catch (Exception e)
            {
                ZSVideoViewHelp.release();
            }

        }

    }

    public int getCurrentPosition() {
        int position = 0;
        if (mPlayer != null) {
            try {
                position = mPlayer.getCurrentPosition();
            } catch (Exception e) {
                e.printStackTrace();
                mPlayer.release();
                mPlayer = null;
            }
        }
        return position;
    }

    public String getPlayStatus()
    {
        Log.d("getStatus","getPlayStatus");
        if(mMediaController.getStatus() == ZSVideoMediaController.PlayState.PLAY)
        {
            Log.d("getStatus","paly");
            return ZSVideoViewHelp.VIDEO_STATUS_PLAY;
        }else if(mMediaController.getStatus() == ZSVideoMediaController.PlayState.PAUSE)
        {
            Log.d("getStatus","pause");
            return ZSVideoViewHelp.VIDEO_STATUS_PAUSE;
        }else if(mMediaController.getStatus() == ZSVideoMediaController.PlayState.STOP)
        {
            return ZSVideoViewHelp.VIDEO_STATUS_STOP;
        }
        return ZSVideoViewHelp.VIDEO_STATUS_PAUSE;
    }

    public boolean isPlaying()
    {
        return  ZSVideoViewHelp.isPlaying();
    }
    private void updatePlayTime() {
        if (mPlayer == null) {
            return;
        }
        try {
            int allTime = mPlayer.getDuration();
            int playTime = mPlayer.getCurrentPosition();
            mMediaController.setPlayProgressTxt(playTime, allTime);
        } catch (Exception e) {
            e.printStackTrace();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void updatePlayProgress() {
        if (mPlayer == null) {
            return;
        }
        try {
            int allTime = mPlayer.getDuration();
            int playTime = mPlayer.getCurrentPosition();
            int progress = playTime * 100 / allTime;
            mMediaController.setProgressBar(progress, progressSec);
        } catch (Exception e) {
            e.printStackTrace();
            mPlayer.release();
            mPlayer = null;
        }
    }

    /***
     *
     */
    private void showOrHideController() {
        if (mMediaController.getVisibility() == View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_exit_from_bottom);
            animation.setAnimationListener(new AnimationImp() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    super.onAnimationEnd(animation);
                    mMediaController.setVisibility(View.GONE);
                }
            });
            mMediaController.startAnimation(animation);
        } else {
            mMediaController.setVisibility(View.VISIBLE);
            mMediaController.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    R.anim.anim_enter_from_bottom);
            mMediaController.startAnimation(animation);
            resetHideTimer();
        }
    }

    private void alwaysShowController() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mMediaController.setVisibility(View.VISIBLE);
    }

    private void resetHideTimer() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER,
                TIME_SHOW_CONTROLLER);
    }

    private void stopHideTimer() {
        mHandler.removeMessages(MSG_HIDE_CONTROLLER);
        mMediaController.setVisibility(View.VISIBLE);
        mMediaController.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(mContext,
                R.anim.anim_enter_from_bottom);
        mMediaController.startAnimation(animation);
    }

    private void resetUpdateTimer() {
        stopUpdateTimer();
        mUpdateTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                mHandler.sendEmptyMessage(MSG_UPDATE_PLAY_TIME);
            }
        };
        mUpdateTimer.schedule(mTimerTask, 0, TIME_UPDATE_PLAY_TIME);
    }

    private void stopUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    private class AnimationImp implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    public interface VideoPlayCallbackImpl {
        void onCloseVideo();

        void onSwitchPageType();

        void onPlayFinish();

        void onErrorCallBack();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                          int width, int height) {
        mSurface = new Surface(surfaceTexture);
        play(videourl);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}