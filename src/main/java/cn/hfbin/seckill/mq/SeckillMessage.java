package cn.hfbin.seckill.mq;


import cn.hfbin.seckill.entity.User;
import lombok.Data;

/**
 * @Date 2022/8/3 22:20
 * @Author zhuzhiwei
 */
@Data
public class SeckillMessage {
	private User user;
	private long goodsId;

	public SeckillMessage() {
	}

	public SeckillMessage(User user, long goodsId) {
		this.user = user;
		this.goodsId = goodsId;
	}
}
