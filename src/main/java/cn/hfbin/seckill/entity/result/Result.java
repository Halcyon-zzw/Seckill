package cn.hfbin.seckill.entity.result;

import lombok.Data;

/**
 * @Date 2022/7/24 17:06
 * @Author zhuzhiwei
 */
@Data
public class Result<T> {

	private int code;
	private String msg;
	private T data;

	public boolean isSuccess(){
		return this.code == CodeMsg.SUCCESS.getCode();
	}
	public static  <T> Result<T> success(T data){
		return new Result<>(data);
	}

	public static  <T> Result<T> error(CodeMsg codeMsg){
		return new Result<>(codeMsg);
	}
	private Result(T data) {
		this.code = CodeMsg.SUCCESS.getCode();
		this.msg = CodeMsg.SUCCESS.getMsg();
		this.data = data;
	}

	private Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	private Result(CodeMsg codeMsg) {
		if(codeMsg != null) {
			this.code = codeMsg.getCode();
			this.msg = codeMsg.getMsg();
		}
	}
}
