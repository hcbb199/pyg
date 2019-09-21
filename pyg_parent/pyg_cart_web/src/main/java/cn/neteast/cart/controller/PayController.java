package cn.neteast.cart.controller;

import cn.neteast.IdWorker;
import cn.neteast.order.service.OrderService;
import cn.neteast.pay.service.WeChatPayService;

import cn.neteast.pojo.TbPayLog;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 */
@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private WeChatPayService weChatPayService;

    @Reference
    private OrderService orderService;

    /**
     * 生成微信支付二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        /*IdWorker idWorker = new IdWorker();
        return weChatPayService.createNative(String.valueOf(idWorker.nextId()), "1");*/
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志存在
        if(payLog!=null){
            return weChatPayService.createNative(payLog.getOutTradeNo(),String.valueOf(payLog.getTotalFee()/1000));
            //商品价格设置得太高了, 所以生成二维码时将价格/1000, 测试无误
        }else{
            return new HashMap();
        }
    }



    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int num = 0;
        while (true) {
            Map<String, String> map = weChatPayService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false, "支付出错!");
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))) {
                result = new Result(true, "支付成功!");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //为了不让循环无休止地运行, 我们定义一个循环变量, 如果这个变量超过了这个值则退出循环, 设置时间为5分钟
            num++;
            if (num > 100) {
                result = new Result(false, "二维码超时");
                break;
            }
        }
        return result;

    }
}
