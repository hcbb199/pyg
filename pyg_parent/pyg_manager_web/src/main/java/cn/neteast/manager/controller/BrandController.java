package cn.neteast.manager.controller;

import cn.neteast.pojo.TbBrand;
import cn.neteast.sellergoods.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 查询全部的品牌列表(不使用分页和条件查询)
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll() throws Exception {
        List<TbBrand> tbBrandList = brandService.findAll();
        return tbBrandList;
    }

    /**
     * 分页查询所有品牌列表
     *
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult<TbBrand> findPages(Integer currentPage, Integer pageSize) throws Exception {
        return brandService.findPage(currentPage, pageSize);
    }

    /**
     * 添加新的品牌信息
     *
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand) {
        try {
            brandService.add(brand);
            return new Result(true, "新增成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "新增失败");
        }

    }

    /**
     * 根据品牌名称查询品牌是否已添加
     *
     * @param brand
     * @return
     */
    @RequestMapping("/findByBrandName")
    public Result findByBrandName(@RequestBody TbBrand brand) throws Exception {
        if (brand == null) {
            return new Result(true, "OK");
        } else {
            List<TbBrand> brandList = brandService.findByBrandName(brand.getName());
            if (brandList != null && brandList.size() != 0) {
                return new Result(false, "此品牌已存在");
            } else {
                return new Result(true, "OK");
            }
        }
    }

    /**
     * 根据id查询品牌详情(用于修改时回显数据)
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("/findById")
    public TbBrand findById(Long id) throws Exception {
        return brandService.findById(id);
    }

    /**
     * 更新品牌详情
     * @param brand
     * @return
     */
    @RequestMapping("/updateBrand")
    public Result updateBrand(@RequestBody TbBrand brand) {
        try {
            brandService.updateBrand(brand);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "更新失败");
        }
    }

    /**
     * 批量删除: 根据id删除品牌们
     * @param ids
     * @return
     */
    @RequestMapping("/deleteSelected")
    public Result deleteSelected(Long[] ids) {
        try {
            brandService.deleteSelected(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 根据条件分页(模糊)查询品牌
     * @param brand
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping("/findPageByConditions")
    public PageResult<TbBrand> findPageByConditions(@RequestBody TbBrand brand, Integer currentPage, Integer pageSize) {
        return brandService.findPageByConditions(brand, currentPage, pageSize);

    }

    /**
     * 查询brand并将结果封装成Map集合(品牌下拉框数据)
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }

}
