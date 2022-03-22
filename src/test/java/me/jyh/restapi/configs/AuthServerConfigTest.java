package me.jyh.restapi.configs;

import me.jyh.restapi.accounts.Account;
import me.jyh.restapi.accounts.AccountRole;
import me.jyh.restapi.accounts.AccountService;
import me.jyh.restapi.common.AppProperties;
import me.jyh.restapi.common.BaseTest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthServerConfigTest extends BaseTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AppProperties appProperties;

    // Spring 이 제공하는 password 방법과 refresh token 을 사용할 것임
    // 최초 토큰을 발급 받을 때는 password 방법으로 발급 받을 것
    // password Grand Type은 홉이 1번(요청과 응답이 한 쌍)
    // 보통 sns 로그인을 하면 토큰을 받아서 토큰을 써드파티에 체크를 하고 복잡하지만
    // password 방식은 서버에서 회원 정보(비밀번호)를 갖고 있기때문에 바로 체크해서 토큰을 발급함
    @Test
    @DisplayName("인증 토큰을 발급 받는 테스트")
    public void getAuthToken() throws Exception {
        mockMvc.perform(post("/oauth/token")
                        .with(httpBasic(appProperties.getClientId(),appProperties.getClientSecret()))
                        .param("username",appProperties.getUserUsername())
                        .param("password", appProperties.getUserPassword())
                        .param("grant_type", "password")) // 사용할 인증 타입
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists());
    }



}