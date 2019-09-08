package cn.neteast.sellergoods.service;

import java.util.List;

import cn.neteast.pojo.TbGoods;

import cn.neteast.pojo.TbItem;
import cn.neteast.pojogroup.Goods;
import entity.PageResult;
import entity.Result;

/**
 * 服务层接口
 *
 * @author Administrator
 */
public interface GoodsService {

    /**
     * 返回全部列表
     *
     * @return
     */
    public List<TbGoods> findAll();


    /**
     * 返回分页列表
     *
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize);


    /**
     * 增加
     */
    public void add(Goods goods);


    /**
     * 修改
     */
    public void update(Goods goods);


    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    public Goods findOne(Long id);


    /**
     * 批量删除(修改商品的IsDelete字段内容: 1为删除,null为未删除)
     *
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 分页
     *
     * @param pageNum  当前页码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize);

    /**
     * 批量审核(修改商品的状态信息: 通过或驳回)
     *
     * @param selectIds
     * @param status
     */
    void updateStatus(Long[] selectIds, String status);

    /**
     * 批量下架(修改商品的IsMarketable字段内容: null为在售, 1为下架)
     *
     * @param selectIds
     */
    void disableSelected(Long[] selectIds);

    /**
     * 根据商品id和状态查询SKU列表
     *
     * @param selectIds
     * @param status
     * @return
     */
    List<TbItem> findItemListByGoodsIdAndStatus(Long[] selectIds, String status);
}
