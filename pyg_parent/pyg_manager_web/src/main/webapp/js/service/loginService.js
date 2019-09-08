//登录服务器层
app.service("loginService", function ($http) {
    //获取登录用户的姓名
    this.loginName = function () {
        return $http.get("../login/name.do");
    }
});