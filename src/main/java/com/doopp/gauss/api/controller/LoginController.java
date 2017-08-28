package com.doopp.gauss.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.doopp.gauss.api.service.LoginService;
import com.doopp.gauss.api.service.RestResponseService;
import com.doopp.gauss.api.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.ShardedJedisPool;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 登录界面
 *
 * Created by henry on 2017/7/11.
 */
@Controller
@RequestMapping(value = "api/v1/")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private RestResponseService restResponse;

    @Resource
    private ShardedJedisPool shardedJedisPool;

    /*
     * 提交登录
     */
    @ResponseBody
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public JSONObject login(HttpServletResponse response,
                            HttpSession httpSession,
                            @RequestParam("account") String account,
                            @RequestParam("password") String password) {

        logger.info(" >>> " + shardedJedisPool);

        // 校验用户名，密码
        if (!loginService.checkLoginRequest(account, password)) {
            // 告诉客户端密码错误
            return restResponse.error(response, 404, "Account or password is failed");
        }
        // 注册一个登录用户，生成 access token ，并缓存这个 key 对应的值 (account)
        if (!loginService.registerLogin(account, httpSession)) {
            return restResponse.error(response, 500, "can not login");
        }
        // 登录成功
        return restResponse.success();
    }

    /*
     * 退出登录
     */
    @ResponseBody
    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public JSONObject logout(HttpServletResponse response, HttpSession httpSession) {
        // 清空 cookie
        if (!loginService.unregisterLogin(httpSession)) {
            // 退出登录失败
            return restResponse.error(response, 500, "logout failed");
        }
        // 退出登录成功
        return restResponse.success();
    }

    @RequestMapping(value = "web/socket", method = RequestMethod.GET)
    public String socket() {
        return "api/login/web_socket";
    }
}
