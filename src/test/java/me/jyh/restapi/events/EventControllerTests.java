package me.jyh.restapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.jyh.restapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // 통합테스트 - default SpringBootTest.WebEnvironment.MOCK; // 웹 테스트는 그냥 통합테스트로 하는게 나음
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
    
    //WebMvcTest 는 repository는 빈 등록 안해줌
//    @MockBean // mock 객체 빈으로 등록
//    EventRepository eventRepository;

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 3, 13, 21, 35))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 3, 14, 21, 35))
                .beginEventDateTime(LocalDateTime.of(2022, 3, 16, 21, 35))
                .endEventDateTime(LocalDateTime.of(2022, 3, 17, 21, 35))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .build();

        // ==== 컨트롤러에는 eventDto를 event로 만들어서 넣었고 여기서는 event를 바로 넣어서 실행되지 않음. -> @SpringBootTest로 변경해서 테스트
//        event.setId(10L); // event에 id가 필요한데 Test기 때문에 수동으로 넣어줌
//        // mock객체는 save등 무엇을 해도 리턴값이 null 임 -> nullPointException 발생
//        Mockito.when(eventRepository.save(event)).thenReturn(event);
//        // Mockito.when(A).thenReturn(B);
//        // A가 발생할때, B를 리턴하라.

        mockMvc.perform(post("/api/events/")
                    .contentType(MediaType.APPLICATION_JSON) // 요청값 타입 설정 : json
                    .accept(MediaTypes.HAL_JSON) // 원하는 응답 설정 : hal+json
                    .content(objectMapper.writeValueAsString(event)))
                .andDo(print()) // 요청,응답 콘솔 찍어보기
                .andExpect(status().isCreated()) // status() import하기
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(Matchers.not(true)))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
        ;
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100L)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 3, 13, 21, 35))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 3, 14, 21, 35))
                .beginEventDateTime(LocalDateTime.of(2022, 3, 16, 21, 35))
                .endEventDateTime(LocalDateTime.of(2022, 3, 17, 21, 35))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events/")
                        .contentType(MediaType.APPLICATION_JSON) // 요청값 타입 설정 : json
                        .accept(MediaTypes.HAL_JSON) // 원하는 응답 설정 : hal+json
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print()) // 요청,응답 콘솔 찍어보기
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @TestDescription("입력값이 비어있는 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDto eventDto = EventDto.builder().build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    //EX) 이벤트 날짜가 잘못됨, basePrice가 maxPrice 보다 큼
    @Test
    @TestDescription("입력값이 잘못된 경우에 에러가 발생하는 테스트")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 3, 16, 21, 35))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 3, 15, 21, 35))
                .beginEventDateTime(LocalDateTime.of(2022, 3, 14, 21, 35))
                .endEventDateTime(LocalDateTime.of(2022, 3, 13, 21, 35))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
//                .andExpect(jsonPath("$[0].field").exists()) // 글로벌에러에는 없어서 에러 날 수도 있음
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
//                .andExpect(jsonPath("$[0].rejectedValue").exists()) // 글로벌에러에는 없어서 에러 날 수도 있음
        ;
    }

}
