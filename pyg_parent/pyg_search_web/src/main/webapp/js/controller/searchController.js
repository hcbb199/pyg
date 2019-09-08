app.controller("searchController", function ($scope,$location, searchService) {
    //搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                //返回搜索的结果
                $scope.resultMap = response;
                //在查询后构建分页标签的方法
                buildPageLabel();
            }
        )
    };
    //定义搜索对象
    $scope.searchMap = {
        "keywords": "",
        "category": "",
        "brand": "",
        "spec": {},
        "price": "",
        "pageNum": 1,
        "pageSize": 20,
        "sort":"",
        "sortField":""
    };

    //添加搜索项
    $scope.addSearchItem = function (key, value) {
        //如果点击的是分类或品牌或价格
        if (key == "brand" || key == "category" || key == "price") {
            $scope.searchMap[key] = value;
        } else {
            //如果点击的是spec
            $scope.searchMap.spec[key] = value;
        }

        //执行查询
        $scope.searchMap.pageNum = 1;
        $scope.search();

    };

    //移除搜索项
    $scope.removeSearchItem = function (key) {
        //如果点击的是分类或品牌或价格
        if (key == "brand" || key == "category" || key == "price") {
            $scope.searchMap[key] = "";
        } else {
            //如果点击的是spec
            delete $scope.searchMap.spec[key];
        }
        //执行查询
        $scope.searchMap.pageNum = 1;
        $scope.search();
    };

    //构建分页标签
    var buildPageLabel = function () {
        //分页栏数组
        $scope.pageLabel = [];
        //最大页码
        var maxPageNum = $scope.resultMap.totalPages;
        var firstPage = 1;
        var lastPage = maxPageNum;
        //如果总页数大于5页, 显示部分页码
        if ($scope.resultMap.totalPages > 5) {
            //如果当前页小于等于3
            if ($scope.searchMap.pageNum - 2 < 1) {
                lastPage = 5;

            } else if ($scope.searchMap.pageNum + 2 > maxPageNum) {
                //如果当前页大于等于最大页码-2
                firstPage = maxPageNum - 4;
            } else {
                //显示以当前页为中心的5页
                firstPage = $scope.searchMap.pageNum - 2;
                lastPage = $scope.searchMap.pageNum + 2;
            }
        }
        //循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    };

    //根据页码查询
    $scope.queryByPage = function (pageNum) {
        //页码验证
        if (pageNum < 1 || pageNum > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNum = new Number(pageNum);
        $scope.search();
    };
    //搜索变量初始化方法
    $scope.initSearchMap = function () {
        $scope.searchMap.category = "";
        $scope.searchMap.brand = "";
        $scope.searchMap.spec = {};
        $scope.searchMap.price = "";
        $scope.searchMap.pageNum = 1;
        $scope.searchMap.sort = "";
        $scope.searchMap.sortField = "";
    };
    //排序筛选
    $scope.queryBySort=function (sort, sortField) {
        //排序方式
        $scope.searchMap.sort = sort;
        //排序字段
        $scope.searchMap.sortField = sortField;
        $scope.search();
    };

    //判断关键字是不是品牌
    $scope.keywordsIsOneOfBrand = function () {
        if ($scope.resultMap.brandList != null && $scope.resultMap.brandList.length > 0) {
            for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
                if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) == 0) {
                    //keywords字段中包含品牌
                    return false;
                }
            }
            return true;
        }
        return false;
    };

    //加载查询字符串查询
    $scope.loadKeywords=function () {
        if ($location.search().keywords!=null) {
            $scope.searchMap.keywords = $location.search().keywords ;
            $scope.search();
        }

    }
});


