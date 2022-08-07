package cn.hfbin.seckill.interceptor;

import cn.hfbin.seckill.annotations.AccessLimit;
import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.entity.result.Result;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.util.CookieUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * @Description 访问流量控制
 * @Date 2022/8/6 23:19
 * @Author zhuzhiwei
 */
//@Component
public class AccessLimitInterceptor implements HandlerInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessLimitInterceptor.class);

    private final RedisService redisService;

    public AccessLimitInterceptor(RedisService redisService) {
        this.redisService = redisService;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //请求controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        //解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        StringBuffer requestParamBuffer = new StringBuffer();
        Map<String, String[]> paramMap = request.getParameterMap();
        for (Map.Entry<String, String[]> stringEntry : paramMap.entrySet()) {
            String mapKey = stringEntry.getKey();
            //request的这个参数map的value返回的是一个String[]
            String[] strArr = stringEntry.getValue();
            String mapValue = "";
            if (strArr != null) {
                mapValue = Arrays.toString(strArr);
            }
            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        //接口限流
        AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
        if (accessLimit == null) {
            return true;
        }
        int seconds = accessLimit.seconds();
        int maxCount = accessLimit.maxCount();
        boolean needLogin = accessLimit.needLogin();
        String key = request.getRequestURI();


        //对于拦截器中拦截manage下的login.do的处理,对于登录不拦截，直接放行
        if (!StringUtils.equals(className, "SeckillController")) {
            //如果是拦截到登录请求，不打印参数，因为参数里面有密码，全部会打印到日志中，防止日志泄露
            LOGGER.info("权限拦截器拦截到请求 SeckillController ,className:{},methodName:{}", className, methodName);
            return true;
        }

        LOGGER.info("--> 权限拦截器拦截到请求,className:{},methodName:{},param:{}", className, methodName, requestParamBuffer);
        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)) {
            user = redisService.get(RedisPrefixKeyConst.USERNAME, loginToken, User.class);
        }

        if (needLogin) {
            if (user == null) {
                render(response, CodeMsg.USER_NO_LOGIN);
                return false;
            }
            key += "_" + user.getId();
        }
        Integer count = redisService.get(RedisPrefixKeyConst.WITH_EXPIRE, key, Integer.class);
        if (count == null) {
            redisService.set(RedisPrefixKeyConst.WITH_EXPIRE, key, 1, seconds);
        } else if (count < maxCount) {
            redisService.incr(RedisPrefixKeyConst.WITH_EXPIRE, key);
        } else {
            render(response, CodeMsg.ACCESS_LIMIT_REACHED);
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
