package cn.neteast.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import cn.neteast.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.neteast.mapper.TbItemCatMapper;
import cn.neteast.pojo.TbItemCat;
import cn.neteast.pojo.TbItemCatExample;
import cn.neteast.pojo.TbItemCatExample.Criteria;
import cn.neteast.sellergoods.service.ItemCatService;

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
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbItemCat> findAll() {
        return itemCatMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbItemCat itemCat) {
        itemCatMapper.insert(itemCat);
        //删除缓存数据
        redisTemplate.delete("itemCat");
    }


    /**
     * 修改
     */
    @Override
    public void update(TbItemCat itemCat) {
        itemCatMapper.updateByPrimaryKey(itemCat);
        //删除缓存数据
        redisTemplate.delete("itemCat");
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbItemCat findOne(Long id) {
        return itemCatMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                //递归删除
                deleteByDG(id);
                //删除缓存数据
                redisTemplate.delete("itemCat");
            }
        }
    }


    @Override
    public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbItemCatExample example = new TbItemCatExample();
        Criteria criteria = example.createCriteria();

        if (itemCat != null) {
            if (itemCat.getName() != null && itemCat.getName().length() > 0) {
                criteria.andNameLike("%" + itemCat.getName() + "%");
            }

        }

        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据父级id分页查询列表
     *
     * @param parentId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPageByParentId(Long parentId, Integer pageNum, Integer pageSize) {

        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
            TbItemCatExample example = new TbItemCatExample();
            Criteria criteria = example.createCriteria();
            criteria.andParentIdEqualTo(parentId);
            Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(example);
            //因每次执行增删改后都要执行此方法, 故每次执行查询的时候, 一次性读取缓存进行存储
            List<TbItemCat> tbItemCatList = findAll();
            if (tbItemCatList != null && tbItemCatList.size() > 0) {
                for (TbItemCat tbItemCat : tbItemCatList) {
                    redisTemplate.boundHashOps("itemCat").put(tbItemCat.getName(),tbItemCat.getTypeId());
                }
            }
            Map itemCat = redisTemplate.boundHashOps("itemCat").entries();
            System.out.println("itemCat: " + itemCat);
            return new PageResult(page.getTotal(), page.getResult());
        }
        return null;
    }

    /**
     * 根据父级id查询列表
     * @param parentId
     * @return
     */
    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
            TbItemCatExample example = new TbItemCatExample();
            Criteria criteria = example.createCriteria();
            criteria.andParentIdEqualTo(parentId);
            return itemCatMapper.selectByExample(example);
    }

    /**
     * 根据查询子节点的集合(用于递归删除)
     *
     * @param parentId
     * @return
     */
    public List<TbItemCat> findListByParentId(Long parentId) {
        TbItemCatExample example = new TbItemCatExample();
        Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        return itemCatMapper.selectByExample(example);
    }

    /**
     * 递归删除
     *
     * @param id
     */
    public void deleteByDG(Long id) {
        List<TbItemCat> listByParentId = findListByParentId(id);
        if (listByParentId != null && listByParentId.size() > 0) {
            for (TbItemCat tbItemCat : listByParentId) {
                deleteByDG(tbItemCat.getId());
            }
            itemCatMapper.deleteByPrimaryKey(id);
        } else {
            itemCatMapper.deleteByPrimaryKey(id);
        }
    }

}
