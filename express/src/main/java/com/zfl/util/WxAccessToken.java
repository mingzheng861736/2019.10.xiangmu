//package com.zfl.util;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.huzi.cache.redis.RedisService;
//import com.huzi.communicate.http.HttpRequestClient;
//import com.huzi.spring.SpringUtils;
//import com.zfl.redisservice.RedisService;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @program: vaccine
// * @description: 微信access_token获取
// * @author: huzi
// * @create: 2019-03-29 10:30
// **/
//public class WxAccessToken {
//    RedisService redisService=(RedisService) SpringUtils.getBean("RedisServiceImpl");
//    private String appId;
//    private String appSecret;
//    public String accessToken;
//    public WxAccessToken(String appId,String appSecret){
//        this.appId=appId;
//        this.appSecret=appSecret;
//        //尝试从缓存获取
//        accessToken=getAccessTokenFromRedis();
//        if(accessToken==null){
//            //从微信服务器获取
//            getAccessTokenFromWxServer();
//        }
//    }
//    public String getAccessTokenFromRedis(){
//        return ()redisService.get(appId);
//    }
//    public void getAccessTokenFromWxServer(){
//        String url="https://api.weixin.qq.com/cgi-bin/token";
//        Map paramsMap=new HashMap(){{
//            put("appid",appId);
//            put("secret",appSecret);
//            put("grant_type","client_credential");
//        }};
//        String params= HttpRequestClient.getUrlParamsByMap(paramsMap);
//        String res=HttpRequestClient.sendGet(url,params);
//        JSONObject resObj= JSON.parseObject(res);
//        if(resObj.containsKey("errcode")&&resObj.getInteger("errcode")!=0){
//            System.out.println("获取accesstoken_失败,错误信息:"+resObj.getString("errmsg"));
//
//        }else if(resObj.containsKey("access_token")){
//            accessToken=resObj.getString("access_token");
//            setAccessTokenToRedis();
//        }
//    }
//    //将access_token设置到redis缓存
//    public void setAccessTokenToRedis(){
//        redisService.setex(appId,accessToken,7000);
//    }
//}
