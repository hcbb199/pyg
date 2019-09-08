package cn.neteast.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * spring-security登录验证
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    /**
     * 获取登录的用户的名称(用于页面显示)
     * @return
     */
    @RequestMapping("/name")
    public Map loginName() {
        //getAuthentication()方法是获取登录用户
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName",loginName);
        return map;
    }

}
