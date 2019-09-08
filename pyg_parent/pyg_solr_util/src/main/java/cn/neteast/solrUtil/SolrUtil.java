package cn.neteast.solrUtil;

import cn.neteast.mapper.TbItemMapper;
import cn.neteast.pojo.TbItem;
import cn.neteast.pojo.TbItemExample;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入商品SKU列表数据
     */
    public void importItemData() {
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //查询状态值为1的值
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);
        //System.out.println(itemList);
        //将动态域字段赋值
        if (itemList != null && itemList.size() > 0) {
            for (TbItem tbItem : itemList) {
                //将spec字段的json字符串转为map
                Map specMap = JSON.parseObject(tbItem.getSpec(), Map.class);
                tbItem.setSpecMap(specMap);
            }
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();

    }

    /**
     * 删除导入的商品SKU列表数据
     */
    public void deleteItem() {
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
