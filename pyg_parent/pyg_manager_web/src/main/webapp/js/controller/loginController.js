//登录的控制器层
app.controller("loginController", function ($scope, $controller, loginService) {
    //获取登录用户的姓名
    $scope.loginName = function () {
        loginService.loginName().success(
            function (response) {
                $scope.loginName = response.loginName;
            }
        )
    }

});