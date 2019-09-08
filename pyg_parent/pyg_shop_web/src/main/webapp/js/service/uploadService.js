//文件上传服务层
app.service("uploadService", function ($http) {
    this.uploadFile = function () {
        var formData = new FormData;
        //append() 方法在被选元素的结尾(仍然在内部)插入指定内容
        formData.append("file",document.getElementById("file").files[0]);
        return $http({
            method: "POST",
            url: "../upload.do",
            data: formData,
            //anjularjs对于post和get请求默认的Content-Type header 是application/json
            //通过设置"Content-Type": undefined, 这样浏览器会把Content-Type 设置为 multipart/form-data
            headers: {"Content-Type": undefined},
            //通过设置 transformRequest: angular.identity
            // anjularjs transformRequest function 将序列化 formData object
            transformRequest: angular.identity
        })
    };
});