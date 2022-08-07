package cn.hfbin.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Date 2022/7/24 16:25
 * @Author zhuzhiwei
 */
@Controller
@RequestMapping("/page")
public class PageController {


    @RequestMapping("login")
    public String loginPage(){
        return "login";
    }
}
