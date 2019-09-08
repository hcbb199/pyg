package cn.neteast.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import cn.neteast.mapper.TbSpecificationOptionMapper;
import cn.neteast.pojo.TbSpecificationOption;
import cn.neteast.pojo.TbSpecificationOptionExample;
import cn.neteast.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import cn.neteast.mapper.TbSpecificationMapper;
import cn.neteast.pojo.TbSpecification;
import cn.neteast.pojo.TbSpecificationExample;
import cn.neteast.pojo.TbSpecificationExample.Criteria;
import cn.neteast.sellergoods.service.SpecificationService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 增加(组合实体类对象)
     *
     * @param specification
     */
    @Override
    public void add(Specification specification) {
        if (specification != null) {
            if (specification.getSpecification() != null) {
                //插入规格
                specificationMapper.insert(specification.getSpecification());
            }
            if (specification.getSpecificationOptionList() != null && specification.getSpecificationOptionList().size() > 0) {
                //循环插入规格选项
                for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList()) {
                    //设置规格选项的外键id;
                    specificationOption.setSpecId(specification.getSpecification().getId());
                    //新增规格选项
                    specificationOptionMapper.insert(specificationOption);
                }
            }
        }
    }

    /**
     * 修改(接收的参数为组合实体类对象)
     *
     * @param specification
     */
    @Override
    public void update(Specification specification) {
        if (specification != null) {
            if (specification.getSpecification() != null) {
                //更新规格表的数据
                specificationMapper.updateByPrimaryKey(specification.getSpecification());
            }
            //先根据规格的id值, 删除规格列表里该外键对应的规格选项列表
            TbSpecificationOptionExample example = new TbSpecificationOptionExample();
            TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
            criteria.andSpecIdEqualTo(specification.getSpecification().getId());
            specificationOptionMapper.deleteByExample(example);
            //重新添加规格选项列表, 规格对象的id作为规格选项表的外键
            //获取组合实体类对象里的规格选项列表
            List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
            if (specificationOptionList != null && specificationOptionList.size() > 0) {
                //循环插入规格选项
                for (TbSpecificationOption tbSpecificationOption : specificationOptionList) {
                    //设置规格选项是外键id
                    tbSpecificationOption.setSpecId(specification.getSpecification().getId());
                    //在规格选项表中插入新数据
                    specificationOptionMapper.insert(tbSpecificationOption);
                }
            }
        }


    }

    /**
     * 根据ID获取(组合实体类对象)实体对象
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        //根据id查询规格
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
        //根据规格id即规格选项外键查询规格列表
        TbSpecificationOptionExample example = new TbSpecificationOptionExample();
        TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
        criteria.andSpecIdEqualTo(tbSpecification.getId());
        List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(example);
        Specification specification = new Specification(tbSpecification, tbSpecificationOptions);
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        if (ids != null && ids.length > 0) {
            for (Long id : ids) {
                //删除规格
                specificationMapper.deleteByPrimaryKey(id);
                //删除规格选项列表
                TbSpecificationOptionExample example = new TbSpecificationOptionExample();
                TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
                criteria.andSpecIdEqualTo(id);
                specificationOptionMapper.deleteByExample(example);
            }
        }
    }


    /**
     * 根据条件分页查询
     *
     * @param specification
     * @param pageNum       当前页 码
     * @param pageSize      每页记录数
     * @return
     */
    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample();
        Criteria criteria = example.createCriteria();

        if (specification != null) {
            if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
                criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 查询specification并将结果封装成Map集合(规格下拉框数据)
     * @return
     */
    @Override
    public List<Map> selectSpecList() {
        return specificationMapper.selectSpecList();
    }

}
