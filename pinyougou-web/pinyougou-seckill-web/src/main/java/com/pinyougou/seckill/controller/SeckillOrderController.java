package com.pinyougou.seckill.controller;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import com.pinyougou.service.WeixinPayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 秒杀订单控制器
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-03-23<p>
 */
@RestController
@RequestMapping("/order")
public class SeckillOrderController {

    @Reference(timeout = 10000)
    private SeckillOrderService seckillOrderService;
    @Reference(timeout = 10000)
    private WeixinPayService weixinPayService ;



    /** 秒杀下单 */
    @GetMapping("/submitOrder")
    public boolean submitOrder(Long id, HttpServletRequest request) {
        try {           //获取用户名
        String userId = request.getRemoteUser();
              // 调用业务层方法,将订单存入数据库
            seckillOrderService.submitOrderToRedis(userId,id);
            return true ;
        } catch (Exception e) {
            e.printStackTrace();
        }
             return  false ;

    }

    /** 获取支付二维码 */
    @GetMapping("/getPayCode")
     public Map<String,Object>getPayCode (HttpServletRequest request) {
        try {
            //获取用户名
            String userId = request.getRemoteUser();
            //从数据库中获取秒杀订单
    SeckillOrder seckillOrder=seckillOrderService.findSeckillOrderForRedis(userId);
            String outTradeNo = String.valueOf(seckillOrder.getId());
          long totalFee = (long)((seckillOrder.getMoney().doubleValue()) * 100);
               // 获得支付二维码
            return    weixinPayService.genPayCode(outTradeNo,String.valueOf(totalFee)) ;


        } catch (Exception e) {
            e.printStackTrace();
        }

         return  null ;
    }
/** 查询支付状态 */
    @GetMapping("/queryPayStatus")
      public Map<String,String> queryPayStatus(String outTradeNo,HttpServletRequest request){
       Map<String,String> data =   new HashMap<>() ;
        String userId = request.getRemoteUser();
        try {
                         data.put("status","3");
            Map<String, String> map = weixinPayService.queryPayStatus(outTradeNo);
                          if(map!=null&&map.size()>0){
                              //   代表支付成功
                if ("SUCCESS".equals(map.get("trade_state"))) {
                    // 在数据库中插入秒杀商品支付订单,同事将redis中的支付订单删除
                  seckillOrderService.addOrder(userId,map.get("transaction_id"));

                    data.put("status", "1");
 }
                              //代表未支付
                              if ("NOTPAY".equals(map.get("trade_state"))) {
                                  data.put("status", "2");
                              }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return data ;
    }

}
