package com.Peter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    /**
     * 根据yml文件配置的redis连接工厂,创建RedisConnectionFactory实例
     * @param connectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        //将java对象序列化为字符串存入redis,以及把redis对象取出时转换为java对象,redis底层只能储存字节数组
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setConnectionFactory(connectionFactory);
        //设置key的序列化方式
        //key用于定位和访问Redis中的数据,每个key都是唯一的,通过key来读取,修改,删除对应的value,通常使用:分隔的层级结构
        template.setKeySerializer(serializer);
        //设置value的序列化方式
        //value是实际存储的数据,可以是任何类型,可以是字符串,数字,JSON,对象(取决于序列化器),通常使用json格式,存储具体的业务信息,如token,用户信息,配置信息etc.
        template.setValueSerializer(serializer);
        //设置hash的key的序列化方式
        /*
          Hash结构示例
          Key: "user:1001"
            ├─ Field: "name"    → Value: "Peter"
            ├─ Field: "email"   → Value: "peter@example.com"
            └─ Field: "age"     → Value: "25"
         */
        template.setHashKeySerializer(serializer);
        //设置hash的value的序列化方式
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
