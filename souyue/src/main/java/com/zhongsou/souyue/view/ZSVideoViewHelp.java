package com.zhongsou.souyue.view;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import com.sina.weibo.sdk.utils.LogUtil;

/**
 * @description: 负责拿到整个 播放过程中 MediaPlayer 的唯一单例
 * 用于全屏时的播放，控制其过程中 全屏在原有位置播放，以及 退出全屏后，继续播放位置播放
 * <p/>
 * 拓展：考虑，youtube 过程中的 窗口播放模式
 * 但是还是要保证其播放唯一
 * @auther: qubian
 * @data: 2016/3/16.
 */
public class ZSVideoViewHelp {
    public static final String REFRESH_VIDEO = "refresh_video"; // 刷新数据的广播
    public static final String VIDEO_STATUS = "status";
    public static final String VIDEO_POSITION = "palyPosition";
    public static final String VIDEO_STATUS_PLAY = "video_status_play"; //
    public static final String VIDEO_STATUS_PAUSE = "video_status_pause"; //
    public static final String VIDEO_STATUS_STOP = "video_status_stop"; //

    public static final String VIDEO_NET_ACTION = "net_status_action";
    public static final String VIDEO_NET_STATUS = "net_status";
    public static final String VIDEO_NET_STATUS_NO = "net_status_no";
    public static final String VIDEO_NET_STATUS_PHONE = "net_status_phone";
    public static final String VIDEO_NET_STATUS_wifi = "net_status_wifi";

    private static MediaPlayer mPlayer;

    public static MediaPlayer getInstance() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        return mPlayer;
    }

    /**
     * MediaPlayer resume
     */
    public static void resume() {
        try {
            if (mPlayer != null) {
                mPlayer.start();
            }
        } catch (Exception e) {
            LogUtil.i("ZSVideoViewHelp", "resume error");
        }

    }

    /**
     * MediaPlayer pause
     */
    public static void pause() {
        try {
            if (mPlayer != null) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
            }
        } catch (Exception e) {
            LogUtil.i("ZSVideoViewHelp", "pause error");
        }

    }

    /**
     * MediaPlayer release
     */
    public static void release() {
        try {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
        } catch (Exception e) {
            LogUtil.i("ZSVideoViewHelp", "release error");
        }

    }
    public static boolean isPlaying()
    {
        boolean isPlay =false;
        if (mPlayer != null)
        {
            synchronized (mPlayer)
            {
                try {
                    isPlay = mPlayer.isPlaying();
                }catch (Exception e)
                {
                    LogUtil.i("ZSVideoViewHelp", "isPlaying error");
                }
            }
        }
        return  isPlay;
    }
    public static void sendStopBroadcast(Context context) {
        Intent intent = new Intent();
        intent.setAction(ZSVideoViewHelp.REFRESH_VIDEO);
        intent.putExtra(ZSVideoViewHelp.VIDEO_STATUS, ZSVideoViewHelp.VIDEO_STATUS_STOP);
        context.sendBroadcast(intent);
    }
}
