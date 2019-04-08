package com.pinyougou.service;

import com.pinyougou.pojo.SeckillOrder;
import java.util.List;
import java.io.Serializable;
/**
 * SeckillOrderService 服务接口
 * @date 2019-02-27 10:03:32
 * @version 1.0
 */
public interface SeckillOrderService {

	/**
	 * 添加方法
	 */
	void save(SeckillOrder seckillOrder);

	/**
	 * 修改方法
	 */
	void update(SeckillOrder seckillOrder);

	/**
	 * 根据主键id删除
	 */
	void delete(Serializable id);

	/**
	 * 批量删除
	 */
	void deleteAll(Serializable[] ids);

	/**
	 * 根据主键id查询
	 */
	SeckillOrder findOne(Serializable id);

	/**
	 * 查询全部
	 */
	List<SeckillOrder> findAll();

	/**
	 * 多条件分页查询
	 */
	List<SeckillOrder> findByPage(SeckillOrder seckillOrder, int page, int rows);

	/**
	 * 保存秒杀订单到Redis数据库
	 */
	void submitOrderToRedis(String userId, Long id);

	/***
	 *   从redis中获取秒杀的订单
	 */
    SeckillOrder findSeckillOrderForRedis(String userId);

    // 在数据库中插入秒杀商品支付订单,同事将redis中的支付订单删除
	void addOrder(String userId, String transationId);

	/**
	 *     查询超时未支付的订单
	 */

    List<SeckillOrder> findOrederTimeOut();

	/**
	 *     删除redis中超时的订单
	 * @param seckillOrder
	 */
	void deleteOrderForRedis(SeckillOrder seckillOrder);
}
