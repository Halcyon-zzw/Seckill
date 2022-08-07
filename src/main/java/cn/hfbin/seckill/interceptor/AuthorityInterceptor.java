package cn.hfbin.seckill.interceptor;

import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.entity.result.Result;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.util.CookieUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


/**
 * @Description 用户权限拦截
 * @Date 2022/8/6 23:19
 * @Author zhuzhiwei
 */
//@Component
public class AuthorityInterceptor implements HandlerInterceptor {

    private final RedisService redisService;

    public AuthorityInterceptor(RedisService redisService) {
        this.redisService = redisService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String loginToken = CookieUtil.readLoginToken(request);
        if (!StringUtils.isNotEmpty(loginToken)) {
            return false;
        }
        User user = redisService.get(RedisPrefixKeyConst.USERNAME, loginToken, User.class);
        if (user == null) {
            render(response, CodeMsg.USER_NO_LOGIN);
            return false;
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }

}
