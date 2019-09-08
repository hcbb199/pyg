package cn.neteast.sellergoods.service;

import cn.neteast.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 查询全部的品牌列表(不使用分页和条件查询)
     *
     * @return
     */
    List<TbBrand> findAll() throws Exception;

    /**
     * 分页查询所有品牌列表
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    PageResult<TbBrand> findPage(Integer currentPage, Integer pageSize) throws Exception;

    /**
     * 添加新的品牌信息
     *
     * @param brand
     */
    void add(TbBrand brand);

    /**
     * 根据品牌名称查询品牌是否已添加
     *
     * @param brandName
     * @return
     */
    List<TbBrand> findByBrandName(String brandName) throws Exception;

    /**
     * 根据id查询品牌详情(用于修改时回显数据)
     *
     * @param id
     * @return
     */
    TbBrand findById(Long id) throws Exception;

    /**
     * 更新品牌详细信息
     *
     * @param brand
     */
    void updateBrand(TbBrand brand);

    /**
     * 批量删除: 根据id删除选中的品牌们
     *
     * @param ids
     */
    void deleteSelected(Long[] ids);

    /**
     * 根据条件分页(模糊)查询品牌
     *
     * @param brand
     * @param currentPage
     * @param pageSize
     * @return
     */
    PageResult<TbBrand> findPageByConditions(TbBrand brand, Integer currentPage, Integer pageSize);

    /**
     * 查询brand并将结果封装成Map集合(品牌下拉框数据)
     *
     * @return
     */
    List<Map> selectOptionList();
}


