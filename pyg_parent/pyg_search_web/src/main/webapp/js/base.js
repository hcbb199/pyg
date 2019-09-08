//定义品优购模块
var app = angular.module("pyg", []);
//定义过滤器
app.filter("trustHtml", ["$sce", function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}]);