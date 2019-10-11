package com.example.demo.controller;

import com.example.demo.weiXinZhiFu.WXPayConstants;
import com.example.demo.weiXinZhiFu.WXPayUtil;
import com.example.demo.weiXinZhiFu.WxPayRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
public class httpController {
    @RequestMapping("/test")
    public String a1() throws Exception {

        HashMap<String, String> data = new HashMap<String, String>();
        data.put("body", "商品描述");
        data.put("out_trade_no", "123456789zxc");//订单号
        //data.put("device_info", "");
        // data.put("fee_type", "CNY");
        // data.put("total_fee", order.getPayAmount().multiply(new BigDecimal(100)).toBigInteger().toString());
        data.put("total_fee", "1");//订单总金额，单位为分
        data.put("spbill_create_ip", "123.12.12.123");//支持IPV4和IPV6两种格式的IP地址。用户的客户端IP
        data.put("notify_url", "http://42.228.200.84/wx/notify");//异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        data.put("trade_type", "JSAPI");//JSAPI -JSAPI支付
        data.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串，长度要求在32位以内。
        // data.put("product_id", "12");
        data.put("openid","oPzpO1fsJM_m7nlCcibTy3n07D-I");
        data.put("appid", "wx1b8fccd09b244d15");
        data.put("mch_id", "1558303321");
        data.put("sign_type", "HMAC-SHA256");
        //2.转成XML请求微信支付平台
        String reqXml = WXPayUtil.generateSignedXml(data, "TZArLFzaL5L5ZTI7GNctqxVUn8d0pE5V", WXPayConstants.SignType.HMACSHA256);
        //3.请求微信支付平台 获取 预支付交易链接
        String respXml = WxPayRequest.requestWx("https://api.mch.weixin.qq.com/pay/unifiedorder", reqXml);
        Map<String, String> resultMap = WXPayUtil.xmlToMap(respXml);
        if (resultMap.get("return_code").equals("SUCCESS") && resultMap.get("result_code").equals("SUCCESS")) {
            Map<String, String> result = new HashMap<>();
            result.put("code_url", resultMap.get("code_url"));
            // return DtoUtil.returnDataSuccess(result);
        } else {
            // return DtoUtil.returnFail(resultMap.get("return_msg"), "100002");
        }
        return "";
    }

}
