package cn.neteast.content.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.neteast.mapper.TbContentMapper;
import cn.neteast.pojo.TbContent;
import cn.neteast.pojo.TbContentExample;
import cn.neteast.pojo.TbContentExample.Criteria;
import cn.neteast.content.service.ContentService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        contentMapper.insert(content);
        //删除添加的categoryId对应的缓存数据
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());

    }

    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {
        //获取修改前及修改后的categoryId, 分别删除其所对应的缓存
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        contentMapper.updateByPrimaryKey(content);
        //删除修改前的categoryId对应的缓存数据
        redisTemplate.boundHashOps("content").delete(categoryId);
        if (categoryId.longValue() != content.getCategoryId().longValue()) {
            //若修改前后该元素的categoryId不相同, 则再次删除修改后的categoryId对应的缓存数据
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //根据要删除的id查询其保存在数据库中的categoryId
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            contentMapper.deleteByPrimaryKey(id);
            //删除已被删除的元素的categoryId对应的缓存数据
            redisTemplate.boundHashOps("content").delete(categoryId);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();

        if (content != null) {
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andTitleLike("%" + content.getTitle() + "%");
            }
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andUrlLike("%" + content.getUrl() + "%");
            }
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andPicLike("%" + content.getPic() + "%");
            }
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andStatusLike("%" + content.getStatus() + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据广告类型id查询广告列表
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        //先查询缓存, 若缓存中查不出数据则在查询数据库, 并将查询结果保存至缓存中
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (contentList == null) {
            System.out.println("从数据库中查询...");
            TbContentExample example = new TbContentExample();
            Criteria criteria = example.createCriteria();
            criteria.andCategoryIdEqualTo(categoryId);
            //查询开启状态的广告
            criteria.andStatusEqualTo("1");
            //查询结果按照升序排列
            example.setOrderByClause("sort_order");
            contentList = contentMapper.selectByExample(example);
            redisTemplate.boundHashOps("content").put(categoryId, contentList);
        } else {
            System.out.println("从缓存中查询...");
        }

        return contentList;
    }
}
