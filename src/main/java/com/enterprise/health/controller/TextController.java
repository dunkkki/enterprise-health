package com.enterprise.health.controller;

import com.enterprise.health.common.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TextController {

    // 正常接口
    @RateLimit(maxRequests = 3, timeWindow = 60)
    @GetMapping("/ok")
    public String testOk(){
        // 模拟业务耗时500毫秒
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "接口访问成功";
    }

    // 会报错的异常接口
    @GetMapping("/error")
    public String testError(){
        int num = 1 / 0; // 算术异常
        return "";
    }
}
