package com.pinyougou.seckill.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class seckillOrderTask {
    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService ;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService ;
    @Scheduled(cron = "0/3 * * * * *")        //关闭超时未支付的订单
            public  void closePayTimeOut() {
        System.out.println("查询超时未支付的订单并关闭");
                     // 获得超时未付的订单
             List<SeckillOrder> outTimeOrders = seckillOrderService.findOrederTimeOut();
        System.out.println(outTimeOrders);
        if(outTimeOrders!=null&&outTimeOrders.size()>0){

            for (SeckillOrder seckillOrder : outTimeOrders) {
                // 关闭微信支付接口
                Map<String,String> map =    weixinPayService.closePayTimeOut(seckillOrder.getId().toString());
                if("SUCCESS".equals(map.get("return_code"))){
                    seckillOrderService.deleteOrderForRedis(seckillOrder);
                }
        }



        }


            }



}
