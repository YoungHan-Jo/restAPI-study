package me.jyh.restapi.accounts;

import me.jyh.restapi.common.BaseTest;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class AccountServiceTest extends BaseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Test
    @DisplayName("userDetailsService에 있는 유저 비밀번호와 일치하는지")
    public void findByUsername() throws Exception {
       //given
        String password = "1234";
        String username = "younghan4494@gmail.com";
        Account account = Account.builder()
                .email(username)
                .password(password)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();

        accountRepository.save(account);

        //when
        UserDetailsService userDetailsService = this.accountService; // 인터페이스로 상속 받기
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

       //then
        assertThat(userDetails.getPassword()).isEqualTo(password);
    }

//    @Test(expected = UsernameNotFoundException.class)
    @Test
    public void findByUsernameFail() throws Exception {
        // Expected 예측한 것을 앞에 나둬야함
        String username = "random@email.com";
        expectedException.expect(UsernameNotFoundException.class);
        expectedException.expectMessage(Matchers.containsString(username));

        //when
        accountService.loadUserByUsername(username);

        // try / catch 로 만드는법
//        try {
//            accountService.loadUserByUsername(username); // null 이 나왔을때 테스트
//            Assertions.fail("supposed to be failed");
//        } catch (UsernameNotFoundException e) {
//            assertThat(e.getMessage()).containsSequence(username); // 에러메세지에 username이 있는지
//        }

    }

}