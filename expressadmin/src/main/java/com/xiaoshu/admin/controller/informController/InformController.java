package com.xiaoshu.admin.controller.informController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoshu.admin.entity.AdminUser;
import com.xiaoshu.admin.entity.Inform;
import com.xiaoshu.admin.service.AdminUserService;
import com.xiaoshu.admin.service.InformService;
import com.xiaoshu.common.base.PageData;
import com.xiaoshu.common.config.MySysUser;
import com.xiaoshu.common.util.HttpRequestClient;
import com.xiaoshu.common.util.ResponseEntity;
import com.xiaoshu.common.util.WxTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/inform")
public class InformController {
    @Autowired
    InformService informService;
    @Autowired
    AdminUserService adminUserService;

    @RequestMapping("list")
    public String list() {
        return "admin/inform/informList";
    }

    @RequestMapping("/informListAll")
    @ResponseBody
    public PageData<Inform> list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                 @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {

        PageData<Inform> userPageData = new PageData<>();
        IPage<Inform> userPage = informService.page(new Page<>(page, limit), null);
        userPageData.setCount(userPage.getTotal());
        userPageData.setData(userPage.getRecords());
        return userPageData;
    }

    @RequestMapping("add")
    public String add() {
        return "admin/inform/informAdd";
    }


    // @PostMapping("/informAdd")
    @RequestMapping("/informAdd")
    @ResponseBody
    public ResponseEntity informAdd(Inform inform) {
        System.out.println(inform.toString());
        //发送对象全部
        if (inform.getNewsToCrowd() == 0) {
            QueryWrapper<AdminUser> qw = new QueryWrapper();
            //qw.ne("openid",null);
            qw.ne("openid", "");
            List<AdminUser> adminUserList = adminUserService.list(qw);
            //循环发送模板消息
            send(adminUserList, inform);
            //保存信息
            String name = MySysUser.nickName();
            inform.setIssuer(name);
            boolean b = informService.save(inform);
            if (b) {
                return ResponseEntity.success("操作成功");
            } else {
                return ResponseEntity.failure("操作失败");
            }
        }
        if (inform.getNewsToCrowd() == 1) {
            //发送对象快递员
            QueryWrapper<AdminUser> qw = new QueryWrapper();
            qw.eq("user_type", 1);
            qw.ne("openid", "");
            List<AdminUser> adminUserList = adminUserService.list(qw);
            //循环发送快递员
            send(adminUserList, inform);
            String name = MySysUser.nickName();
            inform.setIssuer(name);
            boolean b = informService.save(inform);
            if (b) {
                return ResponseEntity.success("操作成功");
            } else {
                return ResponseEntity.failure("操作失败");
            }
        }
        if (inform.getNewsToCrowd() == 2) {
            //发送对象普通用户
            QueryWrapper<AdminUser> qw = new QueryWrapper();
            qw.eq("user_type", 0);
            qw.ne("openid", "");
            List<AdminUser> adminUserList = adminUserService.list(qw);
            //循环发送快递员
            send(adminUserList, inform);
            String name = MySysUser.nickName();
            inform.setIssuer(name);
            boolean b = informService.save(inform);
            if (b) {
                return ResponseEntity.success("操作成功");
            } else {
                return ResponseEntity.failure("操作失败");
            }
        }
        //发送对象个人
        if ((inform.getNewsToCrowd() == 3) && (!inform.getUserAccount().equals(""))) {
            QueryWrapper qw = new QueryWrapper();
            qw.eq("account", inform.getUserAccount());
            AdminUser adminUser = adminUserService.getOne(qw);
            if (adminUser == null) {
                return ResponseEntity.failure("没有此用户");
            }
            if (adminUser.getOpenid() == null || adminUser.getOpenid().equals("")) {
                return ResponseEntity.failure("此用户没有绑定微信");
            }
            WxTokenUtil wxTokenUtil = new WxTokenUtil();
            String access_token = wxTokenUtil.getAccessToken();
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + access_token;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("touser", adminUser.getOpenid());   // openid
            jsonObject.put("template_id", "5AJOOlt6RXg9-5wM_AKvYBSHAaioB9tOimRvRa-tRH0");
            JSONObject first = new JSONObject();
            first.put("value", inform.getTitle());
            JSONObject keyword1 = new JSONObject();
            keyword1.put("value", "");
            JSONObject keyword2 = new JSONObject();
            keyword2.put("value", inform.getContent());
            JSONObject keyword3 = new JSONObject();
            keyword3.put("value", df.format(new Date()));//当前时间
            JSONObject remark = new JSONObject();
            remark.put("value", "感谢您的使用,我们将竭诚为您服务。如有疑问，请拨打客服热线");
            JSONObject data = new JSONObject();
            data.put("first", first);
            data.put("keyword1", keyword1);
            data.put("keyword2", keyword2);
            data.put("keyword3", keyword3);
            data.put("remark", remark);
            jsonObject.put("data", data);
            String res = HttpRequestClient.sendPost(url, jsonObject.toJSONString());
            JSONObject result = JSON.parseObject(res);
            int errcode = result.getIntValue("errcode");
            if (errcode == 0) {
                // 发送成功
                System.out.println("发送成功");
            } else {
                // 发送失败
                System.out.println("发送失败");
            }
            String name = MySysUser.nickName();
            inform.setIssuer(name);
            inform.setUserId(adminUser.getId());
            boolean b = informService.save(inform);
            if (b) {
                return ResponseEntity.success("操作成功");
            } else {
                return ResponseEntity.failure("操作失败");
            }
        }
        return ResponseEntity.failure("操作失败");
    }


    public void send(List<AdminUser> adminUserList, Inform inform) {
        WxTokenUtil wxTokenUtil = new WxTokenUtil();
        String access_token = wxTokenUtil.getAccessToken();
        System.out.println(access_token);
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + access_token;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        for (int i = 0; i < adminUserList.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("touser", adminUserList.get(i).getOpenid());   // openid
            jsonObject.put("template_id", "5AJOOlt6RXg9-5wM_AKvYBSHAaioB9tOimRvRa-tRH0");
            JSONObject first = new JSONObject();
            first.put("value", inform.getTitle());
            JSONObject keyword1 = new JSONObject();
            keyword1.put("value", "");
            JSONObject keyword2 = new JSONObject();
            keyword2.put("value", inform.getContent());
            JSONObject keyword3 = new JSONObject();
            keyword3.put("value", df.format(new Date()));//当前时间
            JSONObject remark = new JSONObject();
            remark.put("value", "感谢您的使用,我们将竭诚为您服务。如有疑问，请拨打客服热线");
            JSONObject data = new JSONObject();
            data.put("first", first);
            data.put("keyword1", keyword1);
            data.put("keyword2", keyword2);
            data.put("keyword3", keyword3);
            data.put("remark", remark);
            jsonObject.put("data", data);
            String res = HttpRequestClient.sendPost(url, jsonObject.toJSONString());
            JSONObject result = JSON.parseObject(res);
            int errcode = result.getIntValue("errcode");
            if (errcode == 0) {
                // 发送成功
                System.out.println("发送成功");
            } else {
                // 发送失败
                System.out.println("发送失败");
            }
        }
    }







/*    public String getAccessTokenFromWxServer(){

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
    }*/
}





