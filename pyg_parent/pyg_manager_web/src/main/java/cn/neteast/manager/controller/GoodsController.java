package cn.neteast.manager.controller;

import java.util.Arrays;
import java.util.List;

import cn.neteast.page.service.ItemPageService;
import cn.neteast.pojo.TbItem;
import cn.neteast.pojogroup.Goods;
import cn.neteast.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import cn.neteast.pojo.TbGoods;
import cn.neteast.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    @Reference
    private ItemSearchService itemSearchService;
    @Reference(timeout = 5000)
    private ItemPageService itemPageService;

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败!");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败!");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除(修改商品的IsDelete字段内容: 1为删除,null为未删除)
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            itemSearchService.deleteByGoodIdList(Arrays.asList(ids));

            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    /**
     * 批量审核(修改商品的状态信息: 通过或驳回)
     *
     * @param selectIds
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] selectIds, String status) {

        try {
            goodsService.updateStatus(selectIds, status);
            if ("1".equals(status)) {
                List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(selectIds, status);
                if (itemList != null && itemList.size() > 0) {
                    itemSearchService.importList(itemList);
                } else {
                    System.out.println("没有符合条件的SKU列表被添加!");
                }
                //生成静态页面
                if (selectIds != null && selectIds.length > 0) {
                    for (Long goodsId : selectIds) {
                        itemPageService.genItemHtml(goodsId);
                    }
                }
            }
            return new Result(true, "审核成功!");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "审核失败!");
        }
    }

    /**
     * 生成静态页面(测试)
     *
     * @param goodsId
     */
    @RequestMapping("/genHtml")
    public void genItemHtml(Long goodsId) {
        itemPageService.genItemHtml(goodsId);
    }

}
