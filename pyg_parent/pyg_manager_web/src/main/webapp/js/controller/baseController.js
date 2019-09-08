//基本控制层
app.controller("baseController", function ($scope) {

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1, //当前页码
        totalItems: 10, //总条数
        itemsPerPage: 5, //每页显示条数
        perPageOptions: [5, 6, 7, 8, 9, 10], //每页显示条数的页码选项
        //查询加载, 更新页面时触发事件
        onChange: function () {
            //重新加载
            $scope.reloadList();
        }
    };

    //查询加载列表, 数据
    $scope.reloadList = function () {
        //切换页码
        /*$scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);*/
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    //定义一个装载选中的id的集合
    $scope.selectIds = [];
    //定义更新复选框的方法
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            //如果是被选中的, 就将其id值添加到数组中
            $scope.selectIds.push(id);
        } else {
            //查询此id的索引值
            var indexOf = $scope.selectIds.indexOf(id);
            //根据索引删除该id
            // splice方法的第一个参数表示要删除的元素的索引, 第二个参数表示要删除的元素的个数
            $scope.selectIds.splice(indexOf, 1);
        }
    };

    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString = function (jsonString, key) {
        var json = JSON.parse(jsonString);//将json字符串转换为json对象
        var value = "";
        for (var i = 0; i < json.length; i++) {
            if (i > 0) {
                value += ", "
            }
            //json[i].key和json[i][key]同样是获取json对象中key索引所对应的值的方法
            value += json[i][key];
        }
        return value;
    };
    $scope.jsonToStringTwo = function (jsonString, key) {
        var json = JSON.parse(jsonString);
        var value = "";
        if (jsonString != null && jsonString.length > 0) {
            for (var i = 0; i < json.length; i++) {
                if (i < json.length - 1) {
                    //json[i].key和json[i][key]同样是获取json对象中key索引所对应的值的方法
                    value += json[i][key]+", ";
                } else {
                    value += json[i][key];
                }
            }
        }
        return value;
    };

    /**
     * 查询集合中是否存在目标属性
     * @param list: 待匹配的集合对象
     * @param key: 目标属性的键
     * @param keyValue: 目标属性的名称
     */
    $scope.searchObjByKey = function (list, key, keyValue) {
        if (list != null && list.length > 0) {
            for (var i = 0; i < list.length; i++) {
                //若查询出目标属性, 说明该属性已存在, 返回该对象
                if (list[i][key] == keyValue) {
                    return list[i];
                }
            }
            //若遍历后未匹配到, 说明该属性尚未添加, 返回null
            return null;
        }
    };

    //改变页面后回显checkBox的选中状态
    $scope.checkCheckBox = function (id) {
        var indexNumber = $scope.selectIds.indexOf(id);
        return indexNumber >= 0;
    }

});