package cn.neteast.page.service;

/**
 * 商品详情页接口
 */
public interface ItemPageService {
    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    Boolean genItemHtml(Long goodsId);
}
