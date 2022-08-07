package cn.hfbin.seckill.exception;


import cn.hfbin.seckill.entity.result.CodeMsg;

public class SecKillException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final CodeMsg cm;

    public SecKillException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }

}
