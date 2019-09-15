//购物车控制层
app.controller('cartController', function ($scope, cartService) {
    //查询购物车列表
    $scope.findCartList = function(){
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                //获取合计金额和合计数量
                $scope.totalValue = cartService.getSumValue($scope.cartList);
            }
        )
    };

    //添加商品到购物车
    $scope.addGoodsToCartList = function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();
                } else {
                    alert(response.message);
                }
            }
        )
    };


});