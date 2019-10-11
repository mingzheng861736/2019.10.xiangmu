package com.zfl.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: vaccine
 * @description: 微信用户工具
 * @author huzi
 * @create: 2019-04-02 19:54
 **/
public class WxUserTool {
    public static JSONObject getWxUserByOpenid(String openid,String accessToken){
        String url="https://api.weixin.qq.com/cgi-bin/user/info";
        String params="access_token="+accessToken+"&openid="+openid+"&lang=zh_CN";
        String res= HttpRequestClient.sendGet(url,params);
        return JSON.parseObject(res);
    }
    /* @Description: 向用户发送模板消息
     *
     * @param accessToken
     * @param dataJsonStr JSON格式化map
     * @return com.alibaba.fastjson.JSONObject
     * @Author:huzi
     * @Date: 2019/4/2 20:45
    */
    public static  JSONObject sendTemplateMsg(String accessToken,String dataJsonStr){
        String url="https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="+accessToken;
        String res=HttpRequestClient.sendPost(url,dataJsonStr);
        return JSON.parseObject(res);
    }
    public static String getUserOpenidByCode(String appid,String appSecret,String code){
        String url="https://api.weixin.qq.com/sns/oauth2/access_token";
        //去请求用户openid
        Map paramsMap=new HashMap();
        paramsMap.put("appid",appid);
        paramsMap.put("secret",appSecret);
        paramsMap.put("code",code);
        paramsMap.put("grant_type","authorization_code");
        String params=HttpRequestClient.getUrlParamsByMap(paramsMap);
        String res=HttpRequestClient.sendGet(url,params);
        JSONObject resObj= JSON.parseObject(res);
        if(resObj.containsKey("errcode")&&resObj.getInteger("errcode")!=0){
            System.out.println("用户信息失败,错误信息:"+resObj.getString("errmsg"));
        }else if(resObj.containsKey("openid")){
           return resObj.getString("openid");
        }
        return null;
    }
}
