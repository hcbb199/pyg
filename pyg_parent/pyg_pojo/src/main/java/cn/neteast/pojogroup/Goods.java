package cn.neteast.pojogroup;

import cn.neteast.pojo.TbGoods;
import cn.neteast.pojo.TbGoodsDesc;
import cn.neteast.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

/**
 * 商家添加商品时的组合实体类
 */
public class Goods implements Serializable {
    //商品基本信息SPU
    private TbGoods goods;
    //商品扩展信息SPU
    private TbGoodsDesc goodsDesc;
    //SKU
    private List<TbItem> itemList;

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public String toString() {
        return "Goods{" +
                "goods=" + goods +
                ", goodsDesc=" + goodsDesc +
                ", itemList=" + itemList +
                '}';
    }
}
