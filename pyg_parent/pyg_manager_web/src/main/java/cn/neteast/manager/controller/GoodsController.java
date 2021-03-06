package cn.neteast.manager.controller;

import java.util.List;

//import cn.neteast.page.service.ItemPageService;
import cn.neteast.pojo.TbItem;
import cn.neteast.pojogroup.Goods;
//import cn.neteast.search.service.ItemSearchService;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import cn.neteast.pojo.TbGoods;
import cn.neteast.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

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
    //@Reference
    //private ItemSearchService itemSearchService;
    //@Reference(timeout = 5000)
    //private ItemPageService itemPageService;
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue queueSolrDestination;

    @Autowired
    private ActiveMQQueue queueSolrDeleteDestination;

    @Autowired
    private ActiveMQTopic topicPageDestination;

    @Autowired
    private ActiveMQTopic topicPageDeleteDestination;

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
            //itemSearchService.deleteByGoodIdList(Arrays.asList(ids));
            //删除solr索引库中的SKU列表
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            //删除静态页面
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
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
                    //itemSearchService.importList(itemList);
                    //添加SKU列表到solr索引库, 把查出来的SKU列表转为JSON字符串, 使用TextMessage发送出去
                    String itemListStr = JSON.toJSONString(itemList);
                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(itemListStr);
                        }
                    });
                    //生成静态页面
                    //itemPageService.genItemHtml(goodsId);
                    jmsTemplate.send(topicPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createObjectMessage(selectIds);
                        }
                    });
                } else {
                    System.out.println("没有符合条件的SKU列表被添加!");
                    System.out.println("没有符合条件的静态页面被生成!");
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
     *//*
    @RequestMapping("/genHtml")
    public void genItemHtml(Long goodsId) {
        itemPageService.genItemHtml(goodsId);
    }*/

}
