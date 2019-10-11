//package com.example.demo.weiXinZhiFu;
//import org.apache.log4j.Logger;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by shang-pc on 2017/8/24.
// */
//@Controller
//@RequestMapping("/api/wx")
//public class WxPaymentController {
//
////    @Resource
////    private OrderService orderService;
//
//    static Logger logger = Logger.getLogger(WxPaymentController.class);
//
//    @Resource
//    private WxConfig wxConfig;
//
//    /***
//     * 生成微信支付二维码的方法
//     * @param orderNo
//     * @return
//     */
//    @RequestMapping(value = "/createcode/{orderNo}", method = RequestMethod.GET)
//    @ResponseBody
//    public void createCode(@PathVariable String orderNo) {
//        //1.构造参数
//        try {
//         //  ItripHotelOrder order = orderService.loadItripHotelOrder(orderNo);
//            HashMap<String, String> data = new HashMap<String, String>();
//            data.put("body", "腾讯充值中心-QQ会员充值");
//            data.put("out_trade_no", orderNo);
//            data.put("device_info", "");
//            data.put("fee_type", "CNY");
////            data.put("total_fee", order.getPayAmount().multiply(new BigDecimal(100)).toBigInteger().toString());
//            data.put("total_fee","1");
//            data.put("spbill_create_ip", "123.12.12.123");
//            data.put("notify_url", "http://itrip.project.bdqn.cn/trade/api/wx/notify");
//            data.put("trade_type", "NATIVE");
//            data.put("nonce_str", WXPayUtil.generateNonceStr());
//            data.put("product_id", "12");
//            data.put("appid", wxConfig.getAppid());
//            data.put("mch_id",wxConfig.getMch_id());
//            data.put("sign_type", "HMAC-SHA256");
//            //2.转成XML请求微信支付平台
//            String reqXml = WXPayUtil.generateSignedXml(data, wxConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
//            //3.请求微信支付平台 获取 预支付交易链接
//            String respXml = WxPayRequest.requestWx("https://api.mch.weixin.qq.com/pay/unifiedorder", reqXml);
//            Map<String, String> resultMap = WXPayUtil.xmlToMap(respXml);
//            if (resultMap.get("return_code").equals("SUCCESS") && resultMap.get("result_code").equals("SUCCESS")) {
//                Map<String, String> result = new HashMap<>();
//                result.put("code_url", resultMap.get("code_url"));
//               // return DtoUtil.returnDataSuccess(result);
//            } else {
//               // return DtoUtil.returnFail(resultMap.get("return_msg"), "100002");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //3.获取code_url 返回给前端
//       // return DtoUtil.returnFail("发生错误", "100001");
//    }
//
//
////    @RequestMapping(value = "/notify", method = RequestMethod.POST)
////    public void wxNotify(HttpServletRequest request, HttpServletResponse response) {
////        //1.从request获取XML流  转化成为MAP 数据
////        logger.info("notify 成功被调用");
////        try {
////            StringBuffer stringBuffer = new StringBuffer();
////            InputStream inputStream = request.getInputStream();
////            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
////            String temp;
////            while ((temp = reader.readLine()) != null) {
////                stringBuffer.append(temp);
////            }
////            reader.close();
////            inputStream.close();
////            Map<String, String> resultMap = WXPayUtil.xmlToMap(stringBuffer.toString());
////            boolean flag = WXPayUtil.isSignatureValid(resultMap, wxConfig.getKey(), WXPayConstants.SignType.HMACSHA256);
////            if (flag) {
////                logger.info("notify 签名验证 成功通过");
////                if (resultMap.get("return_code").equals("SUCCESS") && resultMap.get("result_code").equals("SUCCESS")) {
////                    logger.info("notify 通知 订单支付成功");
////                    String out_trade_no = resultMap.get("out_trade_no");
////                    String trade_no = resultMap.get("prepay_id");
////                    if (!orderService.processed(out_trade_no)) {
////                        orderService.paySuccess(out_trade_no, 2, trade_no);
////                    }
////                }
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        } finally {
////            logger.info("成功返回数据");
////            Map<String, String> returnMap = new HashMap<>();
////            returnMap.put("return_code", "SUCCESS");
////            returnMap.put("return_msg", "SUCESS");
////            try {
////                String respXml = WXPayUtil.generateSignedXml(returnMap,wxConfig.getKey());
////                response.getWriter().write(respXml);
////                response.getWriter().flush();
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////        }
////    }
////
////    @RequestMapping(value = "/queryorderstatus/{orderNo}", method = RequestMethod.GET)
////    @ResponseBody
////    public Dto queryOrderStatus(@PathVariable String orderNo) {
////        try {
////            ItripHotelOrder order = orderService.loadItripHotelOrder(orderNo);
////            return DtoUtil.returnDataSuccess(order);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////        return DtoUtil.returnFail("查询失败","100003");
////    }
//}
