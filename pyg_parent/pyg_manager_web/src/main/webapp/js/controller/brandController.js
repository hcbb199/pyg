//定义控制器
app.controller("brandController", function ($scope, $controller, brandService) {
    //引入(继承)基础控制器, {$scope:$scope}表示将baseController的参数传递给brandController
    //$controller也是angular提供的一个服务, 可以实现伪继承, 实际上就是与BaseController共享$scope
    $controller("baseController", {$scope: $scope});
    //读取列表数据绑定到表单中
    $scope.findAll = function () {
        brandService.findAll.success(
            function (response) {
                $scope.list = response;
            }
        )
    };

    //分页控件配置
    /*$scope.paginationConf = {
        currentPage: 1, //当前页码
        totalItems: 10, //总条数
        itemsPerPage: 5, //每页显示条数
        perPageOptions: [5, 6, 7, 8, 9, 10], //每页显示条数的页码选项
        //查询加载, 更新页面时触发事件
        onChange: function () {
            $scope.reloadList();
        }
    };*/

    //查询加载列表, 数据
    /*$scope.reloadList = function () {
        //切换页码
        /!*$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);*!/
        $scope.findPageByConditions($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };*/

    //分页查询
    $scope.findPage = function (page, rows) {
        brandService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                //更新总记录数
                $scope.paginationConf.totalItems = response.total;
            }
        );
    };

    //保存新的brand
    $scope.save = function () {
        //判断entity对象的id属性是否为空, 为空的话执行添加方法; 不为空的话执行更新方法
        if ($scope.entity.id == null) {
            brandService.save($scope.entity).success(
                function (response) {
                    if (response.success) {
                        //重新查询, 重新加载
                        $scope.reloadList();
                    } else {
                        alert(response.message);
                    }
                }
            );
        } else {
            brandService.update($scope.entity).success(
                function (response) {
                    if (response.success) {
                        //重新查询, 重新加载
                        $scope.reloadList();
                    } else {
                        alert(response.message);
                    }
                }
            );
        }

    };
    //绑定离焦事件, 查询品牌是否已存在
    $scope.findByBrandName = function () {
        brandService.findByBrandName($scope.entity).success(
            function (response) {
                if (!response.success) {
                    alert(response.message);
                    $scope.entity = {};
                }
            }
        )
    };
    //根据id查询品牌详情(做修改时的数据回显)
    $scope.findById = function (id) {
        brandService.findById(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    };
    //定义一个装载选中的id的集合
    /*$scope.selectedIds = [];
    //定义更新复选框的方法
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            //如果是被选中的, 就将其id值添加到数组中
            $scope.selectedIds.push(id);
        } else {
            //查询此id的索引值
            var indexOf = $scope.selectedIds.indexOf(id);
            //根据索引删除该id
            // splice方法的第一个参数表示要删除的元素的索引, 第二个参数表示要删除的元素的个数
            $scope.selectedIds.splice(indexOf, 1);
        }
    };*/

    //批量删除选中的品牌
    $scope.deleteSelected = function (selectIds) {
        if (selectIds != null && selectIds.length > 0) {
            if (confirm("您确认要删除选中的品牌列表吗?")) {
                brandService.deleteSelected(selectIds).success(
                    function (response) {
                        if (response.success) {
                            $scope.reloadList();
                            $scope.selectIds = [];
                        } else {
                            alert(response.message);
                        }
                    }
                );
            }
        } else {
            alert("您尚未有选中的品牌, 请先选中!");
        }
    };
    //定义条件查询的搜索对象
    $scope.searchConditions = {};
    //根据条件查询品牌
    $scope.search = function (page, rows) {
        brandService.search(page, rows, $scope.searchConditions).success(
            function (response) {
                $scope.list = response.rows;
                //更新总记录数
                $scope.paginationConf.totalItems = response.total;
            }
        );
    };



});
