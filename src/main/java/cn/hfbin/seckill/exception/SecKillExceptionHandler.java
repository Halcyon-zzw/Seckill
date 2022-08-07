package cn.hfbin.seckill.exception;

import cn.hfbin.seckill.entity.dto.ProductDeplouResponse;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.entity.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@ControllerAdvice
@ResponseBody
@Slf4j
public class SecKillExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecKillExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    public ProductDeplouResponse<String> exceptionHandler(Exception e) {
        LOGGER.error("", e);
        if (e instanceof SecKillException) {
            SecKillException ex = (SecKillException) e;
            return ProductDeplouResponse.fail(ex.getMsg());
        } else {
            return ProductDeplouResponse.fail("服务端异常");
        }
    }
}
