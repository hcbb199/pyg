//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, itemCatService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //查询实体
    /*$scope.findOne = function (id) {
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    };*/

    //查询实体
    $scope.findOne = function () {
        //通过$location获取id参数值
        //alert($location.search());
        //$location.search()是一个对象, 可使用.属性名或者['属性名']来获取属性值
        var id = $location.search().id;
        //若id为空, 则跳出此方法
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //回显商品介绍即富文本编辑器的内容
                editor.html($scope.entity.goodsDesc.introduction);
                //回显图片列表
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //回显扩展属性
                //alert($scope.entity.goodsDesc.customAttributeItems);//字符串: [{"text":"内存大小","value":"32G"},{"text":"颜色","value":"粉色"}]
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //alert($scope.entity.goodsDesc.customAttributeItems);//对象: [object Object],[object Object]
                //回显规格选项
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //回显SKU列表
                if ($scope.entity.itemList != null && $scope.entity.itemList.length > 0) {
                    for (var i = 0; i < $scope.entity.itemList.length; i++) {
                        $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                    }
                }


            }
        );
    };


    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    };


    //批量删除(修改商品的IsDelete字段内容: 1为删除, null为未删除)
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                } else {
                    alert(response.message);
                }
            }
        );
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };


    //定义商品状态信息
    $scope.status = ["待审核", "审核通过", "审核未通过", "已关闭"];
    //定义商品分类列表
    $scope.itemCatList = [];
    //获取分类id所对应分类名称
    $scope.getItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
                //alert($scope.itemCatList);
            }
        );
    };

    //定义页面实体结构, goodsDesc.itemImages为其上传图片的数组
    //添加goodDesc的specificationItems规格属性数组
    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};


    //获取一级分类目录
    $scope.getFirstCategoryList = function (id) {
        itemCatService.findByParentId(id).success(
            function (response) {
                $scope.itemCatFirstList = response;
            }
        );
    };
    //监控一级分类目录的改变, 当其改变时获取二级分类目录
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCatSecondList = response;
                }
            );
        }
    });
    //监控二级分类目录的改变, 当其改变时获取三级分类目录
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCatThirdList = response;
                }
            );
        }
    });
    //监控三级目录的改变, 当其改变时获取模板id
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        if (newValue) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    //将查询结果的typeId属性赋值给goods.typeTemplateId
                    $scope.entity.goods.typeTemplateId = response.typeId;
                }
            );
        }
    });
    //监控模板id的改变, 当其改变时获取品牌列表和扩展属性列表
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        if (newValue) {
            typeTemplateService.findOne(newValue).success(
                function (response) {
                    //将查询结果赋值给goods.Template
                    $scope.typeTemplate = response;
                    //将查询结果的品牌lieb从json字符串转为json对象
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                    //因此行代码执行后会覆盖findOne方法查询的扩展属性列表, 故对其执行做条件约束
                    if ($location.search().id == null) {
                        //将扩展属性列表从json字符串转为json对象并赋值给entity.goodsDesc.customAttributeItems属性
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                    }
                }
            );
            //根据规格id查询规格选项
            typeTemplateService.findSpecList(newValue).success(
                function (response) {
                    $scope.specList = response;
                    /*[{
                        "options": [{
                            "id": 98,
                            "optionName": "移动3G",
                            "orders": 1,
                            "specId": 27
                        }, ...],
                        "id": 27,
                        "text": "网络"
                    }, {
                        "options": [{
                            "id": 118,
                            "optionName": "16G",
                            "orders": 1,
                            "specId": 32
                        }, ...],
                        "id": 32,
                        "text": "机身内存"
                    }]*/
                }
            );
        }
    });

    //判断目标规格选项的checkBox是否要勾选
    $scope.checkCheckBox = function (attrName, attrValue) {
        if ($scope.entity.goodsDesc.specificationItems != null && $scope.entity.goodsDesc.specificationItems.length > 0) {
            //调用baseController中的searchObjByKey方法查询该属性名是否存在对应的对象
            var obj = $scope.searchObjByKey($scope.entity.goodsDesc.specificationItems, "attributeName", attrName);
            //specificationItems":[{"attributeValue":["移动4G","联通4G"],"attributeName":"网络"},{"attributeValue":["64G","128G"],"attributeName":"机身内存"}]
            if (obj != null) {
                var indexOf = obj.attributeValue.indexOf(attrValue);
                return indexOf >= 0;
            }
        }
        return false;
    };

    //批量审核(修改商品的状态信息: 通过或驳回)
    $scope.updateStatus = function (status) {
        goodsService.updateStatus($scope.selectIds, status).success(
            function (response) {
                if (response.success) {
                    //审核成功
                    alert(response.message);
                    //清空ID数组
                    $scope.selectIds = [];
                    //重载页面
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        )
    };
});	
