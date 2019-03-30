package com.pinyougou.seckill.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillGoods;
import com.pinyougou.pojo.SeckillOrder;
import com.pinyougou.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 秒杀订单服务接口实现类
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-03-23<p>
 */
@Service(interfaceName = "com.pinyougou.service.SeckillOrderService")
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Override
    public void save(SeckillOrder seckillOrder) {

    }

    @Override
    public void update(SeckillOrder seckillOrder) {

    }

    @Override
    public void delete(Serializable id) {

    }

    @Override
    public void deleteAll(Serializable[] ids) {

    }

    @Override
    public SeckillOrder findOne(Serializable id) {
        return null;
    }

    @Override
    public List<SeckillOrder> findAll() {
        return null;
    }

    @Override
    public List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows) {
        return null;
    }



    /**
     * 保存秒杀订单到Redis数据库
     * 该方法必须是线程安全的 synchronized 线程锁(一条进程)
     * 多进程中的线程是安全的：分布式锁(Redis、zookeeper、mysql) 1.txt
     * key: lock true
     *
     * 数据库本来就有锁: 如果事务引擎用得是innoDB，默认采用行级锁 (表锁)
     * */
    @Override
    public void submitOrderToRedis(String userId, Long id) {
        try {
            //根据id从redis 数据库获取下单 秒杀商品
            SeckillGoods seckillGoods =(SeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").get(id);
             // 判断商品库存
              if(seckillGoods!=null && seckillGoods.getStockCount()>0){
            // 同时修改redis中秒杀商品的库存
            seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
            //如果redis中商品库存被秒光,则删除该商品 ,同时修改数据库中秒杀商品的状态
            if(seckillGoods.getStockCount()==0){
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                redisTemplate.boundHashOps("seckillGoodsList").delete(id);

        } //否则将修改后的库存存入redis中
            else
            {     redisTemplate.boundHashOps("seckillGoodsList").put(id,seckillGoods);}


            //生成秒杀商品订单存入redis中
            SeckillOrder seckillOrder = new SeckillOrder();
            // 设置秒杀商品订单id
            seckillOrder.setId(idWorker.nextId());
            // 设置秒杀订单商品的id
            seckillOrder.setSeckillId(id);
            // 设置秒杀商品订单金额
            seckillOrder.setMoney(new BigDecimal(seckillGoods.getCostPrice().doubleValue()));
            // 设置秒杀商品订单用户名
            seckillOrder.setUserId(userId);
            // 设置秒杀商品订单商家id
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            // 设置秒杀商品订单创建时间
            seckillOrder.setCreateTime(new Date());
            // 设置秒杀商品支付状态
            seckillOrder.setStatus("0");
            // 将 秒杀商品订单传入redis数据库
            redisTemplate.boundHashOps("seckillOrder").put(userId,seckillOrder);

              }

        } catch (Exception e) {
           throw  new RuntimeException(e) ;
        }


    }

    /***
     *   从redis中获取秒杀的订单
     */


    public SeckillOrder findSeckillOrderForRedis(String userId){
        try {

            SeckillOrder seckillOrder = (SeckillOrder)   redisTemplate.boundHashOps("seckillOrder").get(userId);
                 return  seckillOrder ;
        }  catch (Exception e) {
            throw  new RuntimeException(e);
        }

    };
    // 在数据库中插入秒杀商品支付订单,同事将redis中的支付订单删除
    public void addOrder(String userId, String transationId){
               // 获取redis中预支付订单
               SeckillOrder seckillOrder =(SeckillOrder)    redisTemplate.boundHashOps("seckillOrder").get(userId);
                    seckillOrder.setPayTime(new Date());
                     seckillOrder.setStatus("1");
                    seckillOrder.setTransactionId(transationId);
                  // 将付完款的订单插入数据库
                     seckillOrderMapper.insertSelective(seckillOrder);
                     //同时删除redis的预付订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
   }

}
