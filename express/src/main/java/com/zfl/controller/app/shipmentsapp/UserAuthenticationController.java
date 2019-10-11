package com.zfl.controller.app.shipmentsapp;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zfl.entity.AdminUser;
import com.zfl.entity.TbUserInfo;
import com.zfl.entity.vo.UserAuthenticationVo;
import com.zfl.jwt.UserJwtService;
import com.zfl.result.Result;
import com.zfl.service.AdminUserService;
import com.zfl.service.TbUserInfoService;
import com.zfl.service.payservice.AliPayService;
//import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author sunzhenpeng
 * @data 2019/9/21
 * @description 用户认证 控制器
 */
@RestController
@RequestMapping("/userAuthentication")
public class UserAuthenticationController {

    @Autowired
    private UserJwtService userJwtService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private TbUserInfoService tbUserInfoService;

    /**
     *认证申请
     * @author sunzhenpeng
     * @date 2019/10/6
     * @param request
     * @param userAuthenticationVo
     * @return com.zfl.result.Result
     */
    @PostMapping("/authentication")
    //@RequiresAuthentication
    public Result authentication(HttpServletRequest request, @RequestBody UserAuthenticationVo userAuthenticationVo){
        AdminUser user = userJwtService.getUser(request);
        JSONObject jsonObject = new JSONObject();
        //查询userInfo表中对应此会员的数据
        TbUserInfo tbUserInfo = tbUserInfoService.getOne(new QueryWrapper<TbUserInfo>().eq("user_id", user.getId()));
        if(tbUserInfo.getIsCertification()==0){
            return new Result(201,"你已经认证过了,无需二次认证！","");
        }
        jsonObject.put("userName",userAuthenticationVo.getUserName());
        jsonObject.put("userCard",userAuthenticationVo.getUserCard());
        tbUserInfo.setUserInfoJson(jsonObject.toString());
        tbUserInfo.setCardNumImage(userAuthenticationVo.getCardNumImage());
        tbUserInfo.setIsCertification(2);//2为请求认证
        boolean up = tbUserInfoService.updateById(tbUserInfo);
        if(!up){
            return new Result(201,"认证提交失败！","");
        }
        return new Result(200,"认证提交成功！","");
    }

    /**
     *返回用户认证信息
     * @author sunzhenpeng
     * @date 2019/10/6
     * @param request
     * @return com.zfl.result.Result
     */
    @PostMapping("/backAuthenticationInfo")
    public Result backAuthenticationInfo(HttpServletRequest request){
        AdminUser user = userJwtService.getUser(request);
        //查询userInfo表中对应此会员的数据
        TbUserInfo tbUserInfo = tbUserInfoService.getOne(new QueryWrapper<TbUserInfo>().eq("user_id", user.getId()));
        if(tbUserInfo==null){
            return new Result(201,"查询错误！","");
        }
        UserAuthenticationVo userAuthenticationVo = new UserAuthenticationVo();
        JSONObject userInfo = JSONObject.parseObject(tbUserInfo.getUserInfoJson());
        userAuthenticationVo.setUserName(userInfo.getString("userName"));//姓名
        userAuthenticationVo.setUserCard(userInfo.getString("userCard"));//身份证号
        userAuthenticationVo.setUserPhone(tbUserInfo.getUserTel());//电话
        userAuthenticationVo.setCardNumImage(tbUserInfo.getCardNumImage());//证件照
        return new Result(200,"查询成功！",userAuthenticationVo);
    }

}
