package cn.neteast.search.service.impl;

import cn.neteast.pojo.TbItem;
import cn.neteast.search.service.ItemSearchService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

/**
 * 监听：用于添加索引库中记录
 */
@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        System.out.println("ItemSearchListener 监听器接收到消息...");
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            for (TbItem item : itemList) {
                //将spec字段中的json字符串转换为map
                Map specMap = JSON.parseObject(item.getSpec());
                //给带注解的字段赋值
                item.setSpecMap(specMap);
            }
            itemSearchService.importList(itemList);
            System.out.println("成功导入到索引库...");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
