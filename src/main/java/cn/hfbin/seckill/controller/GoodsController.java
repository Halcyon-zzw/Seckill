package cn.hfbin.seckill.controller;

import cn.hfbin.seckill.common.Const;
import cn.hfbin.seckill.common.RedisPrefixKeyConst;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.entity.bo.GoodsBo;
import cn.hfbin.seckill.entity.result.CodeMsg;
import cn.hfbin.seckill.entity.result.Result;
import cn.hfbin.seckill.entity.vo.GoodsDetailVo;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Date 2022/7/31 16:09
 * @Author zhuzhiwei
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    private final RedisService redisService;
    private final UserService userService;
    private final SeckillGoodsService seckillGoodsService;

    private final ThymeleafViewResolver thymeleafViewResolver;

    private final ApplicationContext applicationContext;

    public GoodsController(RedisService redisService,
                           UserService userService,
                           SeckillGoodsService seckillGoodsService,
                           ThymeleafViewResolver thymeleafViewResolver,
                           ApplicationContext applicationContext) {
        this.redisService = redisService;
        this.userService = userService;
        this.seckillGoodsService = seckillGoodsService;
        this.thymeleafViewResolver = thymeleafViewResolver;
        this.applicationContext = applicationContext;
    }

    @RequestMapping("/list")
    @ResponseBody
    public String list(Model model, HttpServletRequest request, HttpServletResponse response) {
        String html = redisService.get(RedisPrefixKeyConst.GOODS_LIST, "", String.class);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }
        List<GoodsBo> goodsList = seckillGoodsService.getSeckillGoodsList();
        model.addAttribute("goodsList", goodsList);
        SpringWebContext ctx = new SpringWebContext(request, response,
                request.getServletContext(), request.getLocale(), model.asMap(), applicationContext);
        //手动渲染
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
        if (!StringUtils.isEmpty(html)) {
            redisService.set(RedisPrefixKeyConst.GOODS_LIST, "", html, Const.RedisCacheExtime.GOODS_LIST);
        }
        return html;
    }

    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(Model model,
                                        @PathVariable("goodsId") long goodsId,
                                        HttpServletRequest request) {
        User user = userService.getUserByRequest(request);

        GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
        if (goods == null) {
            return Result.error(CodeMsg.NO_GOODS);
        } else {
            model.addAttribute("goods", goods);
            long startAt = goods.getStartDate().getTime();
            long endAt = goods.getEndDate().getTime();
            long now = System.currentTimeMillis();

            int miaoshaStatus;
            int remainSeconds;
            if (now < startAt) {//秒杀还没开始，倒计时
                miaoshaStatus = 0;
                remainSeconds = (int) ((startAt - now) / 1000);
            } else if (now > endAt) {//秒杀已经结束
                miaoshaStatus = 2;
                remainSeconds = -1;
            } else {//秒杀进行中
                miaoshaStatus = 1;
                remainSeconds = 0;
            }
            GoodsDetailVo vo = new GoodsDetailVo();
            vo.setGoods(goods);
            vo.setUser(user);
            vo.setRemainSeconds(remainSeconds);
            vo.setMiaoshaStatus(miaoshaStatus);
            return Result.success(vo);
        }
    }
}

