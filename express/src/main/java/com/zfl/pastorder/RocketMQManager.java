//package com.zfl.pastorder;
//
//import com.github.pagehelper.PageInfo;
//import com.zfl.entity.TbOddNum;
//import com.zfl.entity.vo.QueryVo;
//import com.zfl.service.TbOddNumService;
//import com.zfl.util.DateUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
///**
// * 管理过期订单消息队列对象
// */
//@Component
//@Slf4j
//public class RocketMQManager implements ApplicationRunner{
//
//    //过期订单消费者
//   /* @Autowired
//    private Consumer consumer;*/
//    //过期订单生产者
//    @Autowired
//    private Producer producer;
//    @Autowired
//    private TbOddNumService oddNumService;
//    //查询条件
//    QueryVo<TbOddNum> queryVo = new QueryVo<>();
//
//
//    //系统启动时初始化资源信息
//    @Override
//    public void run(ApplicationArguments args) {
//        //获取消费者和生产者并启动
////        producer.showParam();
////        consumer.showParam();
//        producer.startProducer();
//        //consumer.startConsumer();
//
//        log.info("初始化RocketMQ成功");
//    }
//
//    //定时拉取过期订单
//    //@Scheduled(cron = "0/20 * * * * ?")
//    //@Async
//    public void getTaskOrder(){
//        //如果没有消息推送者和消费者线程不运行
//        if(producer == null || consumer == null){
//            return;
//        }
//        //初始化查询条件
//        queryVo.setPageNo(1);
//        queryVo.setPageSize(50);
//        //要查询的订单中止时间
//        Date endTime = new Date();
//        try {
//            Date startTime = DateUtils.dateAdd(endTime, -1, true);
//            //设置当前为止过去一天的订单
//            queryVo.setStartTime(DateUtils.dateFormat(startTime, DateUtils.DATE_TIME_PATTERN));
//            queryVo.setEndTime(DateUtils.dateFormat(endTime,DateUtils.DATE_TIME_PATTERN));
//        } catch (ParseException e) {
//        }
//        //查询条件实体类
//        queryVo.setData(new TbOddNum());
//        //支付状态
//        queryVo.getData().setOrderState(0);
//        //获取第一页五十条订单信息
//        List<TbOddNum> pastOrderList = oddNumService.getOrderByCondition(queryVo);
//        PageInfo<TbOddNum> pastOrderPage = new PageInfo<>(pastOrderList);
//        //过期订单快递单号
//        ArrayList<String> pastOrderNum = new ArrayList<>();
//        //遍历所有过期订单页码
//        while(true){
//            //没有过期订单时跳出循环,不处理
//            if(pastOrderList == null || pastOrderList.size() < 1){
//                break;
//            }
//            //添加所有过期快递单号
//            pastOrderList.forEach(e -> pastOrderNum.add(e.getTrackingNumber()));
//
//            //如果数据没有查询完毕,查询下一页并重新循环
//            if(pastOrderPage.getPageNum() <= pastOrderPage.getPages()){
//                //查询下一页
//                queryVo.setPageNo(pastOrderPage.getPageNum() + 1);
//                pastOrderList = oddNumService.getOrderByCondition(queryVo);
//                pastOrderPage = new PageInfo<>(pastOrderList);
//
//            }
//
//        }
//        if(pastOrderNum.size() > 0){
//            //开启一条线程发送要更改的快递单号集合(因为生产者和消费者是同时执行的,所有查询完所有后再发送)
//            new Thread(() ->  producer.sendMessage(pastOrderNum)).start();
//            //清空查询信息,供下次使用
////                pastOrderNum.clear();
//            log.info(String.format("查询出%d条过期订单信息",pastOrderNum.size()));
//        }
//
//    }
//
//
//}
