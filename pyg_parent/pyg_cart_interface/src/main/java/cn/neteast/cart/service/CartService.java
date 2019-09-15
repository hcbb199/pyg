package cn.neteast.cart.service;

import cn.neteast.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {
    /**
     * 添加商品到购物车列表
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 用户登录后, 从redis中查询购物车列表
     *
     * @param username
     * @return
     */
    List<Cart> getCartListFromRedis(String username);

    /**
     * 用户登录后, 将购物车列表存入redis中
     *
     * @param cartList
     * @param username
     */
    void saveCartListToRedis(List<Cart> cartList, String username);

    /**
     * 当用户登录后, 合并购物车列表
     *
     * @param cartList_redis
     * @param cartList_cookie
     * @return
     */
    List<Cart> margeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie);
}
