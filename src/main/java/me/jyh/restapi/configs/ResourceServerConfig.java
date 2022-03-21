package me.jyh.restapi.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    // oauth서버랑 같이 연동됨
    // 리소스 서버는 토큰이 유효한지 확인해서 접근 제한,
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.resourceId("event");
    }
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .anonymous()
                .and()
            .authorizeRequests()
                .mvcMatchers(HttpMethod.GET, "/api/**")
                    .anonymous()// GET 요청은 모두에게 허가(인증 필요없음)
                .anyRequest()
                    .authenticated()
                .and()
            .exceptionHandling() // 예외 발생시
                .accessDeniedHandler(new OAuth2AccessDeniedHandler()); // 접근불가 예외일때 OAuth2AccessDeniedHandler 실행
    }
}
