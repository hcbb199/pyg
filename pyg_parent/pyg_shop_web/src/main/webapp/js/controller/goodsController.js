//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {
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
        //将富文本编辑器中的内容赋值给goodsDesc.introduction
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    //将输入框清空
                    $scope.entity = {};
                    //清空富文本编辑器中的内容
                    editor.html("");
                    //重新查询
                    location.href="goods.html";//跳转到商品列表页
                } else {
                    alert(response.message)
                }
            }
        );
    };


    //批量删除
    $scope.dele = function () {
        if (confirm("您确认要删除选中的品牌列表吗?")) {
            //获取选中的复选框
            goodsService.dele($scope.selectIds).success(
                function (response) {
                    if (response.success) {
                        $scope.reloadList();//刷新列表
                        $scope.selectIds = [];
                    }
                }
            );
        }
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
    //新增商品
    $scope.add = function () {
        //将富文本编辑器中的内容赋值给goodsDesc.introduction
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    //将输入框清空
                    $scope.entity = {};
                    //清空富文本编辑器中的内容
                    editor.html("");
                    //重新查询
                    $scope.reloadList();
                } else {
                    alert(response.message)
                }
            }
        );
    };

    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    //如果上传成功, 取出url, 设置文件地址
                    $scope.image_entity.url = response.message;
                    //alert("上传成功!")
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert("上传发生错误");
        });
    };

    //定义页面实体结构, goodsDesc.itemImages为其上传图片的数组
    //添加goodDesc的specificationItems规格属性数组
    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};

    //添加图片列表
    $scope.add_image_entity = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);

    };
    //列表中移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    };

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
        if(!newValue) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCatSecondList = response;
            }
        );

    });
    //监控二级分类目录的改变, 当其改变时获取三级分类目录
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        if(!newValue) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCatThirdList = response;
            }
        );
    });
    //监控三级目录的改变, 当其改变时获取模板id
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        if(!newValue) {
            return;
        }
        itemCatService.findOne(newValue).success(
            function (response) {
                //将查询结果的typeId属性赋值给goods.typeTemplateId
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        );
    });
    //监控模板id的改变, 当其改变时获取品牌列表和扩展属性列表
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        if(!newValue) {
            return;
        }
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
    });

    //点击checkbox, 更新entity.goodsDesc.specificationItems对象
    $scope.updateSpecAttribute = function (event, key, keyValue) {
        //调用方法查询数组的元素中是否已包含目标属性
        var obj = $scope.searchObjByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', key);

        //判断此时是添加还是移除操作
        if (event.target.checked) {
            //若obj非空, 说明该目标属性已存在于数组的元素中
            if (obj != null) {
                //添加操作
                obj.attributeValue.push(keyValue);
            } else {
                $scope.entity.goodsDesc.specificationItems.push({"attributeName": key, "attributeValue": [keyValue]});
            }
        } else {
            //移除操作
            obj.attributeValue.splice(obj.attributeValue.indexOf(keyValue), 1);
            //若移除规格选项后其规格选项的数组的长度为空, 则移除此条记录
            if (obj.attributeValue.length == 0) {
                $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(obj), 1);
            }
        }
    };

    //创建SKU列表
    $scope.createItemList = function () {
        //定义初始值
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: "0", isDefault: "0"}];
        var items = $scope.entity.goodsDesc.specificationItems;
        //"specificationItems":[{"attributeName":"网络","attributeValue":["电信4G"]},{"attributeName":"机身内存","attributeValue":["64G"]}]
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);

        }
    };
    //添加列值
    var addColumn = function (list, columnName, conlumnValues) {
        //定义新的集合
        var newList = [];
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < conlumnValues.length; j++) {
                //深克隆
                var newRow = JSON.parse(JSON.stringify(oldRow));
                newRow.spec[columnName] = conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
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

    //批量下架(修改商品的IsMarketable字段内容: null为在售, 1为下架)
    $scope.disableSelected = function () {
        goodsService.disableSelected($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.selectIds = [];
                    alert(response.message);
                    $scope.reloadList();
                } else {
                    alert(response.message);
                }
            }
        )

    };
});	
