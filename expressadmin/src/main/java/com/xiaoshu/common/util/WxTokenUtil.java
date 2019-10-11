package com.xiaoshu.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.xiaoshu.admin.entity.TbSysConfig;
import com.xiaoshu.admin.service.TbSysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
public class WxTokenUtil {
    @Autowired
    private TbSysConfigService tbSysConfigService=(TbSysConfigService) SpringContextUtil.getApplicationContext().getBean("TbSysConfigServiceImpl");

    public String getAccessToken() {
        try {
            QueryWrapper qw = new QueryWrapper();
            qw.eq("config_code", "access_token");
            TbSysConfig accessTokenSys = tbSysConfigService.getOne(qw);

            String configValue = accessTokenSys.getConfigValue();
            if (StringUtils.isBlank(configValue)) {
                String accessToken = getAccessTokenFromWxServer();
                accessTokenSys.setConfigValue(accessToken);
                accessTokenSys.setUpdateTime(new Date());
                tbSysConfigService.updateById(accessTokenSys);
                return accessToken;
            }

            long time = accessTokenSys.getUpdateTime().getTime();
            long now = System.currentTimeMillis();
            if ((now - time) > 7000000) {
                String accessToken = getAccessTokenFromWxServer();
                accessTokenSys.setConfigValue(accessToken);
                accessTokenSys.setUpdateTime(new Date());
                tbSysConfigService.updateById(accessTokenSys);
                return accessToken;
            }

            return accessTokenSys.getConfigValue();
        } catch (Exception e) {
        }

        return "";
    }
    public String getJicket() {
        try {
            QueryWrapper qw = new QueryWrapper();
            qw.eq("config_code", "jsapi_ticket");
            TbSysConfig jsapi_ticket = tbSysConfigService.getOne(qw);

            String configValue = jsapi_ticket.getConfigValue();

            if (StringUtils.isBlank(configValue)) {
                String ticket = getAccessTokenJsapi();
                System.out.println("=====ticket====="+ticket);
                jsapi_ticket.setConfigValue(ticket);
                jsapi_ticket.setUpdateTime(new Date());
                tbSysConfigService.updateById(jsapi_ticket);
                return configValue;
            }

            long time = jsapi_ticket.getUpdateTime().getTime();
            long now = System.currentTimeMillis();
            if ((now - time) > 7000000) {
                String jsapi = getAccessTokenJsapi();
                jsapi_ticket.setConfigValue(jsapi);
                jsapi_ticket.setUpdateTime(new Date());
                tbSysConfigService.updateById(jsapi_ticket);
                return jsapi;
            }

            return jsapi_ticket.getConfigValue();
        } catch (Exception e) {
            return "";
        }
    }



    public String getAccessTokenFromWxServer() {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("config_code", "AppID");
        TbSysConfig appSys = tbSysConfigService.getOne(qw);
        String appid = appSys.getConfigValue();
        QueryWrapper qw1 = new QueryWrapper();
        qw1.eq("config_code", "AppSecret");
        TbSysConfig secretSys = tbSysConfigService.getOne(qw1);
        String appSecret = secretSys.getConfigValue();
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        Map paramsMap = new HashMap() {{
            put("appid", appid);
            put("secret", appSecret);
            put("grant_type", "client_credential");
        }};
        String params = HttpRequestClient.getUrlParamsByMap(paramsMap);
        String res = HttpRequestClient.sendGet(url, params);
        JSONObject resObj = JSON.parseObject(res);
        String accessToken = resObj.getString("access_token");
        return accessToken;
    }
    public String getAccessTokenJsapi(){
        String accessToken = getAccessToken();
        String url="https://api.weixin.qq.com/cgi-bin/ticket/getticket";
        String param="access_token="+accessToken+"&type=jsapi";
        String res= HttpRequestClient.sendGet(url,param);
        JSONObject resObj= JSON.parseObject(res);
        String Jsapi = resObj.getString("ticket");
        return Jsapi;
    }
    public static Map<String, String> sign(String url,String jsapi_ticket) {
        Map<String, String> ret = new HashMap<String, String>();
        //这里的jsapi_ticket是获取的jsapi_ticket。
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
        // System.out.println(string1);

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }



}
