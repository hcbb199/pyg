//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    };

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = itemCatService.update($scope.entity); //修改
        } else {
            //因点击"新增"时将当前entity对象置为空, 故需将parentId赋值给此entity对象的parentId属性, 以完成添加
            //$scope.entity.parentId = $scope.parentId;
            serviceObject = itemCatService.add($scope.entity);//增加
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


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        itemCatService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    /*$scope.search=function(page,rows){
        itemCatService.search(page,rows,$scope.searchEntity).success(
            function(response){
                $scope.list=response.rows;
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    };*/
    //定义根据parentId查询itemCat列表
    $scope.parentId = 0;
    $scope.setParentId = function (value) {
        $scope.parentId = value;
    };
    $scope.search = function (page, rows) {
        itemCatService.findPageByParentId($scope.parentId, page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };
    //设置面包屑显示效果的方法
    $scope.grand = 1;
    $scope.setGrand = function (value) {
        $scope.grand = value;
    };

    $scope.findByGrandId = function (grand, p_entity) {
        //若当前处于1级目录下
        if (grand == 1) {
            $scope.entity_1 = null;
            $scope.entity_2 = null;
            //若当前处于2级目录下
        } else if (grand == 2) {
            $scope.entity_1 = p_entity;
            $scope.entity_2 = null;
            //若当前处于3级目录下
        } else if (grand == 3) {
            $scope.entity_2 = p_entity;
        }
        //将p_entity即entity的id设置给ParentId
        $scope.setParentId(p_entity.id);
        //重新加载页面, 即调用$scope.search(page,rows)方法;
        //也就是调用itemCatService.findPageByParentId($scope.parentId,page,rows)实现按parentId查询
        $scope.reloadList();
    };
    $scope.getNewEntity = function (grand,entity_1,entity_2) {
        if (grand == 1) {
            $scope.entity = {parentId: 0};
            //若当前处于2级目录下
        } else if (grand == 2) {
            $scope.entity = {parentId: entity_1.id};
            //若当前处于3级目录下
        } else if (grand == 3) {
            $scope.entity = {parentId: entity_2.id};
        }
    }


});	
