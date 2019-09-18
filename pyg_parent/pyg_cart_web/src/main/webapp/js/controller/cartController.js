//购物车控制层
app.controller('cartController', function ($scope, cartService) {
    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                //获取合计金额和合计数量
                $scope.totalValue = cartService.getSumValue($scope.cartList);
            }
        );
    };

    //添加商品到购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(
            function (response) {
                if (response.success) {
                    $scope.findCartList();
                } else {
                    alert(response.message);
                }
            }
        );
    };


    //获取登录用户地址列表
    $scope.findListByUserId = function () {
        cartService.findListByUserId().success(
            function (response) {
                $scope.addressList = response;
                //设置默认地址
                if ($scope.addressList != null && $scope.addressList.length > 0) {
                    for (var i = 0; i < $scope.addressList.length; i++) {
                        if ($scope.addressList[i].isDefault == "1") {
                            $scope.selectedAddress = $scope.addressList[i];
                            return;
                        }
                    }
                }
            }
        );
    };

    //修改收获地址
    $scope.selectAddress = function (address) {
        $scope.selectedAddress = address;
    };

    $scope.order = {paymentType: "1"};
    //选择付款方式
    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    };

    //保存订单
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.selectedAddress.address;//地址
        $scope.order.receiverMobile = $scope.selectedAddress.mobile;//手机
        $scope.order.receiver = $scope.selectedAddress.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success) {
                    //页面跳转
                    if ($scope.order.paymentType == "1") {//如果是微信支付，跳转到支付页面
                        alert(response.message);
                        location.href = "pay.html";
                    } else {//如果货到付款，跳转到提示页面
                        location.href = "paysuccess.html";
                    }
                } else {
                    alert(response.message);//也可以跳转到提示页面
                }
            }
        );
    };

});