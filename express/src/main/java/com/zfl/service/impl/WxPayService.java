package com.zfl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zfl.entity.AdminUser;
import com.zfl.entity.TbOddNum;
import com.zfl.entity.TbOrder;
import com.zfl.jwt.UserJwtService;
import com.zfl.result.Result;
import com.zfl.service.*;
import com.zfl.util.WxTokenUtil;
import com.zfl.util.XMLUtil;
import com.zfl.wxpay.sdk.WXPayUtil;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author sunzhenpeng
 * @data 2019/10/10
 * @description 微信支付服务类
 */
@Service
public class WxPayService {

    @Autowired
    private TbSysConfigService tbSysConfigService;

    @Autowired
    private UserJwtService userJwtService;

    @Autowired
    private TbOrderService tbOrderService;

    @Autowired
    private TbOddNumService tbOddNumService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private TbFundInfoService tbFundInfoService;

    public Result pay(HttpServletRequest request, String orderId) throws Exception {
        String AppID = tbSysConfigService.getConfigValue("AppID");
        String mchid = tbSysConfigService.getConfigValue("mch_id");
        String AppSecret = tbSysConfigService.getConfigValue("AppSecret");
        String mchKey = tbSysConfigService.getConfigValue("mch_key");
        Map<String, Object> map = new HashMap<>();
        AdminUser user = userJwtService.getUser(request);
        if (user == null) {
            return new Result(201, "请先登录！", "");
        }
        String openId = user.getOpenid();
        Integer totalFee = 0;
        System.out.println("请求订单单号为：" + orderId);
        if (orderId.substring(0, 5).equals("KDDD-")) {//此为快递订单

            TbOddNum tbOddNum = tbOddNumService.getOne(new QueryWrapper<TbOddNum>().eq("sys_num", orderId));
            if (tbOddNum == null) {
                return new Result(201, "此订单不存在！", "");
            }
            totalFee = Integer.valueOf((int) (tbOddNum.getPayPrice() * 100));
        } else if (orderId.substring(0, 5).equals("JFSC-")) {
            TbOrder order = tbOrderService.getById(orderId);
            if (order == null) {
                return new Result(201, "此订单不存在！", "");
            }
            totalFee = Integer.valueOf((int) (order.getPayAmount() * 100));
        }
        System.out.println("请求订单支付金额为：" + totalFee);
        //拼接统一下单地址参数
        Map<String, String> paraMap = new TreeMap<String, String>();
        //String openId ="offK61ZNg66aBjn8tkIg1SI1EL98";

//			prices=String.valueOf(Double.parseDouble(prices)*100);
        String today = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String ip = InetAddress.getLocalHost().getHostAddress();
        String nonceStr = WXPayUtil.generateNonceStr();

        paraMap.put("appid", AppID);
        paraMap.put("body", "商城-订单结算");
        paraMap.put("mch_id", mchid);
        paraMap.put("nonce_str", nonceStr);
        paraMap.put("openid", openId);
        paraMap.put("out_trade_no", orderId);//订单号
        paraMap.put("spbill_create_ip", ip);//支持IPV4和IPV6两种格式的IP地址。用户的客户端IP
        paraMap.put("total_fee", String.valueOf(totalFee));
        paraMap.put("trade_type", "JSAPI");//http://postal.zfllpay.com:8089/WxPay/notify
        paraMap.put("notify_url", "http://192.168.0.116:8089/WxPay/notify");// 此路径是微信服务器调用支付结果通知路径随意写
        String sign = WXPayUtil.generateSignature(paraMap, mchKey);
        //String sign = WXPayUtil.createSign("UTF-8", paraMap, mch_secret);
        paraMap.put("sign", sign);
        System.out.println(sign);
        String xml = WXPayUtil.mapToXml(paraMap);//将所有参数(map)转xml格式
        System.out.println(xml);
        // 统一下单 https://api.mch.weixin.qq.com/pay/unifiedorder
        String unifiedorder_url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

        //	String xmlStr = HttpRequest.sendPost(unifiedorder_url, xml);//发送post请求"统一下单接口"返回预支付id:prepay_id
        Connection.Response wxPayResponse = com.zfl.util.HttpUtils.post(unifiedorder_url, xml);
        String wxPayResult = wxPayResponse.body();
        System.out.println(wxPayResponse);
        //以下内容是返回前端页面的json数据
        String prepay_id = "";//预支付id
        String noncstr = "";
        if (wxPayResult.indexOf("SUCCESS") != -1) {
            Map<String, String> map1 = XMLUtil.doXMLParse(wxPayResult);
            prepay_id = (String) map1.get("prepay_id");
            noncstr = (String) map1.get("nonce_str");
        }
        SortedMap<Object, Object> payMap = new TreeMap<Object, Object>();
        payMap.put("appId", AppID);
        payMap.put("nonceStr", noncstr);
        payMap.put("package", "prepay_id=" + prepay_id);
        payMap.put("signType", "MD5");
        payMap.put("timeStamp", today);
        payMap.put("key", mchKey);

        String paySign = WXPayUtil.generateSignature(paraMap, mchKey);
        payMap.put("paySign", paySign);
        //生成jsapiMap
        WxTokenUtil wxTokenUtil = new WxTokenUtil();
        String jsApiTicket = wxTokenUtil.getJicket();
        String currentUrl = "http://postal.zfllpay.com/send/";
        Map signMap = wxTokenUtil.sign(currentUrl, jsApiTicket);
        Map payResultMap = new HashMap() {{
            put("payResult", payMap);
            put("jsApiResult", signMap);
        }};
        return new Result(200, "", payResultMap);
    }
}
