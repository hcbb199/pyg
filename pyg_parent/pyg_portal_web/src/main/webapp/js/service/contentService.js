//根据广告类型id查询广告列表
app.service("contentService", function ($http) {
    this.findByCategoryId = function (categoryId) {
        return $http.get("content/findByCategoryId.do?categoryId=" + categoryId);

    }

});