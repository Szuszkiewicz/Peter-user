package com.Peter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Slf4j
@RefreshScope
public class TestController {
    @Value(value="${peter.test.hh}")
    private String nacosStr;

    @RequestMapping(value="/nacos",method= RequestMethod.GET)
    public String testNacos(){
        return nacosStr;
    }
}
