package cn.neteast.cart.controller;

import cn.neteast.CookieUtil;
import cn.neteast.cart.service.CartService;
import cn.neteast.pojogroup.Cart;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout=6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 获取购物车列表
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //无论用不用, 都要先从cookie中查询有没有购物车列表
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if ("anonymousUser".equals(username)) {
            //未登录, 从cookie中获取cartList
            System.out.println("从cookie中查询购物车列表...");
            return cartList_cookie;
        } else {
            //已登录, 从redis中获取cartList
            List<Cart> cartList_redis = cartService.getCartListFromRedis(username);
            if (cartList_cookie != null && cartList_cookie.size() > 0) {
                //合并cookie和redis中的购物车列表
                cartList_redis = cartService.margeCartList(cartList_redis, cartList_cookie);
                //将合并后的购物车列表保存到redis中
                cartService.saveCartListToRedis(cartList_redis, username);
                //删除原cookie中的购物车列表
                CookieUtil.deleteCookie(request, response, "cartList");
            }
            return cartList_redis;
        }
    }

    /**
     * 添加商品到购物车列表
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户是: " + username);
        //获取购物车列表
        List<Cart> cartList = findCartList();
        //添加商品到购物车列表
        cartList = cartService.addGoodsToCartList(cartList, itemId, num);
        try {

            if ("anonymousUser".equals(username)) {
                //未登录, 将cartList保存到cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {
                //已登录, 将cartList保存到redis
                cartService.saveCartListToRedis(cartList,username);
            }

            return new Result(true, "修改购物车成功! ");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改购物车失败! ");
        }
    }


}