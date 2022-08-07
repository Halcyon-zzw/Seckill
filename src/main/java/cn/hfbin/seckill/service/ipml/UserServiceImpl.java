package cn.hfbin.seckill.service.ipml;

import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.dao.UserMapper;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.exception.SecKillException;
import cn.hfbin.seckill.param.LoginParam;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.service.UserService;
import cn.hfbin.seckill.util.CookieUtil;
import cn.hfbin.seckill.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * @Date 2022/7/31 12:30
 * @Author zhuzhiwei
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    @Override
    public User login(LoginParam loginParam) {

        User user = userMapper.checkPhone(loginParam.getMobile());
        if (user == null) {
            throw new SecKillException(CodeMsg.MOBILE_NOT_EXIST);
        }
        String dbPwd = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(loginParam.getPassword(), saltDB);
        if (!StringUtils.equals(dbPwd, calcPass)) {
            throw new SecKillException(CodeMsg.PASSWORD_ERROR);
        }
        user.setPassword(StringUtils.EMPTY);
        return user;
    }

    @Override
    public User getUserByRequest(HttpServletRequest request) {
        String loginToken = CookieUtil.readLoginToken(request);
        return redisService.get(RedisPrefixKeyConst.USERNAME, loginToken, User.class);
    }
}
