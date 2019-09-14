package cn.neteast.sms;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {
    @Autowired
    private SmsUtil smsUtil;
    @JmsListener(destination = "sms")
    public void sendSms(Map<String,String> map) {
        try {
            SendSmsResponse smsResponse = smsUtil.sendSms(
                    map.get("mobile"), map.get("template_code"), map.get("sign_name"), map.get("param"));
            String code = smsResponse.getCode();
            System.out.println("code: " + smsResponse.getCode());
            System.out.println("bizId: " + smsResponse.getBizId());
            System.out.println("message: " + smsResponse.getMessage());
            System.out.println("requestId: " + smsResponse.getRequestId());
        } catch (ClientException e) {
            e.printStackTrace();
        }

    }
}
