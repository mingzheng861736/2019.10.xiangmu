package com.zfl.controller.app.shipmentsapp;
import com.alibaba.druid.wall.violation.ErrorCode;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zfl.entity.AdminUser;
import com.zfl.entity.TbFundInfo;
import com.zfl.entity.TbOddNum;
import com.zfl.entity.TbOrder;
import com.zfl.jwt.UserJwtService;
import com.zfl.service.AdminUserService;
import com.zfl.service.TbFundInfoService;
import com.zfl.service.TbOddNumService;
import com.zfl.service.TbOrderService;
import com.zfl.service.payservice.AliPayService;
import com.zfl.service.payservice.AlipayConfig;
//import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author sunzhenpeng
 * @data 2019/9/27
 * @description 支付，控制器
 */
@Controller
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private AliPayService aliPayService;
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TbOddNumService tbOddNumService;

    @Autowired
    private TbOrderService tbOrderService;

    @Autowired
    private UserJwtService userJwtService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private TbFundInfoService tbFundInfoService;


    public String aliNotify( HttpServletResponse response)throws AlipayApiException, IOException {

       return "";
    }
    @ResponseBody
    @RequestMapping(value = "/callback", produces = "text/plain;charset=utf-8")
    public String aliPayNotify(HttpServletRequest req) throws Exception {
        Map<String, String[]> requestParams = req.getParameterMap();
        System.out.println("支付宝支付结果通知" + requestParams.toString());
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();

        for (Iterator<?> iter = requestParams.keySet().iterator(); iter
                .hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        // 获取到返回的所有参数 先判断是否交易成功trade_status 再做签名校验
        // 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
        // 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
        // 3、校验通知中的seller_id（或者seller_email)
        // 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
        // 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
        try {

            // 订单金额
            String total_amount = params.get("total_amount");
            // 用户支付金额
            String amount = params.get("buyer_pay_amount");
            // 商户订单号
            String out_trade_no = params.get("out_trade_no");
            // 支付宝交易号
            String trade_no = params.get("trade_no");
            // 买家支付宝账号
            String buyer_logon_id = params.get("buyer_logon_id");
            // 交易状态
            String trade_status = params.get("trade_status");
            // 商品描述
            String body = params.get("body");
            // 交易创建时间
            String gmt_create = params.get("gmt_create");
            // 交易付款时间
            String gmt_payment = params.get("gmt_payment");

           /* //交易查询接口,可以与回调接口得到的参数作比较，看是否一致。
            AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, AlipayConfig.APPID, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();//创建API对应的request类
            request.setBizContent("{" +
                    " \"out_trade_no\":\""+out_trade_no+"\"," +
                    " \"trade_no\":\""+trade_no+"\"" +
                    " }");//设置业务参数
            AlipayTradeQueryResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
            System.out.print(response.getBody());
            //根据response中的结果继续业务逻辑处理
            JSONObject payParamsa  = JSONObject.parseObject(response.getBody());
            String trade_status1 = payParamsa.getString("trade_status");// 交易状态*/

            // 验证签名
            boolean flag = AlipaySignature.rsaCheckV1(params, AlipayConfig.ALIPAY_PUBLIC_KEY, AlipayConfig.CHARSET, AlipayConfig.SIGNTYPE);
            if (flag) {
                if ("TRADE_SUCCESS".equals(params.get("trade_status"))/*
                        && trade_status1.equals(params.get("trade_status"))*/) {
                    System.out.println("====================交易成功=========================");
                    String appId = params.get("app_id");
                    if (!appId.equals(AlipayConfig.APPID)) {
                        return "failure";
                    }
                    String ss = out_trade_no.substring(0,5);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    if(out_trade_no.substring(0,5).equals("KDDD-")){//此为快递下单的支付订单
                        //利用商家系统单号，查询商家订单
                        TbOddNum tbOddNum = tbOddNumService.getOne(new QueryWrapper<TbOddNum>().eq("sys_num",out_trade_no));
                        //修改订单状态
                        tbOddNum.setOrderState(1)//订单状态
                                .setTripartiteOrderNum(trade_no);//支付宝交易号;
                        boolean tb = tbOddNumService.updateById(tbOddNum);
                        if(tb){
                            return "success";
                        }
                    }else if(out_trade_no.substring(0,5).equals("JFSC-")){//此为积分商城的兑换支付订单
                        TbOrder tbOrder = tbOrderService.getById(out_trade_no);
                        //修改订单状态
                        tbOrder.setOrderStatus(1);//支付状态
                        tbOrder.setPayAmount(Double.valueOf(amount));//支付金额
                        tbOrder.setPayTime(sdf.parse(gmt_payment));//支付时间
                        tbOrder.setPayIntegral(tbOrder.getGoodsIntegralTotal());//支付积分
                        boolean ordSave = tbOrderService.updateById(tbOrder);//更新订单
                        //首先获取当前登录用户
                        AdminUser adminUser = userJwtService.getUser(request);
                        //扣除用户积分余额，产生收入支出明细记录
                        adminUser.setRemainingSum(adminUser.getRemainingSum()-tbOrder.getGoodsIntegralTotal());
                        boolean userSave = adminUserService.updateById(adminUser);
                        //生成收支明细
                        TbFundInfo tbFundInfo = new TbFundInfo();
                        tbFundInfo.setUserId(adminUser.getId());//用户编号
                        tbFundInfo.setFundType(1);//1为支出
                        tbFundInfo.setMoney(tbOrder.getPayAmount());//支付金额
                        tbFundInfo.setMoneyPurpose(0);//用途
                        tbFundInfo.setPurposeInfo("积分商品兑换");
                        tbFundInfo.setAddTime(new Date());//添加时间
                        boolean fundSave = tbFundInfoService.save(tbFundInfo);
                        if(ordSave&&userSave&&fundSave){
                            return "success";
                        }
                    }
                    return "failure";
                }else if ("TRADE_FINISHED".equals(params.get("trade_status"))
                       /* && trade_status1.equals(params.get("trade_status"))*/){
                    System.out.println("====================交易失败=========================");
                    return "failure";
                }
            } /*else {
                return "failure";
            }*/
        } catch (AlipayApiException e) {
            return "failure";
        }
        return "failure";
    }

    @GetMapping("notify")
    public ModelAndView sss(HttpServletResponse response, HttpServletRequest req){
        response.setContentType("type=text/html;charset=UTF-8");
        System.out.println("------------同步回调-----------");
        boolean flag = aliPayService.checkSign(req);
        return  new ModelAndView(new RedirectView("http://postal.zfllpay.com:8589/order")).addObject("flag",flag);
    }

    private String getUrl(){
        StringBuffer url = request.getRequestURL();
        String tempContextUrl = url.delete(url.length() - request.getRequestURI().length(), url.length()).append(request.getServletContext().getContextPath()).append("/").toString();
        System.out.println("当前项目url:"+tempContextUrl);
        return tempContextUrl;
    }
}
