package cn.neteast.cart.service.impl;

import cn.neteast.cart.service.CartService;
import cn.neteast.mapper.TbItemMapper;
import cn.neteast.pojo.TbItem;
import cn.neteast.pojo.TbOrderItem;
import cn.neteast.pojogroup.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品到购物车
     *
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("该商品不存在! ");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("该商品尚未开售! ");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {

            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List orderItemList = new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);

            if (orderItem == null) {
                //5.1. 如果没有，新增购物车明细
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);

            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));

                //如果数量操作后小于等于0，则移除
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 从redis中查询购物车列表(当用户登录后)
     *
     * @param username
     * @return
     */
    @Override
    public List<Cart> getCartListFromRedis(String username) {
        System.out.println("从redis中查询购物车列表...");
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<Cart>();
        }
        return cartList;
    }

    /**
     * 将购物车列表存入redis中(当用户登录后)
     *
     * @param cartList
     * @param username
     */
    @Override
    public void saveCartListToRedis(List<Cart> cartList, String username) {
        System.out.println("将购物车列表存入到redis中...");
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * 当用户登录后, 合并购物车列表
     *
     * @param cartList_redis
     * @param cartList_cookie
     * @return
     */
    @Override
    public List<Cart> margeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) {
        /*
            将两个集合的其中之一拆出成SKU列表, 对该SKU列表进行遍历, 得到每一个SKU对象;
            将得到的对象添加到另一个集合中, 实现两个集合的去重合并
         */
        if (cartList_cookie != null && cartList_cookie.size() > 0) {
            for (Cart cart : cartList_cookie) {
                if (cart.getOrderItemList() != null && cart.getOrderItemList().size() > 0) {
                    for (TbOrderItem OrderItem : cart.getOrderItemList()) {
                        cartList_redis = addGoodsToCartList(cartList_redis, OrderItem.getItemId(), OrderItem.getNum());
                    }
                }
            }
        }
        return cartList_redis;
    }

    /**
     * 根据商家ID查询购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (cart.getSellerId().equals(sellerId)) {
                    return cart;
                }
            }
        }
        return null;
    }

    /**
     * 根据商品明细ID查询
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        if (orderItemList != null && orderItemList.size() > 0) {
            for (TbOrderItem orderItem : orderItemList) {
                if (orderItem.getItemId().longValue() == itemId.longValue()) {
                    return orderItem;
                }
            }
        }
        return null;
    }

    /**
     * 创建订单明细
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }

}
