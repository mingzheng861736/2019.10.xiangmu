package com.zfl.controller.app.shipmentsapp;

import java.io.*;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zfl.entity.AdminUser;
import com.zfl.entity.TbFundInfo;
import com.zfl.entity.TbOddNum;
import com.zfl.entity.TbOrder;
import com.zfl.jwt.UserJwtService;
import com.zfl.result.Result;
import com.zfl.service.*;
import com.zfl.service.impl.WxPayService;
import com.zfl.util.WxTokenUtil;
import com.zfl.util.XMLUtil;
import com.zfl.wxpay.sdk.WXPayUtil;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import xin.zhuyao.httputil.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/WxPay")
public class WxPayController {

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

    @Autowired
    private WxPayService wxPayService;

    @PostMapping("goTopay")
    @ResponseBody
    public Result pay(HttpServletRequest request, String orderId) throws Exception {
        String AppID = tbSysConfigService.getConfigValue("AppID");
        String mchid = tbSysConfigService.getConfigValue("mch_id");
        String AppSecret = tbSysConfigService.getConfigValue("AppSecret");
        String mchKey = tbSysConfigService.getConfigValue("mch_key");
        Map<String ,Object> map = new HashMap<>();
        AdminUser user = userJwtService.getUser(request);
        if(user==null){
            return new Result(201,"请先登录！","");
        }
        String openId = user.getOpenid();
        Integer totalFee = 0;
        if(orderId.substring(0,5).equals("KDDD-")){//此为快递订单
            TbOddNum tbOddNum = tbOddNumService.getOne(new QueryWrapper<TbOddNum>().eq("sys_num",orderId));
            if (tbOddNum==null){
                return new Result(201,"此订单不存在！","");
            }
            totalFee = Integer.valueOf((int)(tbOddNum.getPayPrice()*100));
        }else if(orderId.substring(0,5).equals("JFSC-")){
            TbOrder order = tbOrderService.getById(orderId);
            if (order==null){
                return new Result(201,"此订单不存在！","");
            }
            totalFee = Integer.valueOf((int)(order.getPayAmount()*100));
        }

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
        paraMap.put("total_fee",String.valueOf(totalFee));
        paraMap.put("trade_type", "JSAPI");//http://postal.zfllpay.com:8089/WxPay/notify
        paraMap.put("notify_url","http://192.168.0.116:8089/WxPay/notify");// 此路径是微信服务器调用支付结果通知路径随意写
        String sign = WXPayUtil.generateSignature( paraMap, mchKey);
        //String sign = WXPayUtil.createSign("UTF-8", paraMap, mch_secret);
        paraMap.put("sign", sign);
        System.out.println(sign);
        String xml = WXPayUtil.mapToXml(paraMap);//将所有参数(map)转xml格式
        System.out.println(xml);
        // 统一下单 https://api.mch.weixin.qq.com/pay/unifiedorder
        String unifiedorder_url ="https://api.mch.weixin.qq.com/pay/unifiedorder";

        //	String xmlStr = HttpRequest.sendPost(unifiedorder_url, xml);//发送post请求"统一下单接口"返回预支付id:prepay_id
        Connection.Response wxPayResponse = com.zfl.util.HttpUtils.post(unifiedorder_url, xml);
        String wxPayResult=wxPayResponse.body();
        System.out.println(wxPayResponse);
        //以下内容是返回前端页面的json数据
        String prepay_id = "";//预支付id
        String noncstr="";
        if (wxPayResult.indexOf("SUCCESS") != -1) {
            Map<String, String> map1 = XMLUtil.doXMLParse(wxPayResult) ;
            prepay_id = (String) map1.get("prepay_id");
            noncstr=(String) map1.get("nonce_str");
        }
        SortedMap<Object, Object> payMap = new TreeMap<Object, Object>();
        payMap.put("appId", AppID);
        payMap.put("nonceStr", noncstr);
        payMap.put("package", "prepay_id=" + prepay_id);
        payMap.put("signType", "MD5");
        payMap.put("timeStamp", today);
        payMap.put("key", mchKey);

         String paySign = WXPayUtil.generateSignature( paraMap, mchKey);
        payMap.put("paySign", paySign);
        //生成jsapiMap
        WxTokenUtil wxTokenUtil=new WxTokenUtil();
        String jsApiTicket=wxTokenUtil.getAccessTokenJsapi();
        String currentUrl="http://postal.zfllpay.com/#/send";
        Map signMap=wxTokenUtil.sign(currentUrl,jsApiTicket);
        Map payResultMap=new HashMap(){{
            put("payResult",payMap);
            put("jsApiResult",signMap);
        }};
        return new Result(200,"",payResultMap);
      // return wxPayService.pay(request,orderId);
    }

    /**
     * 支付成功后，接收微信返回的回调方法
     */
    @RequestMapping(value="/notify", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public void order(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String msg = "success";
        response.setContentType("text/xml");
        String resXml = "";
        BufferedReader reader = null;
        reader = request.getReader();
        String line = "";
        String xmlString = null;
        StringBuffer inputString = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            inputString.append(line);
        }
        xmlString = inputString.toString();
        request.getReader().close();
        Document doc = null;
        try {
            // 下面的是通过解析xml字符串的
            doc = DocumentHelper.parseText(xmlString); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            Element recordEle = (Element)rootElt;
            String result_code = recordEle.elementTextTrim("result_code");
            String return_code = recordEle.elementTextTrim("return_code");
            String out_trade_no = recordEle.elementTextTrim("out_trade_no");
            String transaction_id = recordEle.elementTextTrim("transaction_id");//微信支付订单号
            String time_end = recordEle.elementTextTrim("time_end");//微信支付时间
            //   String stuNo =out_trade_no.split("_ms_")[1];
            System.out.println(out_trade_no);
            //在此处开始进行数据库订单记录状态插入与修改
            //Orders o=new Orders();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            if(return_code.indexOf("SUCCESS")!=-1){
                if(result_code.indexOf("SUCCESS")!=-1){
                    //支付成功,获取相应的订单参数
                    if(out_trade_no.substring(0,5).equals("KDDD-")){//此为快递订单
                        TbOddNum tbOddNum = tbOddNumService.getById(out_trade_no);
                        tbOddNum.setOrderState(1);
                        tbOddNum.setSysNum(transaction_id);
                        tbOddNumService.updateById(tbOddNum);
                    }else if(out_trade_no.substring(0,5).equals("JFSC-")){
                        TbOrder order = tbOrderService.getById(out_trade_no);
                        order.setOrderStatus(1);
                        order.setPayTime(sdf.parse(time_end));
                        order.setPayIntegral(order.getGoodsIntegralTotal());//支付积分
                        tbOrderService.updateById(order);
                        //首先获取当前登录用户
                        AdminUser adminUser = userJwtService.getUser(request);
                        //扣除用户积分余额，产生收入支出明细记录
                        adminUser.setRemainingSum(adminUser.getRemainingSum()-order.getGoodsIntegralTotal());
                        adminUserService.updateById(adminUser);
                        //生成收支明细
                        TbFundInfo tbFundInfo = new TbFundInfo();
                        tbFundInfo.setUserId(adminUser.getId());//用户编号
                        tbFundInfo.setFundType(1);//1为支出
                        tbFundInfo.setMoney(order.getPayAmount());//支付金额
                        tbFundInfo.setMoneyPurpose(0);//用途
                        tbFundInfo.setPurposeInfo("积分商品兑换");
                        tbFundInfo.setAddTime(new Date());//添加时间
                        tbFundInfoService.save(tbFundInfo);
                    }

                }else{
                    //支付失败

                }
                //这一步非常重要：返回给微信成功通知，否则会一直回调。
                resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
                        + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
                BufferedOutputStream out = new BufferedOutputStream(
                        response.getOutputStream());
                out.write(resXml.getBytes());
                out.flush();
                out.close();
                response.getWriter().println(msg);
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 通过xml 发给微信消息
    public static String setXml(String return_code, String return_msg) {
        return "<xml><return_code><![CDATA[" + return_code + "]]>" + "</return_code><return_msg><![CDATA[" + return_msg
                + "]]></return_msg></xml>";
    }
}

