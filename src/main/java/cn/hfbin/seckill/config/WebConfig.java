package cn.hfbin.seckill.config;

import cn.hfbin.seckill.filter.SessionExpireFilter;
import cn.hfbin.seckill.interceptor.AccessLimitInterceptor;
import cn.hfbin.seckill.interceptor.AuthorityInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.Filter;

/**
 * @Date 2022/7/31 16:16
 * @Author zhuzhiwei
 */
//@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;

    @Autowired
    private AuthorityInterceptor authorityInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        // 映射为 user 的控制器下的所有映射
        //registry.addInterceptor(authorityInterceptor).addPathPatterns("/user/login").excludePathPatterns("/index", "/");
        registry.addInterceptor(authorityInterceptor)
                .excludePathPatterns("/page/login")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/logout");

        registry.addInterceptor(accessLimitInterceptor);

        

        super.addInterceptors(registry);
    }

    @Bean("myFilter")
    public Filter uploadFilter() {
        return new SessionExpireFilter();
    }

    @Bean
    public FilterRegistrationBean uploadFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new DelegatingFilterProxy("myFilter"));
        registration.addUrlPatterns("/**");
        registration.setName("MyFilter");
        registration.setOrder(1);
        return registration;
    }


}
