//定义服务层
app.service("brandService", function ($http) {
    this.findAll = function () {
        return $http.get("../brand/findAll.do");
    };
    this.findPage = function (page, rows) {
        return $http.get("../brand/findPage.do?currentPage=" + page + "&pageSize=" + rows);

    };
    this.save = function (entity) {
        return $http.post("../brand/add.do", entity);
    };
    this.update = function (entity) {
        return $http.post("../brand/updateBrand.do", entity);

    };
    this.findByBrandName = function (entity) {
        return $http.post("../brand/findByBrandName.do", entity);

    };
    this.findById = function (id) {
        return $http.get("../brand/findById.do?id=" + id);
    };
    this.deleteSelected = function (selectedIds) {
        return $http.get("../brand/deleteSelected.do?ids=" + selectedIds)
    };
    this.search = function (page,rows,searchConditions) {
        return $http.post("../brand/findPageByConditions.do?currentPage=" + page + "&pageSize=" + rows, searchConditions);
    };
    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do");

    };

});