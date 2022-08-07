package cn.hfbin.seckill.exception;


import cn.hfbin.seckill.entity.result.CodeMsg;
import lombok.Getter;

@Getter
public class SecKillException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String msg;

    private final CodeMsg cm;

    public SecKillException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public SecKillException(String msg) {
        super(msg);
        this.cm = null;
        this.msg = msg;
    }
}
