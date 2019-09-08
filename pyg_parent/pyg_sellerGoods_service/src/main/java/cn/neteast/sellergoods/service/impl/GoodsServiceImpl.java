package cn.neteast.sellergoods.service.impl;

import java.util.*;

import cn.neteast.mapper.*;
import cn.neteast.pojo.*;
import cn.neteast.pojogroup.Goods;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.neteast.pojo.TbGoodsExample.Criteria;
import cn.neteast.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //从组合对象中取出TbGoods对象, 设置状态值为未申请状态, 保存到数据库
        goods.getGoods().setAuditStatus("0");
        //给属性为空的项设置初始值, 防止修改时报错
        if (goods.getGoods().getIsEnableSpec() == null) {
            //默认不启用规格
            goods.getGoods().setIsEnableSpec("0");
        }
        goodsMapper.insert(goods.getGoods());

        //从组合对象中取出TbGoodsDesc对象, 将插入TbGoods对象的id值赋值给此TbGoodsDesc对象的id值, 并保存到数据库
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //给属性为空的项设置初始值, 防止修改时报错
        if (goods.getGoodsDesc().getItemImages() == null) {
            //默认图片列表为空数组
            goods.getGoodsDesc().setItemImages("[]");
        }
        if (goods.getGoodsDesc().getSpecificationItems() == null) {
            //默认属性列表为空数组
            goods.getGoodsDesc().setItemImages("[]");
        }
        if (goods.getGoodsDesc().getCustomAttributeItems() == null) {
            //默认扩展属性列表为空数组
            goods.getGoodsDesc().setCustomAttributeItems("[]");
        }
        goodsDescMapper.insert(goods.getGoodsDesc());
        //保存SKU列表到数据库中
        insertItemList(goods);

    }

    /**
     * 完善商品的SKU的其他属性的设置
     *
     * @param goods
     * @param tbItem
     */
    private void setItemPara(Goods goods, TbItem tbItem) {
        //设置商品SPU编号
        tbItem.setGoodsId(goods.getGoods().getId());
        //设置商家编号
        tbItem.setSellerId(goods.getGoods().getSellerId());
        //设置商品分类编号(即goods的3级分类编号)
        tbItem.setCategoryid(goods.getGoods().getCategory3Id());
        //设置创建日期
        tbItem.setCreateTime(new Date());
        //设置修改日期
        tbItem.setUpdateTime(new Date());

        //设置品牌名称
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        tbItem.setBrand(tbBrand.getName());

        //设置分类名称
        TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        tbItem.setCategory(tbItemCat.getName());

        //设置商家名称
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        tbItem.setSeller(tbSeller.getNickName());

        /*
        设置图片地址(goodsDesc.itemImage的第一张图片)

        "goodsDesc": {
        		"itemImages": [{
        			"color": "爱啥啥",
        			"url": "http://192.168.25.133/group1/M00/00/00/wKgZhV1o9VCAEoCDAABv1rEl6pk557.jpg"
        		}, {
        			"color": "小白",
        			"url": "http://192.168.25.133/group1/M00/00/00/wKgZhV1o9bOAJzSBAAAxMTN9zcE134.jpg"
       		 }],...}
        */
        //先将goodsDesc.itemImage转为List集合
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList != null && imageList.size() > 0) {
            //将集合中第一张图的url属性转为字符串, 赋值给tbItem的图片地址
            tbItem.setImage((String) imageList.get(0).get("url"));
        }
    }

    /**
     * 在数据库中插入SKU列表
     *
     * @param goods
     */
    private void insertItemList(Goods goods) {
        //若启用规格
        if ("1".equals(goods.getGoods().getIsEnableSpec()) || goods.getGoods().getIsEnableSpec() == null) {

            //从组合中获取itemList对象, 补齐商品的SKU属性值, 保存到数据库
            List<TbItem> itemList = goods.getItemList();
            /*
            "itemList": [{
             		"spec": {
             			"网络": "双卡",
             			"机身内存": "64G"
             		},
             		"price": 0,
             		"num": 99999,
             		"status": "0",
             		"isDefault": "0"
             	}, ...]

             */
            if (itemList != null && itemList.size() > 0) {
                for (TbItem tbItem : itemList) {
                    //设置标题: 商品KPU+规格描述串
                    String title = goods.getGoods().getGoodsName();
                    //获取TbItem的spec属性, 目前为Object格式, 将其转换为Map格式
                    Map<String, Object> specMap = JSON.parseObject(tbItem.getSpec());
                    if (specMap != null && specMap.size() > 0) {
                        Collection<Object> values = specMap.values();
                        for (Object value : values) {
                            title += " " + value;
                        }
                    }
                    tbItem.setTitle(title);
                    //调用方法完善商品的SKU
                    setItemPara(goods, tbItem);
                    //数据添加到数据库
                    itemMapper.insert(tbItem);
                }

            }
        } else {
            TbItem tbItem = new TbItem();
            //商品KPU+规格描述串作为SKU名称, 此时规格描述为空
            tbItem.setTitle(goods.getGoods().getGoodsName());
            //设置价格
            tbItem.setPrice(goods.getGoods().getPrice());
            //设置状态
            tbItem.setStatus("1");
            //设置是否默认
            tbItem.setIsDefault("1");
            //设置库存默认为0
            tbItem.setNum(0);
            //为保证数据库中数据格式一致, 故赋值为空对象的字符串
            tbItem.setSpec("{}");
            //调用方法完善商品的SKU
            setItemPara(goods, tbItem);
            //数据添加到数据库
            itemMapper.insert(tbItem);
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //设置未申请状态: 如果是经过修改的商品, 需要重新设置状态
        goods.getGoods().setAuditStatus("0");
        goodsMapper.updateByPrimaryKey(goods.getGoods());
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
        //先删除原SKU列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);
        //保存SKU列表到数据库中
        insertItemList(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //查询商品基本信息
        goods.setGoods(goodsMapper.selectByPrimaryKey(id));
        //查询商品扩展信息
        goods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
        TbItemExample example = new TbItemExample();
        //查询商品的SKU信息
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        goods.setItemList(tbItems);
        return goods;
    }

    /**
     * 批量删除(修改商品的IsDelete字段内容: 1为删除, null为未删除)
     */
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                //先查询之前的数据, 设置下架字符后, 更新数据库中的数据
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
                tbGoods.setIsDelete("1");
                goodsMapper.updateByPrimaryKey(tbGoods);
            }
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        //筛选未被逻辑删除的商品
        criteria.andIsDeleteIsNull();
        //筛选未被下架的商品
        criteria.andIsMarketableIsNull();
        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //商家id只能采用精确匹配查询, 避免查到别家的商品信息
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                //商品状态采用精确匹配查询, 避免影响性能
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量审核(修改商品的状态信息: 通过或驳回)
     *
     * @param selectIds
     * @param status
     */
    public void updateStatus(Long[] selectIds, String status) {
        if (selectIds != null && selectIds.length > 0) {
            for (Long selectId : selectIds) {
                //先查询之前的数据, 修改状态后, 更新数据库中的数据
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(selectId);
                tbGoods.setAuditStatus(status);
                goodsMapper.updateByPrimaryKey(tbGoods);
            }
        }
    }

    /**
     * 批量下架(修改商品的IsMarketable字段内容: null为在售, 1为下架)
     *
     * @param selectIds
     */
    @Override
    public void disableSelected(Long[] selectIds) {
        if (selectIds != null && selectIds.length > 0) {
            for (Long selectId : selectIds) {
                //先查询之前的数据, 设置下架字符后, 更新数据库中的数据
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(selectId);
                tbGoods.setIsMarketable("1");
                goodsMapper.updateByPrimaryKey(tbGoods);
            }
        }
    }

    /**
     * 根据商品id和SKU状态查询SKU列表
     *
     * @param selectIds
     * @param status
     * @return
     */
    @Override
    public List<TbItem> findItemListByGoodsIdAndStatus(Long[] selectIds, String status) {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo(status);
        criteria.andGoodsIdIn(Arrays.asList(selectIds));
        return itemMapper.selectByExample(example);
    }


}
