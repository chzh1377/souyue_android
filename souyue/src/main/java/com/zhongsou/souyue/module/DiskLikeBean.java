package com.zhongsou.souyue.module;

import com.zhongsou.souyue.DontObfuscateInterface;

/**
 * Created by lvqiang on 15/11/13.
 */
public class DiskLikeBean implements DontObfuscateInterface{
    private String log;
    private String tag;

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
