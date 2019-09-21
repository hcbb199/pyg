app.controller("payController", function ($scope, $location, payService) {
    //生成二维码
    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                $scope.money = (response.total_fee / 100).toFixed(2);//总金额
                $scope.out_trade_no = response.out_trade_no;//订单号
                //二维码
                var qr = new QRious({
                    element: document.getElementById("qrious"),
                    size: 250,
                    level: "H",
                    value: response.code_url
                });

                queryPayStatus(response.out_trade_no);//查询支付状态
            }
        )
    };

    //定义一个变量, 用于在二维码过期后提醒用户刷新页面重新获取二维码
    $scope.reloadHtml = false;

    //查询支付状态
    var queryPayStatus = function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(
            function (response) {
                if (response.success) {
                    location.href = "paysuccess.html#?money=" + $scope.money;
                } else {
                    if (response.message == "二维码超时") {
                        $scope.reloadHtml = true; //提示用户重新生成二维码
                    } else {
                        location.href = "payfail.html";
                    }
                }
            }
        )
    };

    //支付成功页面获取金额
    $scope.getMoney = function () {
        $scope.paiedMoney = $location.search()["money"];
    }


});