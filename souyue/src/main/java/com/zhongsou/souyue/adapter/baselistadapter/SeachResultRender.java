package com.zhongsou.souyue.adapter.baselistadapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.ZSImageView;
import com.zhongsou.souyue.R;
import com.zhongsou.souyue.module.listmodule.DefaultItemBean;

/**
 * @description:  5.2 新增的 搜索结果 视图类型
 * @auther: qubian
 * @data: 2016/4/14.
 */
public class SeachResultRender extends ListTypeRender {

    private ZSImageView image;
    private TextView discriptionTv;
    private ImageView addIv;
    public SeachResultRender(Context context, int itemType, int bottomType, BaseListViewAdapter adapter) {
        super(context, itemType, bottomType, adapter);
    }


    @Override
    public View getConvertView() {
        mConvertView = View.inflate(mContext,
                R.layout.listitem_searchresult, null);
        image = (ZSImageView) mConvertView.findViewById(R.id.image);
        discriptionTv = findView(mConvertView, R.id.discription);
        addIv = findView(mConvertView, R.id.iv_add);
        addIv.setOnClickListener(this);
        return super.getConvertView();
    }

    @Override
    public void fitDatas(int position) {
        super.fitDatas(position);
        DefaultItemBean bean = (DefaultItemBean) mAdaper.getItem(position);
        showImage(image, (bean.getImage().size() > 0 ? bean.getImage().get(0) : ""), R.drawable.default_small, null);
        mTitleTv.setText(ListUtils.calcTitle(mContext, bean.getTitleIcon(), getTitle(bean)));
        image.setTag((bean.getImage().size() > 0 ? bean.getImage().get(0) : ""));
        discriptionTv.setText(bean.getDesc());
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        if(v.getId() == addIv.getId())
        {

        }

    }
}
