//package com.zfl.pastorder;
//
//import com.zfl.redisservice.RedisService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.producer.TransactionMQProducer;
//import org.apache.rocketmq.client.producer.TransactionSendResult;
//import org.apache.rocketmq.common.message.Message;
//import org.apache.rocketmq.remoting.common.RemotingHelper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
///**
// * @Auther: wbh
// * @Date: 2019/9/5 11:51
// * @Description:
// */
//@Slf4j
//@Component
//public class Producer {
//
//    //负责发送过期订单信息的消费者
//    private TransactionMQProducer producer;
//    @Value("${rocketmq.orderPast.producerGroup}")
//    private String orderProducerGroup;
//    @Value("${rocketmq.namesrvAddr}")
//    private String namesrvAddr;
//    @Value("${rocketmq.orderPast.topic}")
//    private String orderPastTopic;
//    @Value("${rocketmq.orderPast.tags}")
//    private String orderPastTags;
//    @Autowired
//    private RedisService redisService;
//    //用于统计新发送信息数量
//    int count = 0;
//
//    public void showParam(){
//        System.out.println("orderProducerGroup = " + orderProducerGroup);
//        System.out.println("namesrvAddr = " + namesrvAddr);
//        System.out.println("orderPastTopic = " + orderPastTopic);
//        System.out.println("orderPastTags = " + orderPastTags);
//    }
//
//    /**
//     * 发送快递单号信息
//     * @param oddNumList 订单快递单号集合
//     */
//    public synchronized void sendMessage(List<String> oddNumList){
//        oddNumList.forEach(e -> {
//            //判断该过期订单号是否已发送消费信息
//            if(redisService.hasKey(e)){
//                //lambda中使用return代替continue
//                return;
//            }
//            try {
//                //将过期订单信息存储到数据库中
//                redisService.set(e,0,86400);
//                //新建一条消息 keys和body都为过期订单号
//                Message message = new Message(orderPastTopic, orderPastTags, e, e.getBytes(RemotingHelper.DEFAULT_CHARSET));
//                //发送一条事务消息
//                TransactionSendResult result = producer.sendMessageInTransaction(message, e + "");
//                //避免发送过快而失败
////                Thread.sleep(5);
//                count++;
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//        //如果缓存中有该消息数据,则不重复发送
//        log.info(String.format("新发送%d条过期订单信息,%d条过期订单信息已经发送过",count,oddNumList.size() - count));
//        count = 0;
//    }
//
//    /**
//     * 开启过期订单生产者
//     * @return
//     */
//    public boolean startProducer() {
//        try {
//            producer = new TransactionMQProducer(orderProducerGroup);
//            producer.setNamesrvAddr(namesrvAddr);
//            //设置事务监听
//            TransactionListenerImpl transactionListener = new TransactionListenerImpl();
//            producer.setTransactionListener(transactionListener);
//            //线程池
//            ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 20, 100, TimeUnit.SECONDS,
//                    new ArrayBlockingQueue<Runnable>(2000), new ThreadFactory() {
//                @Override
//                public Thread newThread(Runnable r) {
//                    Thread thread = new Thread(r);
//                    thread.setName("client-transaction-msg-check-thread");
//                    return thread;
//                }
//            }
//            );
//            //设置线程池
//            producer.setExecutorService(pool);
//            producer.start();
//            log.info("过期订单消息生产者启动成功");
//        } catch (Exception e) {
//            producer = null;
//            return  false;
//        }
//
//        return true;
//    }
//
//    public void shutdownProducer(){
//        producer.shutdown();
//    }
//
//}
