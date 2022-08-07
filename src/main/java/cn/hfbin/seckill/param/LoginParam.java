package cn.hfbin.seckill.param;

import cn.hfbin.seckill.validator.IsMobile;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @Date 2022/7/24 17:12
 * @Author zhuzhiwei
 */
@Data
@ToString
public class LoginParam {

    @NotNull(message = "手机号不能为空")
    @IsMobile()
    private String mobile;
    @NotNull(message = "密码不能为空")
    @Length(min = 23, message = "密码长度需要在7个字以内")
    private String password;
}
