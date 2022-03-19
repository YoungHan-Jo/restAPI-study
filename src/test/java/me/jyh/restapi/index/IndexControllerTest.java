package me.jyh.restapi.index;


import me.jyh.restapi.common.RestDocsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest // 통합테스트 - default SpringBootTest.WebEnvironment.MOCK; // 웹 테스트는 그냥 통합테스트로 하는게 나음
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test") // application-test.yml도 오버라이딩으로 적용됨
public class IndexControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void index() throws Exception {

        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.events").exists())
                .andDo(print());
    }
}
