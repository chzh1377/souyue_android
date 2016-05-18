package com.zhongsou.souyue.adapter.baselistadapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.ZSImageView;
import com.zhongsou.souyue.R;
import com.zhongsou.souyue.circle.util.CircleUtils;
import com.zhongsou.souyue.countUtils.UpEventAgent;
import com.zhongsou.souyue.module.listmodule.SigleBigImgBean;
import com.zhongsou.souyue.net.volley.CMainHttp;
import com.zhongsou.souyue.ui.AccountConflicDialog;
import com.zhongsou.souyue.ui.NetChangeDialog;
import com.zhongsou.souyue.ui.SouYueToast;
import com.zhongsou.souyue.utils.DeviceUtil;
import com.zhongsou.souyue.utils.IntentUtil;
import com.zhongsou.souyue.utils.NetWorkUtils;
import com.zhongsou.souyue.utils.StringUtils;
import com.zhongsou.souyue.utils.ToastUtil;
import com.zhongsou.souyue.view.HotConfigView;
import com.zhongsou.souyue.view.ZSVideoView;
import com.zhongsou.souyue.view.ZSVideoViewHelp;

/**
 * @description: 视频样式 5.2
 * @auther: qubian
 * @data: 2015/12/23.
 */

public class VideoRender extends ListTypeRender {
    private static final int STOP_PLAY_POSITION =-1;
    private ZSVideoView mVideoView;
    private ZSImageView image;
    private HotConfigView hotConfigView;
    private ImageView controllerIv;
    private String videoUrl;
    private int indexPostion = STOP_PLAY_POSITION;
    private boolean isPlaying;
    private TextView durationTv;
    private SigleBigImgBean bean;
    private RelativeLayout videolayout;
    private int width;
    private int deviceWidth;

    public VideoRender(Context context, int itemType, int bottomType, BaseListViewAdapter adapter) {
        super(context, itemType, bottomType, adapter);
        deviceWidth = CircleUtils.getDeviceWidth(context);
        width = deviceWidth - DeviceUtil.dip2px(context, 20) ;

    }

    @Override
    public View getConvertView() {
        mConvertView = View.inflate(mContext, R.layout.listitem_video, null);
        mVideoView = findView(mConvertView, R.id.videoView);
        image = findView(mConvertView, R.id.image);
        durationTv = findView(mConvertView, R.id.durationTv);
        hotConfigView = findView(mConvertView, R.id.hotconfigView);
        controllerIv = findView(mConvertView, R.id.controller);
        videolayout = findView(mConvertView, R.id.videolayout);
        controllerIv.setOnClickListener(this);
        image.setOnClickListener(this);
        if(mListManager instanceof HomeListManager)
        {

            ((HomeListManager) mListManager).sendCancleAll();
            ((HomeListManager) mListManager).setUpdateReciever();
            ((HomeListManager) mListManager).setHomeListener();
            ((HomeListManager) mListManager).setScreenListener();
        }
        return super.getConvertView();
    }
    private void setViewLayout(View v , int width, double aspectRatio)
    {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.width = width;
        params.height = (int) (width/aspectRatio);
        v.setLayoutParams(params);
    }


    @Override
    public void fitDatas(int position) {
        super.fitDatas(position);
        setViewLayout(videolayout, width, 1.7);
        setViewLayout(image, width, 1.7);
        setViewLayout(mVideoView, width, 1.7);
        mTitleTv.setOnClickListener(this);
        findView(mConvertView, R.id.bottomView).setOnClickListener(this);
        bean = (SigleBigImgBean) mAdaper.getItem(position);
        showImageForce(image, bean.getBigImgUrl(), R.drawable.default_gif, null);
        durationTv.setText(bean.getDuration());
        videoUrl = bean.getPhoneImageUrl();
        hotConfigView.setData(bean.getTitleIcon());
        mTitleTv.setText(ListUtils.calcTitle(mContext, bean.getTitleIcon(), getTitle(bean)));
        controllerIv.setTag(position);
        if (mListManager instanceof IItemInvokeVideo) {
            indexPostion=((IItemInvokeVideo) mListManager).getPlayPosition();
        }
        setViewGone(false);
        if (indexPostion == position) {
            mVideoView.setVisibility(View.VISIBLE);
            setViewGone(true);
        } else {
            mVideoView.setVisibility(View.GONE);
            mVideoView.close();
            setViewGone(false);
        }
    }

    public void stopPlay()
    {
        ZSVideoViewHelp.release();
        setViewGone(false);
        indexPostion=STOP_PLAY_POSITION;
        isPlaying =false;
        if (mListManager instanceof IItemInvokeVideo) {
            ((IItemInvokeVideo) mListManager).setPlayPosition(indexPostion);
            ((IItemInvokeVideo) mListManager).setIsPalying(false);
        }
    }
    public void pausePlay()
    {
        controllerIv.setVisibility(View.GONE);
        mVideoView.pausePlay();
    }
    public void startPlay()
    {
        controllerIv.setVisibility(View.GONE);
        mVideoView.resume();
    }

    private void setViewGone(boolean isPlay) {
        controllerIv.setVisibility(isPlay ? View.GONE : View.VISIBLE);
        image.setVisibility(isPlay ? View.GONE : View.VISIBLE);
        mVideoView.setVisibility(isPlay ? View.VISIBLE : View.GONE);
        durationTv.setVisibility(isPlay ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId())
        {
            case R.id.image:
            case R.id.controller:
                if (CMainHttp.getInstance().isNetworkAvailable(mContext)) {
                    UpEventAgent.onZSVideoEvent(mContext,UpEventAgent.video_list_play,bean.getInvoke().getSrpId());
                    startToPlay();
                }
                break;
            case R.id.title:
            case R.id.bottomView:
                if(mListManager instanceof HomeListManager)
                {
                    mListManager.clickItem(bean);
                }
                break;
        }

    }
    public void startToPlay()
    {
        if (StringUtils.isNotEmpty(videoUrl)) {
            ZSVideoViewHelp.release();
            controllerIv.setVisibility(View.GONE);
            image.setVisibility(View.GONE);
            durationTv.setVisibility(View.GONE);
            indexPostion = (Integer) controllerIv.getTag();
            if (mListManager instanceof IItemInvokeVideo) {
                ((IItemInvokeVideo) mListManager).stopPlay(indexPostion);
                ((IItemInvokeVideo) mListManager).setPlayRender(this);
                ((IItemInvokeVideo) mListManager).setPlayPosition(indexPostion);
                ((IItemInvokeVideo) mListManager).setIsPalying(true);
            }
            isPlaying = true;
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.loadAndPlay(ZSVideoViewHelp.getInstance(), videoUrl, 0, false);
            mVideoView.setVideoPlayCallback(new ZSVideoViewPlayCallback(controllerIv,image, mVideoView));
        }
    }

    class ZSVideoViewPlayCallback implements ZSVideoView.VideoPlayCallbackImpl {
        ImageView mPlayBtnView;
        ZSVideoView mSuperVideoPlayer;
        ZSImageView imageView;
        public ZSVideoViewPlayCallback(ImageView mPlayBtnView,ZSImageView imageView, ZSVideoView mSuperVideoPlayer) {
            this.mPlayBtnView = mPlayBtnView;
            this.mSuperVideoPlayer = mSuperVideoPlayer;
            this.imageView= imageView;
        }

        @Override
        public void onCloseVideo() {
            closeVideo();
        }

        @Override
        public void onSwitchPageType() {
            if (((Activity) mContext).getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                IntentUtil.gotoFullScreen(mContext, videoUrl,mSuperVideoPlayer.getCurrentPosition(),mSuperVideoPlayer.getPlayStatus());
//                ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//                videolayout.setLayoutParams(lp);
                if(mListManager instanceof  HomeListManager)
                {
                    ((HomeListManager) mListManager).setIsExpand(true);
                }

            }
        }

        @Override
        public void onPlayFinish() {
            closeVideo();
        }

        @Override
        public void onErrorCallBack() {
            if(!NetWorkUtils.isNetworkAvailable())
            {
                Toast.makeText(mContext, R.string.networkerror, Toast.LENGTH_SHORT).show();
                if(indexPostion!=STOP_PLAY_POSITION)
                {
                    stopPlay();
                }
            }

        }

        private void closeVideo() {
//            isPlaying = false;
//            indexPostion = STOP_PLAY_POSITION;
//            mSuperVideoPlayer.close();
//            ZSVideoViewHelp.release();
//            mPlayBtnView.setVisibility(View.VISIBLE);
//            mSuperVideoPlayer.setVisibility(View.GONE);
//            imageView.setVisibility(View.VISIBLE);
            stopPlay();
        }

    }
    public int getCurrentPosition() {
        int position = 0;
        position = mVideoView.getCurrentPosition();
        return position;
    }


}
