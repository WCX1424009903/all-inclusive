package org.example.filter;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局拦截器，鉴权和封装请求头，使请求头中存在user，供下游服务使用
 * @author wcx
 * @date 2021/9/14 20:39
 */
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info("{token信息为}:"+token);
        if (StrUtil.isEmpty(token)) {
            return chain.filter(exchange);
        }
        // 从token中解析用户信息并设置到header中
        String realToken = token.replace("Bearer ", "");
        // 根据token获取用户信息，远程调用认证服务器
        String userStr = "user_info_message";
        ServerHttpRequest request = exchange.getRequest().mutate().header("user", userStr).build();
        exchange = exchange.mutate().request(request).build();
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
