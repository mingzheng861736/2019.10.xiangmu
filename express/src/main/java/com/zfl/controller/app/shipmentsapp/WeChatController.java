package com.zfl.controller.app.shipmentsapp;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zfl.entity.AdminUser;
import com.zfl.entity.TbSysConfig;
import com.zfl.jwt.JWTUtil;
import com.zfl.result.Result;
import com.zfl.service.AdminUserService;
import com.zfl.service.TbSysConfigService;
import com.zfl.util.WxUserTool;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sun.nio.ch.IOUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;

/**
 * 微信Controller
 * @date 2019.09.12
 * @author weishilei
 */
@RestController
@RequestMapping("/weChat")
public class WeChatController{

    @Autowired
    private TbSysConfigService tbSysConfigService;

    @Autowired
    private AdminUserService adminUserService;

    private String appId = "";

    private String appSecret = "";

    @PostMapping("/userOauth")
    public Result userOauth() {
        appId = tbSysConfigService.getConfigValue("AppID");
        return new Result(200,"",appId);
    }

    @PostMapping("/oauth")
    @ResponseBody
    public Result  oauth(HttpServletRequest request, HttpServletResponse response,@RequestParam String code) throws UnsupportedEncodingException {
        appSecret = tbSysConfigService.getConfigValue("AppSecret");
        //String code = request.getParameter("code");
        if(StringUtils.isEmpty(code)){
            return new Result(201,"用户未授权登陆","");
        }
        String openId = WxUserTool.getUserOpenidByCode(appId, appSecret, code);
        //把这个openId存起来

        if(StringUtils.isEmpty(openId)){
            return new Result(201,"授权失败","");
        }
        AdminUser user = adminUserService.getOne(new QueryWrapper<AdminUser>().eq("openid",openId));
        if(user==null){//如果使用openId找不到用户，则说明此用户未注册或者未授权登陆过
            //跳转到登陆注册界面
            return new Result(202,"",openId);
        }
        String pass = user.getPass();
        String token = JWTUtil.sign(user.getAccount(),pass);
        response.setHeader("token", token);
        return new Result(200,"",token);
    }

    private static String urlEncodeUTF8(String source) {
        String result = source;
        try {
            result = java.net.URLEncoder.encode(source, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }


}