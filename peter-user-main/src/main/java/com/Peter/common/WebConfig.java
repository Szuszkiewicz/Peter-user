package com.Peter.common;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${swagger.prefix:}")
    private String swaggerPrefix;

    @Resource
    private JwtInterceptor jwtInterceptor;

    // 加自定义拦截器JwtInterceptor，设置拦截规则
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor((HandlerInterceptor) jwtInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/files/**")
                .excludePathPatterns("/user/query/simple/user/info/**")
                .excludePathPatterns("/media/blog/selectPage")
                .excludePathPatterns("/category/selectAll")
                .excludePathPatterns("/media/blog/selectTop")
                .excludePathPatterns("/media/notice/selectAll")
                .excludePathPatterns("/media/query")
                .excludePathPatterns("/media/blog/selectRecommend/**")
                .excludePathPatterns("/media/updateReadCount/**")
                .excludePathPatterns("/comment/query");
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (isPrefixSet()) {
            registry.addResourceHandler(swaggerPrefix + "/swagger-ui.html*").addResourceLocations("classpath:/META-INF/resources/");
            registry.addResourceHandler(swaggerPrefix + "/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");

            registry.addResourceHandler(swaggerPrefix + "/doc.html")
                    .addResourceLocations("classpath:/META-INF/resources/");
        }
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        if (isPrefixSet()) {
            registry.addRedirectViewController(swaggerPrefix + "/v2/api-docs", "/v2/api-docs").setKeepQueryParams(true);
            registry.addRedirectViewController(swaggerPrefix + "/swagger-resources", "/swagger-resources");
            registry.addRedirectViewController(swaggerPrefix + "/swagger-resources/configuration/ui", "/swagger-resources/configuration/ui");
            registry.addRedirectViewController(swaggerPrefix + "/swagger-resources/configuration/security", "/swagger-resources/configuration/security");
            registry.addRedirectViewController("/swagger-ui.html", "/404");

            // 处理 doc.html 的视图重定向
            registry.addViewController(swaggerPrefix + "/doc.html")
                    .setViewName("forward:/doc.html");
        }
    }

    private boolean isPrefixSet() {
        return swaggerPrefix != null && !"".equals(swaggerPrefix) && !"/".equals(swaggerPrefix);
    }
}
