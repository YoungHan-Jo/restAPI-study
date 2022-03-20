package me.jyh.restapi.configs;

import me.jyh.restapi.accounts.AccountService;
import net.bytebuddy.build.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration // 빈 설정 파일
@EnableWebSecurity // + WebSecurityConfigurerAdapter를 상속 받으면 스프링부트에서 자동 적용하는 설정은 멈추고 여기서 수동으로 설정해서 적용됨
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    AccountService accountService;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Bean
    public TokenStore tokenStore() { // 토큰 저장소
        return new InMemoryTokenStore();
    }

    @Bean // 빈으로 등록해서 다른데서 쓸 수 있도록
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override // authenticationManager 를 어떻게 만들지 설정
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService)
                .passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/docs/index.html");
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());// 정적인 것들 다 무시할때 사용 servlet로 써야함
    }

    // 시큐리티에 들어와서 걸러주는거 그냥 웹에서 걸러주는거 위에꺼 쓰면됨
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests()
//                .mvcMatchers("/docs/index.html").anonymous()
//                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
//    }
}
