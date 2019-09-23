package cn.neteast.seckill.service;

import java.util.List;

import cn.neteast.pojo.TbSeckillOrder;

import entity.PageResult;

/**
 * 服务层接口
 *
 * @author Administrator
 */
public interface SeckillOrderService {

    /**
     * 返回全部列表
     *
     * @return
     */
    public List<TbSeckillOrder> findAll();


    /**
     * 返回分页列表
     *
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize);


    /**
     * 增加
     */
    public void add(TbSeckillOrder seckillOrder);


    /**
     * 修改
     */
    public void update(TbSeckillOrder seckillOrder);


    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    public TbSeckillOrder findOne(Long id);


    /**
     * 批量删除
     *
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 分页
     *
     * @param pageNum  当前页 码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize);

    /**
     * 提交订单
     *
     * @param seckillId
     * @param userId
     */
    void submitOrder(Long seckillId, String userId);

    /**
     * 根据用户名查询秒杀订单
     *
     * @param userId
     */
    TbSeckillOrder searchOrderFromRedisByUserId(String userId);

    /**
     * 支付成功保存订单
     *
     * @param userId
     * @param orderId
     */
    void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId);

    /**
     * 从缓存中删除订单
     *
     * @param userId
     * @param orderId
     */
    void deleteOrderFromRedis(String userId, Long orderId);


}
