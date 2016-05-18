package com.zhongsou.souyue.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.zhongsou.souyue.R;
import com.zhongsou.souyue.bases.BaseActivity;
import com.zhongsou.souyue.receiver.HomeListener;
import com.zhongsou.souyue.receiver.ScreenListener;
import com.zhongsou.souyue.ui.NetChangeDialog;
import com.zhongsou.souyue.utils.NetWorkUtils;
import com.zhongsou.souyue.view.ZSVideoMediaController;
import com.zhongsou.souyue.view.ZSVideoView;
import com.zhongsou.souyue.view.ZSVideoViewHelp;

/**
 * @description: 全屏
 * @auther: qubian
 * @data: 2016/3/14.
 */
public class VideoFullScreenActivity extends BaseActivity implements View.OnClickListener{
    private ZSVideoView videoView;
    private ImageView controllerIv;
    private String url;
    private int palyPosition;
    private boolean isPlaying;
    private String status;
    private ImageView imageIv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullvideo);
        videoView = (ZSVideoView) findViewById(R.id.videoView);
        imageIv= (ImageView) findViewById(R.id.image);
        imageIv.setVisibility(View.GONE);
        controllerIv = findView(R.id.controller);
        controllerIv.setOnClickListener(this);
        url = getIntent().getExtras().getString("videoUrl");
        palyPosition = getIntent().getExtras().getInt("position");
        status= getIntent().getExtras().getString("status");

        if(status.equalsIgnoreCase(ZSVideoViewHelp.VIDEO_STATUS_PLAY))
        {
            isPlaying= true;
            controllerIv.setVisibility(View.GONE);
            loadVideoView();
        }
        else if(status.equalsIgnoreCase(ZSVideoViewHelp.VIDEO_STATUS_PAUSE))
        {
            isPlaying= true;
            controllerIv.setVisibility(View.GONE);
            loadVideoView();
//            isPlaying = false;
//            controllerIv.setVisibility(View.VISIBLE);
//            loadVideoView();
//            videoView.pausePlay();
//            imageIv.setVisibility(View.VISIBLE);
//            imageIv.setImageBitmap(videoView.getDrawingCache());
        }
        else
        {
            controllerIv.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
        }
        setUpdateReciever();
        setHomeListener();
        setScreenListener();
    }

    private void loadVideoView() {

        if(isPlaying)
        {
            videoView.setVisibility(View.VISIBLE);
            videoView.loadAndPlay(ZSVideoViewHelp.getInstance(), url, palyPosition, true);
            videoView.setPageType(ZSVideoMediaController.PageType.EXPAND);
            videoView.setVideoPlayCallback(new ZSVideoView.VideoPlayCallbackImpl() {

                @Override
                public void onSwitchPageType() {
                    if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        sendVideoBroadcast(videoView.getPlayStatus(),videoView.getCurrentPosition());
                        finish();
                    }
                }

                @Override
                public void onPlayFinish() {
                    sendVideoBroadcast(ZSVideoViewHelp.VIDEO_STATUS_STOP,videoView.getCurrentPosition());
                    isPlaying= false;
                    finish();
                }

                @Override
                public void onErrorCallBack() {
                    if(!NetWorkUtils.isNetworkAvailable())
                    {
                        Toast.makeText(mContext, R.string.networkerror, Toast.LENGTH_SHORT).show();
                        ZSVideoViewHelp.getInstance().release();
                        isPlaying= false;
                        finish();
                    }
                }

                @Override
                public void onCloseVideo() {
                    sendVideoBroadcast(ZSVideoViewHelp.VIDEO_STATUS_STOP,videoView.getCurrentPosition());
                    isPlaying= false;
                    finish();
                }
            });
        }else
        {
            videoView.setVisibility(View.GONE);
            ZSVideoViewHelp.pause();
        }

    }
    private void sendVideoBroadcast(String status,int palyPosition)
    {
        Intent intent = new Intent();
        intent.setAction(ZSVideoViewHelp.REFRESH_VIDEO);
        intent.putExtra(ZSVideoViewHelp.VIDEO_STATUS, status);
        intent.putExtra(ZSVideoViewHelp.VIDEO_POSITION, palyPosition);
        sendBroadcast(intent);
    }
    @Override
    public void finish() {

        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlaying)
            ZSVideoViewHelp.resume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode ==KeyEvent.KEYCODE_BACK)
        sendVideoBroadcast(videoView.getPlayStatus(), videoView.getCurrentPosition());
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying)
        ZSVideoViewHelp.pause();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.controller:
                isPlaying=true;
                controllerIv.setVisibility(View.GONE);
                loadVideoView();
                break;
        }
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
//        ZSVideoViewHelp.release();
        cancelReciever();
        cancelHomeListener();
        cancelScreenListener();

    }
    private void stopVideo()
    {
        ZSVideoViewHelp.sendStopBroadcast(mContext);
        finish();
    }
    VideoUpdateBroadCastRecever receiver;

    /**
     * 注册用于刷新 视频数据的广播
     *
     */
    public void setUpdateReciever() {
        if(receiver == null)
        {
            IntentFilter inf = new IntentFilter();
            inf.addAction(ZSVideoViewHelp.REFRESH_VIDEO);
            inf.addAction(ZSVideoViewHelp.VIDEO_NET_ACTION);
            inf.setPriority(111110);
            receiver = new VideoUpdateBroadCastRecever();
            registerReceiver(receiver, inf);
        }

    }
    private void cancelReciever()
    {
        try
        {
            if(receiver!=null)
            {
                unregisterReceiver(receiver);
                receiver=null;
            }
        }catch (Exception e)
        {

        }

    }
    public class VideoUpdateBroadCastRecever extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ZSVideoViewHelp.REFRESH_VIDEO))
            {
                dealWithBroaCast(intent);
            }
            if(action.equals(ZSVideoViewHelp.VIDEO_NET_ACTION))
            {
                dealWithNetBroaCast(intent);
                abortBroadcast();
            }
        }
    }
    public void dealWithBroaCast(Intent intent)
    {
        String status =intent.getStringExtra(ZSVideoViewHelp.VIDEO_STATUS);
        int palyPosition =intent.getIntExtra(ZSVideoViewHelp.VIDEO_POSITION,0);
         if(status.equals(ZSVideoViewHelp.VIDEO_STATUS_STOP))
        {
            finish();
        }
    }
    private void dealWithNetBroaCast(Intent intent) {
        String status =intent.getStringExtra(ZSVideoViewHelp.VIDEO_NET_STATUS);
        if(status.equalsIgnoreCase(ZSVideoViewHelp.VIDEO_NET_STATUS_PHONE))
        {
            showNetChangeDialog();
        }else if(status.equalsIgnoreCase(ZSVideoViewHelp.VIDEO_NET_STATUS_NO))
        {
            finish();
        }
    }
    public void showNetChangeDialog()
    {
        if(ZSVideoViewHelp.isPlaying())
        {
            videoView.pausePlay();
            final NetChangeDialog dialog = NetChangeDialog.getInstance(this, new NetChangeDialog.NetClickListener() {
                @Override
                public void continuePlay()
                {
                    videoView.resume();
                }
                @Override
                public void canclePlay() {
                    finish();
                }
            });
            dialog.show();
        }
    }
    private HomeListener mHomeWatcher;

    public void setHomeListener()
    {
        if(mHomeWatcher==null)
        {
            mHomeWatcher = new HomeListener(mContext);
            mHomeWatcher.setOnHomePressedListener(new HomeListener.OnHomePressedListener() {
                @Override
                public void onHomePressed() {
                    stopVideo();
                }

                @Override
                public void onHomeLongPressed() {
                    stopVideo();
                }
            });
            mHomeWatcher.startWatch();
        }

    }
    public void cancelHomeListener()
    {
        if(mHomeWatcher!=null)
        {
            mHomeWatcher.stopWatch();
            mHomeWatcher= null;
        }
    }

    private ScreenListener screenListener;
    public void setScreenListener()
    {
        if(screenListener==null)
        {
            screenListener = new ScreenListener(mContext);
            screenListener.setScreenStateListener(new ScreenListener.ScreenStateListener() {
                @Override
                public void onScreenOn() {
                    stopVideo();
                }

                @Override
                public void onScreenOff() {
                    stopVideo();
                }

                @Override
                public void onUserPresent() {
                    stopVideo();
                }
            });
            screenListener.startWatch();
        }

    }
    public void cancelScreenListener()
    {
        if(screenListener!=null)
        {
            screenListener.stopWatch();
            screenListener= null;
        }
    }


}
