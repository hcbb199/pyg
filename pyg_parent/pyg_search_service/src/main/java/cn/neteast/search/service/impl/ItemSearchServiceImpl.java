package cn.neteast.search.service.impl;

import cn.neteast.pojo.TbItem;
import cn.neteast.search.service.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.*;

/**
 * 因dubbo默认调用时长为1000, 故设置超时报警时长为5000ms
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据条件查询
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        //1.按关键字查询(高亮显示), 将方法的查询结果追加到原始map中
        map.putAll(searchList(searchMap));
        //2.根据关键字查询商品分类
        List<String> categoryList = searchCategoryList(searchMap);
        if (categoryList != null && categoryList.size() > 0) {
            map.put("categoryList", categoryList);
        }

        //3.根据分类名称查询redis数据库中的品牌及规格列表
        if (!"".equals(searchMap.get("category"))) {
            //若searchMap中有分类名称, 则根据给定的名称查询
            map.putAll(searchBrandAndSpecList((String)searchMap.get("category")));
        } else {
            //若searchMap中没有分类名称, 则根据分类列表的第一项查询
            if (categoryList != null && categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    /**
     * 导入新审核通过商品对应的SKU列表
     * @param list
     */
    @Override
    public void importList(List<TbItem> list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 商品被删除后同步redis的SKU列表
     * @param goodIdList
     */
    @Override
    public void deleteByGoodIdList(List<Long> goodIdList) {
        if (goodIdList != null && goodIdList.size() > 0) {
            Query query = new SimpleQuery();
            Criteria criteria = new Criteria("item_goodsid").in(goodIdList);
            query.addCriteria(criteria);
            solrTemplate.delete(query);
            solrTemplate.commit();
        }
    }


    /**
     * 根据输入框输入数据查询(最基础的查询)
     * @param searchMap
     * @return
     */
    private Map<String, Object> searchBaseList(Map searchMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        Query query = new SimpleQuery();
        //添加查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows", page.getContent());
        return map;
    }
    /**
     * 根据关键字搜索列表
     * @param searchMap
     * @return
     */
    private Map<String, Object> searchList(Map searchMap) {
        Map<String, Object> map = new HashMap<String, Object>();
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮的域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //设置高亮前缀
        highlightOptions.setSimplePrefix("<span style='color:red'>");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</span>");
        //设置高亮选项
        query.setHighlightOptions(highlightOptions);
        //1. 根据关键字查询
        //替换查询关键字中的空格
        String keywords = (String) searchMap.get("keywords");
        keywords = keywords.replace(" ", "");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);
        //2. 按分类筛选
        if (!"".equals(searchMap.get("category"))) {
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery) ;
        }
        //3. 按品牌筛选
        if (!"".equals(searchMap.get("brand"))) {
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //4. 过滤规格
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //5. 按价格筛选
        if (!"".equals(searchMap.get("price"))) {
            String[] priceStr = ((String) searchMap.get("price")).split("-");
            if (!"0".equals(priceStr[0])) {
                //如果最低价格区间不等于0
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(priceStr[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!"*".equals(priceStr[1])) {
                //如果最高价格区间不是*
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(priceStr[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //6. 分页查询
        //获取当前页数
        Integer pageNum = (Integer) searchMap.get("pageNum");
        //获取每页显示条数
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageNum == null) {
            pageNum = 1;

        }
        if (pageSize == null) {
            pageSize = 20;
        }
        query.setOffset((pageNum - 1) * pageSize);
        query.setRows(pageSize);
        //7. 排序
        //排序方式(顺序or倒序)
        String sortValue = (String) searchMap.get("sort");
        //排序字段
        String sortField = (String) searchMap.get("sortField");
        if (sortValue != null && !"".equals(sortValue)) {
            if ("ASC".equals(sortValue)) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if ("DESC".equals(sortValue)) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }

        //高亮显示处理
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        /*
        {
        "highlighting": {
            "1082433": {
              "item_title": [
                "乐视TV（Letv） Max70 70英寸3D智能LED液晶 超级电视(标配挂件，含48<em>个</em>月乐视网服务费）"
              ]
            },...}
         }
         */
        if (page.getHighlighted() != null && page.getHighlighted().size() > 0) {
            //System.out.println("page: " + page.toString());
            //page: Page 1 of 37 containing cn.neteast.pojo.TbItem instances
            //System.out.println("page.getHighlighted(): "+ page.getHighlighted().toString());
            /*
            page.getHighlighted():
            [org.springframework.data.solr.core.query.result.HighlightEntry@674287c1, org.springframework.data.solr.core.query.result.HighlightEntry@20f91df2,
            org.springframework.data.solr.core.query.result.HighlightEntry@5ea52100, org.springframework.data.solr.core.query.result.HighlightEntry@34e7879a,
            org.springframework.data.solr.core.query.result.HighlightEntry@47686248, org.springframework.data.solr.core.query.result.HighlightEntry@2f96f1da,
            org.springframework.data.solr.core.query.result.HighlightEntry@584c6809, org.springframework.data.solr.core.query.result.HighlightEntry@501fffcd,
            org.springframework.data.solr.core.query.result.HighlightEntry@5461ada4, org.springframework.data.solr.core.query.result.HighlightEntry@4c619ed3,
            org.springframework.data.solr.core.query.result.HighlightEntry@77d6bb76, org.springframework.data.solr.core.query.result.HighlightEntry@5898d3d6,
            org.springframework.data.solr.core.query.result.HighlightEntry@1cd59ec, org.springframework.data.solr.core.query.result.HighlightEntry@411ac800,
            org.springframework.data.solr.core.query.result.HighlightEntry@355c65e, org.springframework.data.solr.core.query.result.HighlightEntry@1dcda381,
            org.springframework.data.solr.core.query.result.HighlightEntry@76a48bed, org.springframework.data.solr.core.query.result.HighlightEntry@616907f1,
            org.springframework.data.solr.core.query.result.HighlightEntry@4ca35ab4, org.springframework.data.solr.core.query.result.HighlightEntry@3c82390a]
             */
            //循环高亮入口集合: tbItemHighlightEntry为单个高亮对象
            for (HighlightEntry<TbItem> tbItemHighlightEntry : page.getHighlighted()) {
                //获取每个对象
                TbItem item = tbItemHighlightEntry.getEntity();
                //System.out.println("tbItemHighlightEntry.getEntity: " + item.toString());
                /*
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@6eab2694
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@47d4e23f
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@1174768b
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@3c1357c6
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@b3b4956
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@6b9e8714
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@59ec74c2
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@7fe7b55e
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@2ec7220d
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@317eb6e6
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@b0e8e6f
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@49819715
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@5ec10362
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@1a4c57da
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@65cecf0a
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@20b2c599
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@76be08fe
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@eb46374
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@1d114b37
                tbItemHighlightEntry.getEntity: cn.neteast.pojo.TbItem@2b39db01
                 */
                if (tbItemHighlightEntry.getHighlights() != null && tbItemHighlightEntry.getHighlights().size() > 0) {
                    //设置高亮的结果:
                    // tbItemHighlightEntry.getHighlights().get(0)为单个对象的所有高亮集合的第一个元素
                    // tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0)为高亮集合第一个元素的第一行
                    item.setTitle(tbItemHighlightEntry.getHighlights().get(0).getSnipplets().get(0));


                }
            }
        }
        map.put("rows", page.getContent());
        //System.out.println("page.getContent(): " + page.getContent().toString());
        /*
        page.getContent():
        [cn.neteast.pojo.TbItem@6eab2694, cn.neteast.pojo.TbItem@47d4e23f, cn.neteast.pojo.TbItem@1174768b, cn.neteast.pojo.TbItem@3c1357c6,
        cn.neteast.pojo.TbItem@b3b4956, cn.neteast.pojo.TbItem@6b9e8714, cn.neteast.pojo.TbItem@59ec74c2, cn.neteast.pojo.TbItem@7fe7b55e,
        cn.neteast.pojo.TbItem@2ec7220d, cn.neteast.pojo.TbItem@317eb6e6, cn.neteast.pojo.TbItem@b0e8e6f, cn.neteast.pojo.TbItem@49819715,
        cn.neteast.pojo.TbItem@5ec10362, cn.neteast.pojo.TbItem@1a4c57da, cn.neteast.pojo.TbItem@65cecf0a, cn.neteast.pojo.TbItem@20b2c599,
        cn.neteast.pojo.TbItem@76be08fe, cn.neteast.pojo.TbItem@eb46374, cn.neteast.pojo.TbItem@1d114b37, cn.neteast.pojo.TbItem@2b39db01]

         */
        //返回总记录数
        map.put("total", page.getTotalElements());
        //返回总条数
        map.put("totalPages",page.getTotalPages());
        return map;
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap) {
        List<String> categoryList = new ArrayList<String>();
        Query query = new SimpleQuery();
        //替换查询关键字中的空格
        String keywords = (String) searchMap.get("keywords");
        keywords = keywords.replace(" ", "");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //System.out.println("page: " + page.toString());
        //page: Page 1 of 1 containing UNKNOWN instances
        //根据列得到分组集合
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //System.out.println("groupResult: " + groupResult.toString());
        //groupResult: SimpleGroupResult [name=item_category, matches=148, groupsCount=null, groupsEntries.total=2]
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //System.out.println("groupEntries: " + groupEntries.toString());
        //groupEntries: Page 1 of 1 containing org.springframework.data.solr.core.query.result.SimpleGroupEntry instances
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        //System.out.println("content: " + content.toString());
        //content: [SimpleGroupEntry [groupValue=手机, result=Page 1 of 1 containing cn.neteast.pojo.TbItem instances],
        // SimpleGroupEntry [groupValue=平板电视, result=Page 1 of 1 containing cn.neteast.pojo.TbItem instances]]
        if (content != null && content.size() > 0) {
            for (GroupEntry<TbItem> entry : content) {
                //System.out.println("entry: " + entry.toString());
                //System.out.println("entry.getGroupValue(): "+entry.getGroupValue());
                //entry: SimpleGroupEntry [groupValue=手机, result=Page 1 of 1 containing cn.neteast.pojo.TbItem instances]
                //将分组结果的名称封装到返回值中
                categoryList.add(entry.getGroupValue());
            }
        }
        return categoryList;
    }

    /**
     * 根据分类名称查询redis数据库中的品牌及规格列表
     * @param category
     * @return
     */
    private Map<String, Object> searchBrandAndSpecList(String category) {
        Map<String, Object> map = new HashMap<String, Object>();
        //根据分类名称查询对应的模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            //根据模板id查询品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);
            //根据模板id查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }
}
