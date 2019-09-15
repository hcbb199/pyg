app.service("cartService", function ($http) {
    //获取购物车列表
    this.findCartList = function () {
        return $http.get("cart/findCartList.do");
    };
    this.addGoodsToCartList = function (itemId, num) {
        return $http.get("cart/addGoodsToCartList.do?itemId=" + itemId + "&num=" + num);
    };
    //获取合计金额和合计数量
    this.getSumValue = function (cartList) {
        var totalValue = {totalNum: 0, totalMoney: 0.00};

        if (cartList != null) {
            for (var i = 0; i < cartList.length; i++) {
                var orderItem = cartList[i].orderItemList;
                for (var j = 0; j < orderItem.length; j++) {
                    totalValue.totalMoney += orderItem[j].totalFee;
                    totalValue.totalNum += orderItem[j].num;
                }
            }
        }
        return totalValue;
    }
});