package com.zhongsou.souyue.platform;
/**
 * Project Name: souyue-platform-4.0.1
 * @version  4.0.1
 */
  

import com.zhongsou.souyue.R;

/** 
 * Description: 布局文件API<br/> 
 * Company:   ZhongSou.com<br/> 
 * Copyright: 2003-2014 ZhongSou All right reserved<br/> 
 * @date      2014-7-31 上午11:16:27
 * @author    liudl
 */
public class LayoutApi {
    public static final String TRADE_LAYOUT_NAME_PRE = "trade_";
    
    /**  
     * 获取布局文件资源ID
     * 超级APP命名规则: trade_{souyue_layout_name}<br/>  
     * @author liudl
     * @date   2014-7-31 上午11:22:49
     * @param resourceId
     * @return  
     */
    public static int getLayoutResourceId(int resourceId){
        if(ConfigApi.isSouyue()){
            return resourceId;
        }else{
            String vName = TRADE_LAYOUT_NAME_PRE + ConfigApi.getResourceName(resourceId);
            return ConfigApi.getLayoutResourceId(vName);
        }
    }
    
    /**  
     * 获取图片资源ID
     * 超级APP命名规则: trade_{souyue_drawble_name}<br/>  
     * @author liudl
     * @date   2014-11-26 上午17:01:49
     * @param resourceId
     * @return  
     */
    public static int getDrawbleResourceId(int resourceId){
        if(ConfigApi.isSouyue()){
            return resourceId;
        }else{
            String vName = TRADE_LAYOUT_NAME_PRE + ConfigApi.getResourceName(resourceId);
            return ConfigApi.getDrawableResourceId(vName);
        }
    }
    
    /**  
     * 获取分享位置信息的logo. <br/>  
     *  
     * @author liudl
     * @date   2014-8-5 上午10:17:40
     * @return  
     */
    public static int getLocationShareLogo(){
        if(ConfigApi.isSouyue()){
            return R.drawable.ent_map_logo;
        }else{
            return R.drawable.logo;
        }
    }
}
  
	