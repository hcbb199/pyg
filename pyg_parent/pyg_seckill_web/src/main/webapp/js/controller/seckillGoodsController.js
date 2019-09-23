//秒杀控制层
app.controller('seckillGoodsController', function ($scope, $location, $interval, seckillGoodsService) {
    //读取列表数据绑定到表单中
    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {
                $scope.list = response;
            }
        )
    };

    $scope.findOneFromRedis = function () {
        seckillGoodsService.findOneFromRedis($location.search()["id"]).success(
            function (response) {
                $scope.entity = response;
                allSecond = Math.floor((new Date($scope.entity.endTime).getTime() - (new Date().getTime())) / 1000);
                time = $interval(function () {
                    if (allSecond > 0) {
                        allSecond--;
                        $scope.timeString = convertTimeString(allSecond);
                    } else {
                        $interval.cancel(time);
                        alert("秒杀已结束!");
                    }

                }, 1000);
            }
        )
    };

    //转换秒为   天小时分钟秒格式  XXX天 10:22:33
    convertTimeString = function (allSecond) {
        var days = Math.floor(allSecond / 864000);
        var hours = Math.floor((allSecond % 86400) / 3600);
        var minutes = Math.floor((allSecond % 3600) / 60);
        var seconds = Math.floor(allSecond % 60);
        var timeStr = "";
        if (days > 0) {
            timeStr = days + " 天 ";
        }
        return timeStr + hours + " 小时 " + minutes + " 分钟 " + seconds + " 秒";
    };

    //提交订单
    $scope.submitOrder = function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(
            function (response) {
                if (response.success) {
                    alert("下单成功, 请在5分钟内完成支付!");
                    location.href = "pay.html";
                } else {
                    if (response.message == "用户未登录,订单提交失败") {
                        //encodeURIComponent(location.href): 获得当前浏览器的请求路径
                        location.href = "http://localhost:9100/cas/login?service=" + encodeURIComponent(location.href);

                    }else {
                        alert(response.message);
                    }

                }

            }
        );
    }

});