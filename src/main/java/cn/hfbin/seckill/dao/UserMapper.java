package cn.hfbin.seckill.dao;

import cn.hfbin.seckill.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * @Date 2022/7/24 17:02
 * @Author zhuzhiwei
 */
public interface UserMapper {

    User selectByPhoneAndPassword(@Param("phone") String phone , @Param("password") String password);

    User checkPhone(@Param("phone") String phone );
}
