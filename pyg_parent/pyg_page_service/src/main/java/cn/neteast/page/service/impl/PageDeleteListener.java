package cn.neteast.page.service.impl;

import cn.neteast.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("PageDeleteListener 接收到消息：" + goodsIds);
            if (goodsIds != null && goodsIds.length > 0) {
                for (Long goodsId : goodsIds) {
                    itemPageService.deleteItemHtml(goodsId);
                }
            }
            System.out.println("删除静态页面完成...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
