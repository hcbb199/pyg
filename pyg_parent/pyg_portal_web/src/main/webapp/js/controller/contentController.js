//广告控制层
app.controller("contentController", function ($scope, contentService) {
    //定义广告集合
    $scope.contentList = [];
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (response) {
                //数组套数组
                $scope.contentList[categoryId] = response;

            }
        );
    };
    $scope.search = function () {
        //angularJS中请求参数缀在#?后面
        location.href = "http://localhost:9104/search.html#?keywords=" + $scope.keywords;
    }

});