package cn.neteast.pay.service.impl;

import cn.neteast.HttpClient;
import cn.neteast.pay.service.WeChatPayService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信支付接口实现类
 */
@Service
public class WeChatPayServiceImpl implements WeChatPayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;

    /**
     * 生成微信支付二维码
     *
     * @param out_trade_no
     * @param total_fee
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1. 创建参数
        Map<String, String> param = new HashMap<String, String>();
        param.put("appid", appid); //公众号
        param.put("mch_id", partner); //商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
        param.put("body", "品优购"); //商品描述
        param.put("out_trade_no", out_trade_no); //商户订单号
        param.put("total_fee", total_fee); //总金额（分）
        param.put("spbill_create_ip", "127.0.0.1"); //IP
        param.put("notify_url", "http://test.itcast.cn"); //回调地址(随便写)
        param.put("trade_type", "NATIVE"); //交易类型
        Map<String, String> map = new HashMap<String, String>();
        try {
            //2. 生成要发送的xml
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("发送的xml:" + xmlParam);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //3. 获得结果
            String content = httpClient.getContent();
            System.out.println("返回的结果是: " + content);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            map.put("code_url", resultMap.get("code_url")); //支付地址
            map.put("total_fee", total_fee);
            map.put("out_trade_no", out_trade_no);

        } catch (Exception e) {
            e.printStackTrace();

        }
        return map;
    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map param = new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println("订单支付的结果集合: " + map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 关闭支付(此处仅查询支付状态)
     *
     * @param out_trade_no
     * @return
     */
    @Override
    public Map closePay(String out_trade_no) {
        Map param = new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client = new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
