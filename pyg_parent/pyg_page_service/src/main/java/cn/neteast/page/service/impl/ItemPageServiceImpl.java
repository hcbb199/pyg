package cn.neteast.page.service.impl;

import cn.neteast.mapper.TbGoodsDescMapper;
import cn.neteast.mapper.TbGoodsMapper;
import cn.neteast.mapper.TbItemCatMapper;
import cn.neteast.mapper.TbItemMapper;
import cn.neteast.page.service.ItemPageService;
import cn.neteast.pojo.TbGoods;
import cn.neteast.pojo.TbGoodsDesc;
import cn.neteast.pojo.TbItem;
import cn.neteast.pojo.TbItemExample;
//import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.asm.FieldWriter;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pageDir}")
    private String pageDir;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public Boolean genItemHtml(Long goodsId) {
        try {
            //获取configuration对象
            Configuration configuration = freeMarkerConfigurer.getConfiguration();

            //获取template对象
            Template template = configuration.getTemplate("item.ftl");

            //构建数据模型
            Map<String, Object> map = new HashMap<String, Object>();
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods", tbGoods);
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc", tbGoodsDesc);
            //获取商品分类
            String category1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            String category2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            String category3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
            map.put("itemCat1", category1);
            map.put("itemCat2", category2);
            map.put("itemCat3", category3);
            //获取SKU列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            //按照状态降序, 保证第一个为默认
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);

            map.put("itemList", itemList);
            //创建输出流对象
            Writer writer = new FileWriter( pageDir + goodsId + ".html");

            //生成静态页面
            template.process(map, writer);

            //关流
            writer.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
