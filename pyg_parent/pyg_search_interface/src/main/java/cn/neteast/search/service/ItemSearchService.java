package cn.neteast.search.service;

import cn.neteast.pojo.TbItem;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务的接口
 */
public interface ItemSearchService {
    /**
     * 根据搜索条件查询
     * @param searchMap
     * @return
     */
    Map<String, Object> search(Map searchMap);

    /**
     * 新审核通过商品通过后同步redis的SKU列表
     * @param list
     */
    void importList(List<TbItem> list);

    /**
     * 商品被删除后同步redis的SKU列表
     * @param goodIdList
     */
    void deleteByGoodIdList(List<Long> goodIdList);
}
