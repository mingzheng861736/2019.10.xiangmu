package com.xiaoshu.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xiaoshu.admin.entity.TbSysConfig;
import com.xiaoshu.admin.service.TbSysConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class tokenUtil {
    @Autowired
    TbSysConfigService tbSysConfigService;
    public void getAccessTokenFromWxServer(){
        /*String url="https://api.weixin.qq.com/cgi-bin/token";
        Map paramsMap=new HashMap(){{
            put("appid",tbSysConfigService.get);
            put("secret",appSecret);
            put("grant_type","client_credential");
        }};
        String params= HttpRequestClient.getUrlParamsByMap(paramsMap);
        String res=HttpRequestClient.sendGet(url,params);
        JSONObject resObj= JSON.parseObject(res);
        if(resObj.containsKey("errcode")&&resObj.getInteger("errcode")!=0){
            System.out.println("获取accesstoken_失败,错误信息:"+resObj.getString("errmsg"));

        }else if(resObj.containsKey("access_token")){
            accessToken=resObj.getString("access_token");
            setAccessTokenToRedis();
        }*/
    }
    public String getAccessTokenFromDatabase(){
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long days = null;
            Date currentTime = dateFormat.parse(dateFormat.format(new Date()));//现在系统当前时间
            List<TbSysConfig> sysConfigList=tbSysConfigService.list(null);
           for (TbSysConfig sysConfig:sysConfigList){
               if (sysConfig.getConfigCode().equals("access_token")){

               }
           }

        } catch (Exception e) {

        }

        String url="https://api.weixin.qq.com/cgi-bin/token";
        Map paramsMap=new HashMap(){{
            put("appid","wx1b8fccd09b244d15");
            put("secret","d0b9a6b66c9e44dcfad845023495ecb1");
            put("grant_type","client_credential");
        }};
        String params= HttpRequestClient.getUrlParamsByMap(paramsMap);
        String res=HttpRequestClient.sendGet(url,params);
        JSONObject resObj= JSON.parseObject(res);
        String  accessToken=resObj.getString("access_token");
        return accessToken;
    }


    public static void main(String[] args) throws Exception {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long days = null;

        Date currentTime = dateFormat.parse(dateFormat.format(new Date()));//现在系统当前时间
        Date pastTime = dateFormat.parse("2019-10-08 11:13:12");//过去时间
        long diff = currentTime.getTime() - pastTime.getTime();
        days = diff / (1000 * 60 * 60 * 24);

    }

}
