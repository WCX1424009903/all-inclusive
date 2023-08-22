package org.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 授权认证服务配置,继承授权服务配置适配器类
 * @author wcx
 * @date 2021/9/12 17:54
 */
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private DomainUserService domainUserService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 方法实现说明:服务器颁发的token的存储方式有四种存储方式
     * ①:基于内存的(生产环境几乎不用,因为认证服务器重启后token就消失了) 基于内存的不需要配该组件
     * ②:基于Db存储的(生产上也机会不用，因为读取数据库的没有redis快)
     * ③:基于redis存储的(可用于生产,速度可以，但是基于redis存储的token 没有实际的业务意义,如果需要手动设置token过期就有意义)
     * ④:基于jwt存储的(合适用于生产，无法保证手动注销token)
     */
    @Bean
    public TokenStore tokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

    /**
     * 授权服务器端点配置器
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 调用此方法才能支持 password 模式
        endpoints.authenticationManager(authenticationManager);
        // 用户验证服务
        endpoints.userDetailsService(domainUserService);
        // token 的存储方式
        endpoints.tokenStore(tokenStore());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 允许客户端进行表单身份验证
        security.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()").allowFormAuthenticationForClients();
    }

     /**
     *authorizedGrantTypes 可以包括如下几种设置中的一种或多种：
     * authorization_code：授权码类型。
     * implicit：隐式授权类型。
     * password：资源所有者（即用户）密码类型。
     * client_credentials：客户端凭据（客户端ID以及Key）类型。
     * refresh_token：通过以上授权获得的刷新令牌来获取新的令牌。
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        /** 可采用与jdbc方式注入数据源并写查询sql语句进行交互*/
        // 配置client_id和client_secret
        clients.inMemory()
                .withClient("client-app")
                // 配置申请的权限范围
                .scopes("all")
                .secret(passwordEncoder.encode("123456"))
                // 重定向登录地址,authorization_code模式进行
               // .redirectUris("http://www.baidu.com")
                // 该客户端允许授权的类型
                .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
                .accessTokenValiditySeconds(3600)
                .refreshTokenValiditySeconds(7200);
    }
}
