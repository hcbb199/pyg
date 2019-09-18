//商品详情页(控制层)
app.controller("itemController", function ($http, $scope) {

    //数量操作
    $scope.changeNum = function (x) {
        $scope.num = $scope.num + x;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    };
    //记录用户选择的规格
    $scope.specificationItems = {};

    /*//用户选择规格
    $scope.selectSpecification = function (key, value) {
        $scope.specificationItems[key] = value;
    };*/

    //判断某规格是否被用户选中
    $scope.isSelected = function (key, value) {
        return $scope.specificationItems[key] == value;
    };

    //加载默认的SKU信息(即查询出的第一条数据)
    $scope.loadSKU = function () {
        $scope.sku = skuList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec))
    };

    //匹配两个对象
    var matchObject = function (map1, map2) {
        //获取两个对象的属性个数
        var length1 = Object.getOwnPropertyNames(map1).length;
        var length2 = Object.getOwnPropertyNames(map2).length;
        if (length1 != length2) {
            return false;
        }
        //获取map1中的所有属性, 一一与map2对比同一个key得到的值是否相同
        for (var key in map1) {
            if (map1[key] != map2[key]) {
                return false;
            }
        }
        return true;
    };

    //查询SKU
    var searchSKU = function () {
        for (var i = 0; i < skuList.length; i++) {
            if (matchObject(skuList[i].spec, $scope.specificationItems)) {
                $scope.sku = skuList[i];
                return;

            }
        }
        //匹配不成功的
        $scope.sku = {id: 0, title: '--------', price: 0};
    };
    //在用户选择后触发该方法
    $scope.selectSpecification = function (key, value) {
        $scope.specificationItems[key] = value;
        //读取SKU
        searchSKU();
    };

    //添加商品到购物车
    /*$scope.addToCart = function () {
        alert("商品数量: " + $scope.num + "  SKU的id: " + $scope.sku.id);
    };*/

    //添加商品到购物车
    $scope.addToCart = function () {

        $http.get("http://localhost:9107/cart/addGoodsToCartList.do?itemId="+ $scope.sku.id + "&num=" + $scope.num, {"withCredentials": true}).success(
            function (response) {
                if (response.success) {
                    location.href = "http://localhost:9107/cart.html";//跳转到购物车页面
                } else {
                    alert(response.message);
                }
            }
        );
    }

});