package cn.hfbin.seckill.controller;

import cn.hfbin.seckill.common.Const;
import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.result.Result;
import cn.hfbin.seckill.param.LoginParam;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.redis.UserKey;
import cn.hfbin.seckill.service.UserService;
import cn.hfbin.seckill.util.CookieUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * @Date 2022/7/24 16:20
 * @Author zhuzhiwei
 */
@Controller
@RequestMapping("/user")
public class LoginController {

    private final RedisService redisService;
    private final UserService userService;

    public LoginController(RedisService redisService, UserService userService) {
        this.redisService = redisService;
        this.userService = userService;
    }

    @RequestMapping("/login")
    @ResponseBody
    public Result<User> doLogin(HttpServletResponse response, HttpSession session, @Valid LoginParam loginParam) {
        User user = userService.login(loginParam);
        CookieUtil.writeLoginToken(response, session.getId());
        redisService.set(RedisPrefixKeyConst.USERNAME, session.getId(), user, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        return Result.success(user);
    }

    @RequestMapping("/logout")
    public String doLogout(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtil.readLoginToken(request);
        CookieUtil.delLoginToken(request, response);
        redisService.del(UserKey.getByName, token);
        return "login";
    }
}
