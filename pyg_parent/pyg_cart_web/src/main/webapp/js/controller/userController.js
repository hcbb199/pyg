//控制层
app.controller('userController', function ($scope, $controller, userService) {
    //注册
    $scope.register = function () {
        if ($scope.entity.password != $scope.password) {
            alert("两次输入的密码不一致, 请重新输入! ");
            return;
        }
        userService.add($scope.entity, $scope.smsCode).success(
            function (response) {
                alert(response.message);
            }
        )
    };
    $scope.entity = {"phone":""};
    //发送短信
    $scope.sendCode = function () {
        if ($scope.entity.phone == "") {
            alert("请输入手机号! ");
            return;
        }
        //定义正则
        var reg_phone = /^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\d{8}$/;
        //判断, 给出提示信息
        var flag = reg_phone.test($scope.entity.phone);
        if (flag) {
            userService.sendCode($scope.entity.phone).success(
                function (response) {
                    alert(response.message);
                }
            );
        } else {
            alert("号码格式不正确, 请重新输入! ")
        }
    };


});	
