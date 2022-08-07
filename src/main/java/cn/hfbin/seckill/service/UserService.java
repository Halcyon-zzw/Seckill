package cn.hfbin.seckill.service;

import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.result.Result;
import cn.hfbin.seckill.param.LoginParam;

import javax.servlet.http.HttpServletRequest;

/**
 * @Date 2022/7/31 11:30
 * @Author zhuzhiwei
 */
public interface UserService {
    User login(LoginParam loginParam);

    User getUserByRequest(HttpServletRequest request);
}
