//package com.zfl.pastorder;
//
//import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
//import com.zfl.entity.TbOddNum;
//import com.zfl.redisservice.RedisService;
//import com.zfl.service.TbOddNumService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
//import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
//import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 监听过期订单消息并处理
// */
//@Component
//@Slf4j
//public class ConsumerListener implements MessageListenerConcurrently {
//
//    @Autowired
//    private TbOddNumService oddNumService;
//    @Value("${rocketmq.orderPast.topic}")
//    private String orderPastTopic;
//    @Value("${rocketmq.orderPast.tags}")
//    private String orderPastTags;
//    @Autowired
//    private RedisService redisService;
//
//    @Override
//    @Async
//    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
//        UpdateWrapper<TbOddNum> updateWrapper = new UpdateWrapper<>();
//        //要修改的过期订单 的快递单号列表
//        List<String> oddNumList = new ArrayList<>();
//        //过期订单消息,key: 消息keys,value: 过期订单订单号
//        Map<String,Integer> pastOddMap = new HashMap<>();
//        //迭代消息信息
//        for (MessageExt msg : msgs) {
//            //获取消息key,快递单号
//            String orderNum = msg.getKeys();
//
//            //查看消息是否被消费过(-1已消费,0未消费,0以上表示消费失败,已经重试的次数)
//            Integer count = (Integer) redisService.get(orderNum);
//            //如果缓存中没有该消息消费信息,设置默认值
//            count = count == null ? 0 : count;
//            try {
//                //消息已消费过不在消费
//                if(count == -1){
//                    continue;
//                }
//                //获取消息主体
//                String msgTopic = msg.getTopic();
//                //获取消费标签
//                String msgTags = msg.getTags();
//                //判断消费信息是否正确
//                if (orderPastTopic.equals(msgTopic) && orderPastTags.equals(msgTags)) {
//                    //获取消费信息主体(key和body都为快递单号)
////                    String orderNum = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
//                    oddNumList.add(orderNum);
//                    //设置消息的消费信息
//                    pastOddMap.put(orderNum,-1);
//                }
//            } catch (Exception e) {
//                //清空所有消费操作数据
//                pastOddMap.clear();
//                oddNumList.clear();
//                //打印错误信息
//                log.error(String.format("第%d次处理该消息时出现异常,等待下一次重试",msg.getReconsumeTimes()+1));
//                //等待下次消费
//                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
//            }
//        }
//        //如果拉取到的消息没有可操作的,跳出方法
//        if(oddNumList.size() == 0){
//            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//        }
//        //设置更改条件
//        updateWrapper.in("tracking_number", oddNumList);
//        updateWrapper.set("order_state",-1);
//        TbOddNum tbOddNum = new TbOddNum();
//        tbOddNum.setOrderState(-1);
//        //更改数据库中过期订单的信息
//        oddNumService.update(updateWrapper);
//        //消费信息保存到缓存中一天
//        pastOddMap.forEach((k,v) -> redisService.set(k,v,86400));
//        //清空所有消费信息
//        pastOddMap.clear();
//        //清空所有订单号信息
//        oddNumList.clear();
//        updateWrapper = new UpdateWrapper<>();
//        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//    }
//}