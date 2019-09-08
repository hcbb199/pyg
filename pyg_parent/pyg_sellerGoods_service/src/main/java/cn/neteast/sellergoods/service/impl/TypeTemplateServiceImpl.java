package cn.neteast.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import cn.neteast.mapper.TbSpecificationOptionMapper;
import cn.neteast.pojo.*;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.neteast.mapper.TbTypeTemplateMapper;
import cn.neteast.pojo.TbTypeTemplateExample.Criteria;
import cn.neteast.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert(typeTemplate);
        //删除缓存数据
        redisTemplate.delete("brandList");
        redisTemplate.delete("specLit");
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey(typeTemplate);
        //删除缓存数据
        redisTemplate.delete("brandList");
        redisTemplate.delete("specLit");
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        //System.out.println("tbTypeTemplate:" + tbTypeTemplate);
        return tbTypeTemplate;


    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey(id);
            //删除缓存数据
            redisTemplate.delete("brandList");
            redisTemplate.delete("specLit");
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample();
        Criteria criteria = example.createCriteria();

        if (typeTemplate != null) {
            if (typeTemplate.getName() != null && typeTemplate.getName().length() > 0) {
                criteria.andNameLike("%" + typeTemplate.getName() + "%");
            }
            if (typeTemplate.getSpecIds() != null && typeTemplate.getSpecIds().length() > 0) {
                criteria.andSpecIdsLike("%" + typeTemplate.getSpecIds() + "%");
            }
            if (typeTemplate.getBrandIds() != null && typeTemplate.getBrandIds().length() > 0) {
                criteria.andBrandIdsLike("%" + typeTemplate.getBrandIds() + "%");
            }
            if (typeTemplate.getCustomAttributeItems() != null && typeTemplate.getCustomAttributeItems().length() > 0) {
                criteria.andCustomAttributeItemsLike("%" + typeTemplate.getCustomAttributeItems() + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(example);
        saveToRedis();
        Map brandList = redisTemplate.boundHashOps("brandList").entries();
        System.out.println("brandList: " + brandList);
        Map specLit = redisTemplate.boundHashOps("specList").entries();
        System.out.println("specList: " + specLit);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据外键查询规格选项列表
     *
     * @param specId
     * @return
     */
    @Override
    public List<Map> findSpecList(Long specId) {
        //根据主键查找模板对象
        TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(specId);
        //[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        //将以上模板对象的规格属性从字符串转为List<Map>集合
        List<Map> list = JSON.parseArray(tbTypeTemplate.getSpecIds(), Map.class);
        if (list != null && list.size() > 0) {
            for (Map map : list) {
                //根据外键查询规格选项列表
                TbSpecificationOptionExample example = new TbSpecificationOptionExample();
                TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
                //获取map.get("id")数据类型
                //String typeStr = map.get("id").getClass().toString();
                //System.out.println("id的数据类型是: " + typeStr);
                //输出结果: "id的数据类型是: class java.lang.Integer"
                //map.get("id")此时是object类型, 需先强转成Integer, 再转为Long
                criteria.andSpecIdEqualTo(new Long((Integer) map.get("id")));
                List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
                map.put("options", options);
            }
        }
        return list;
    }

    /**
     * 将品牌和规格数据存入redis缓存
     */
    private void saveToRedis() {
        //获取模板数据
        List<TbTypeTemplate> tbTypeTemplateList = findAll();
        if (tbTypeTemplateList != null && tbTypeTemplateList.size() > 0) {
            //循环模板数据
            for (TbTypeTemplate tbTypeTemplate : tbTypeTemplateList) {
                //存储品牌列表
                List<Map> brandList = JSON.parseArray(tbTypeTemplate.getBrandIds(), Map.class);
                //将品牌列表加入到redis缓存中
                redisTemplate.boundHashOps("brandList").put(tbTypeTemplate.getId(), brandList);
                //存储规格列表
                //根据模板id查询规格列表
                List<Map> specList = findSpecList(tbTypeTemplate.getId());
                //将规格及规格选项列表加入到redis缓存中
                redisTemplate.boundHashOps("specList").put(tbTypeTemplate.getId(), specList);

            }
        }
    }

}
