package com.zfl.controller.app.shipmentsapp.integralShoppingMall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zfl.controller.app.shipmentsapp.WxPayController;
import com.zfl.entity.*;
import com.zfl.entity.receivingappentity.AliPayRespones;
import com.zfl.jwt.UserJwtService;
import com.zfl.result.Result;
import com.zfl.service.*;
import com.zfl.service.impl.WxPayService;
import com.zfl.service.payservice.AliPayService;
import org.apache.commons.lang3.StringUtils;
//import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author sunzhenpeng
 * @data 2019/9/12
 * @description 积分商城 前端控制器
 */
@EnableTransactionManagement
@RestController
@RequestMapping("/integralMall")
public class IntegralMallController {

    @Autowired
    private TbGoodsService tbGoodsService;

    @Autowired
    private TbOrderService tbOrderService;

    @Autowired
    private UserJwtService userJwtService;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private TbFundInfoService tbFundInfoService;

    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private TbUserInfoService tbUserInfoService;

    @Autowired
    private WxPayService wxPayService;

    /**
     *获取商品列表
     * @author sunzhenpeng
     * @date 2019/9/12
     * @param request
     * @return com.zfl.result.Result
     */
    @PostMapping("/getGoodsList")
    //@RequiresAuthentication
    public IPage<TbGoods> getGoodsList(HttpServletRequest request, @RequestParam Integer pageNo, @RequestParam Integer limit,Integer isAdvance){
        Page<TbGoods> page = new Page<>(pageNo,limit);
        IPage<TbGoods> iPage = tbGoodsService.page(page,new QueryWrapper<TbGoods>().eq("is_putaway",1)
                .eq("is_advance",isAdvance));
        return iPage;
    }

    /**
     *根据商品Id查询商品
     * @author sunzhenpeng
     * @date 2019/9/12
     * @param request
     * @param id
     * @return com.zfl.result.Result
     */
    @PostMapping("/getGoodsById")
    //@RequiresAuthentication
    public Result getGoodsById (HttpServletRequest request, @RequestParam Integer id){
        TbGoods tbGoods = tbGoodsService.getById(id);
        if(tbGoods==null){
            return new Result(201,"此商品已下架或不存在！","");
        }
        return new Result(200,"查询成功！",tbGoods);
    }

    /**
     * 商品购买
     * @author sunzhenpeng
     * @date 2019/9/12
     * @param request
     * @param id
     * @return com.zfl.result.Result
     */
    @PostMapping("/purchase")
    //@RequiresAuthentication
    public Result purchase(HttpServletRequest request, @RequestParam Integer id){
        Map<String,Object> data = new HashMap<>();
        AdminUser user = userJwtService.getUser(request);
        String userTel = (String)request.getAttribute("userTel");
        //查询获取用户电话号码
        TbUserInfo userInfo = tbUserInfoService.getOne(new QueryWrapper<TbUserInfo>().eq("user_id",user.getId()));
        if(userInfo==null)return new Result(202,"请输入联系电话！","");
        userTel = userInfo.getUserTel();
        if(StringUtils.isEmpty(userTel))return new Result(202,"请输入联系电话！","");
        //首先根据Id查询商品信息
        TbGoods tbGoods = tbGoodsService.getById(id);
        //首先判断此商品是不是可预支商品
        int isAdvance = tbGoods.getIsAdvance();
        if(isAdvance==1){//是否可预支积分，0为 是，1为 否
            if(user.getRemainingSum()<tbGoods.getGoodsIntegralPrice()){
                return new Result(201,"积分不足，下单失败！","");
            }
        }else{
            //判断用户积分余额是否足够支付此商品最低积分额度
            if(user.getRemainingSum()<tbGoods.getMinConvertibility()){
                return new Result(201,"积分不足，下单失败！","");
            }
            //再判断此用户是否有未付清的订单
            List<TbOrder> list = tbOrderService.outstandingOrder(user.getId());
            if(list.size()>0){
                return new Result(201,"你尚有其他订单未付清，下单失败！","");
            }
        }
        TbOrder tbOrder = new TbOrder();
        //生成系统订单
        String uuid = UUID.randomUUID().toString().replace("-", "").trim().substring(2,20);
        String orderId = "JFSC-"+uuid;
        System.out.println(orderId);
        tbOrder.setGoodsId(tbGoods.getId());//商品Id
        tbOrder.setId(orderId);//订单编号
        tbOrder.setUserId(user.getId());//用户Id
        tbOrder.setGoodsNum(1);//商品数量
        tbOrder.setOrderStatus(0);//订单状态，待支付
        tbOrder.setOrderTime(new Date());//下单时间
        tbOrder.setUserTel(userTel);//联系电话
        tbOrder.setGoodsPriceTotal(tbGoods.getGoodsMoneyPrice());//商品所需支付金额金额
        tbOrder.setGoodsIntegralTotal(tbGoods.getGoodsIntegralPrice());//商品所需支付的积分
        //创建订单
        boolean save = tbOrderService.save(tbOrder);
        if(!save){
            return new Result(201,"下单失败！","");
        }
        data.put("orderId",orderId);//订单id
        data.put("goodsId",tbGoods.getId());//商品id
        return new Result(200,"下单成功",data);
    }

    @PostMapping("/paymentOrder")
    //@RequiresAuthentication
    @Transactional
    public Result paymentOrder(HttpServletRequest request, @RequestParam String orderId,Integer goodsId) throws Exception {
        //首先获取当前登录用户
        AdminUser adminUser = userJwtService.getUser(request);
        //再根据订单号获取订单
        TbOrder tbOrder = tbOrderService.getById(orderId);
        //获取商品，支付时需要用到
        TbGoods good = tbGoodsService.getById(goodsId);
        if(tbOrder.getOrderStatus()==1){
            return new Result(201,"此订单已完成支付，无需二次支付！","");
        }
        //此处需判断此订单的商品是否分期
        if(good.getIsAdvance()==0){//可预支
            //商品为可预支的话，只需判断用户余额大于等于商品最小支付积分即可完成支付
            if(adminUser.getRemainingSum()<good.getMinConvertibility()){
                return new Result(201,"积分余额不足，支付失败！","");
            }
        }else{//不可预支
            if(adminUser.getRemainingSum()<tbOrder.getGoodsIntegralTotal()){
                return new Result(201,"积分余额不足，支付失败！","");
            }
        }
        //判断此商品现金价格如果为0的话，直接扣除积分，不走第三方支付平台
        if(tbOrder.getGoodsPriceTotal()==0){
            //修改订单状态
            tbOrder.setOrderStatus(1);//支付状态
            tbOrder.setPayAmount(0);//支付金额
            tbOrder.setPayTime(new Date());//支付时间
            tbOrderService.updateById(tbOrder);
            //扣除用户积分余额，产生收入支出明细记录
            boolean ordSave = tbOrderService.updateById(tbOrder);//更新订单
            //扣除用户积分余额
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
            if(!ordSave||!userSave||!fundSave){
                return  new Result(201, "支付失败！", "");
            }
            return new Result(200, "支付成功！", "");
        }else {//此商品现金价格如果为0,调用第三方支付平台，进行现金支付
            String form = "";
            //支付宝
            AliPayRespones aliPayRespones = new AliPayRespones();
            if (tbOrder.getPayType().equals("1")){//支付宝支付
            aliPayRespones = aliPayService.getPreOrder(good.getGoodsName(), tbOrder.getGoodsPriceTotal(), tbOrder.getId());
                return new Result(200, "", aliPayRespones);
            }
            //微信支付
            Result result = wxPayService.pay(request,tbOrder.getId());
            return result;
        }
    }

    /**
     *兑换商品继续支付
     * @author sunzhenpeng
     * @date 2019/10/9
     * @param id
     * @param payType
     * @return com.zfl.result.Result
     */
    @PostMapping("continuePay")
    public Result continuePay(HttpServletRequest request,@RequestParam Integer id,String payType) throws Exception {
        TbOrder order = tbOrderService.getById(id);
        if (order==null)return new Result(201,"此订单不存在，请刷新页面！","");
        //获取订单状态
        if(order.getOrderStatus()!=1){
            return new Result(201,"此订单已支付,请确认！","");
        }
        TbGoods goods = tbGoodsService.getById(order.getGoodsId());
        if(goods==null)return new Result(201,"系统数据错误！","");
        //支付宝
        AliPayRespones aliPayRespones = new AliPayRespones();
        if (StringUtils.equals(payType,"1")){
            aliPayRespones = aliPayService.getPreOrder(goods.getGoodsName(),order.getPayAmount(),order.getId());
            String form=aliPayRespones.getPayUrl().replace("<script>document.forms[0].submit();</script>","");
            System.out.println(form);
            return new Result(200,"请求成功",form);
        }else{
            //微信支付
            Result result = wxPayService.pay(request,order.getId());
            return result;
        }

    }

    @PostMapping("/getMyOrderList")
    public IPage<TbOrder> getMyOrderList(HttpServletRequest request, @RequestParam Integer pageNo, @RequestParam Integer limit){
        //首先获取当前登录用户
        AdminUser adminUser = userJwtService.getUser(request);
        Page<TbOrder> page = new Page<>(pageNo,limit);
        IPage<TbOrder> iPage = tbOrderService.page(page,new QueryWrapper<TbOrder>().eq("user_id",adminUser.getId()));
        iPage.getRecords().forEach(t->{
            t.setTbGoods(tbGoodsService.getById(t.getGoodsId()));
        });
        return iPage;
    }

}
