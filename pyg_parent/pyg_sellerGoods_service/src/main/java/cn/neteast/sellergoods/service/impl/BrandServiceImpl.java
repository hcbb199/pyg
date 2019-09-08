package cn.neteast.sellergoods.service.impl;

import cn.neteast.pojo.TbBrandExample;
import cn.neteast.mapper.TbBrandMapper;
import cn.neteast.pojo.TbBrand;
import cn.neteast.sellergoods.service.BrandService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper tbBrandMapper;

    /**
     * 查询全部的品牌列表(不使用分页和条件查询)
     *
     * @return
     */
    @Override
    public List<TbBrand> findAll() throws Exception {
        return tbBrandMapper.selectByExample(null);
    }

    /**
     * 分页查询所有品牌列表
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<TbBrand> findPage(Integer currentPage, Integer pageSize) throws Exception {
        if (currentPage != null && pageSize != null) {
            //PageHelper为Mybatis分页插件
            PageHelper.startPage(currentPage, pageSize);
            //用Page<TbBrand>接收查询的结果集; 因为翻源码发现Page<TbBrand>是ArrayList<>的一个子类
            Page<TbBrand> pages = (Page<TbBrand>) tbBrandMapper.selectByExample(null);
            //将查询结果的总页数, 总TbBrand结果列表作为PageResult的有参构造的参数
            return new PageResult<TbBrand>(pages.getTotal(), pages.getResult());
        }

        return null;
    }

    /**
     * 插入新的品牌信息
     *
     * @param brand
     */
    @Override
    public void add(TbBrand brand) {
        tbBrandMapper.insert(brand);
    }

    @Override
    public List<TbBrand> findByBrandName(String brandName) throws Exception {
        List<TbBrand> brandList = null;
        try {
            TbBrandExample tbBrandExample = new TbBrandExample();
            TbBrandExample.Criteria criteria = tbBrandExample.createCriteria();
            criteria.andNameEqualTo(brandName);
            brandList = tbBrandMapper.selectByExample(tbBrandExample);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return brandList;
    }

    /**
     * 根据id查询品牌详情(用于修改时的数据回显)
     *
     * @param id
     * @return
     */
    @Override
    public TbBrand findById(Long id) throws Exception {
        return tbBrandMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据id更新品牌详情
     *
     * @param brand
     */
    @Override
    public void updateBrand(TbBrand brand) {
        //此处更新方法出入封装的品牌对象即可, 不必拆出其id值
        tbBrandMapper.updateByPrimaryKey(brand);
    }

    /**
     * 批量删除: 根据id删除选中的品牌们
     *
     * @param ids
     */
    @Override
    public void deleteSelected(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                tbBrandMapper.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 根据条件分页(模糊)查询品牌
     * @param brand
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public PageResult<TbBrand> findPageByConditions(TbBrand brand, Integer currentPage, Integer pageSize) {
        PageHelper.startPage(currentPage, pageSize);
        TbBrandExample tbBrandExample = new TbBrandExample();
        TbBrandExample.Criteria criteria = tbBrandExample.createCriteria();
        if (brand != null && currentPage != null && pageSize != null) {
            if (brand.getName() != null && brand.getName().length() > 0) {
                //添加模糊查询条件
                criteria.andNameLike("%" + brand.getName() + "%");
            }
            if (brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
                //首字母只有单个字符, 故不需要进行模糊查询, 以免降低查询效率
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }
        Page<TbBrand> pages = (Page<TbBrand>) tbBrandMapper.selectByExample(tbBrandExample);
        //将查询结果的总页数, 总TbBrand结果列表作为PageResult的有参构造的参数
        return new PageResult<TbBrand>(pages.getTotal(), pages.getResult());
    }

    /**
     * 查询brand并将结果封装成Map集合(品牌下拉框数据)
     * @return
     */
    @Override
    public List<Map> selectOptionList() {
        return tbBrandMapper.selectOptionList();
    }
}
