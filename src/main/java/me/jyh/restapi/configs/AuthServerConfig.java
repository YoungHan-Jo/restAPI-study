package me.jyh.restapi.configs;

import me.jyh.restapi.accounts.AccountService;
import me.jyh.restapi.common.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManager authenticationManager; // 유저 정보 인증을 가지고 있음
    @Autowired
    AccountService accountService;
    @Autowired
    TokenStore tokenStore;
    @Autowired
    AppProperties appProperties;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 패스워드 인코더를 설정
        security.passwordEncoder(passwordEncoder);
    }
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.jdbc() // 이상적인건 jdbc를 써서 db에 저장하는 것
        clients.inMemory() // 인 메모리
                .withClient(appProperties.getClientId())
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("read", "write") // 권한 범위
                .secret(passwordEncoder.encode(appProperties.getClientSecret()))
                .accessTokenValiditySeconds(10 * 60) // 엑세스 토큰 유효 시간 단위 초
                .refreshTokenValiditySeconds(6 * 10 * 60); // 리프레시토큰 유효시간 단위 초
    }
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager) // 내가 갖고 있는 manager로 설정
                .userDetailsService(accountService) // 내가 갖고 있는 accountService로 설정
                .tokenStore(tokenStore);
    }
}
