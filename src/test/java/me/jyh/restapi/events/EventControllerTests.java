package me.jyh.restapi.events;

import me.jyh.restapi.accounts.Account;
import me.jyh.restapi.accounts.AccountRepository;
import me.jyh.restapi.accounts.AccountRole;
import me.jyh.restapi.accounts.AccountService;
import me.jyh.restapi.common.AppProperties;
import me.jyh.restapi.common.BaseTest;
import me.jyh.restapi.common.TestDescription;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class EventControllerTests extends BaseTest {

    @Autowired
    EventRepository eventRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AppProperties appProperties;

    MediaType contentType = new MediaType("application", "hal+json", Charset.forName("UTF-8"));

    @Before
    public void setUp() {
        eventRepository.deleteAll();
        accountRepository.deleteAll();
    }
    
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
                    .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                    .contentType(MediaType.APPLICATION_JSON) // 요청값 타입 설정 : json
                    .accept(MediaTypes.HAL_JSON) // 원하는 응답 설정 : hal+json
                    .content(objectMapper.writeValueAsString(event)))
                .andDo(print()) // 요청,응답 콘솔 찍어보기
                .andExpect(status().isCreated()) // status() import하기
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, contentType.toString()))
//                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
//                .andExpect(jsonPath("_links.self").exists())
//                .andExpect(jsonPath("_links.query-events").exists())
//                .andExpect(jsonPath("_links.update-event").exists())
//                .andExpect(jsonPath("_links.profile").exists()) // 밑에서 다 체크하기 때문에 여기서는 없어도 됨.
                .andDo(document("create_event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query events"),
                                linkWithRel("update-event").description("link to update an existing event"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("enrollment time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("enrollment time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base Price of new event"),
                                fieldWithPath("maxPrice").description("max Price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit Of Enrollment of new event")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("enrollment time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("enrollment time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base Price of new event"),
                                fieldWithPath("maxPrice").description("max Price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit Of Enrollment of new event"),
                                fieldWithPath("offline").description("it tells is this event is offline event or not"),
                                fieldWithPath("free").description("it tells is this event is free or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager.id").description("user id num"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.query-events.href").description("link to query events"),
                                fieldWithPath("_links.update-event.href").description("link to update event"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
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
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
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
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
//                .andExpect(jsonPath("$[0].field").exists()) // 글로벌에러에는 없어서 에러 날 수도 있음
//                .andExpect(jsonPath("$[0].rejectedValue").exists()) // 글로벌에러에는 없어서 에러 날 수도 있음
        ;
    }



    @Test
    @DisplayName("30개의 이벤트를 10개씩 두번재 페이지 조회하기, 인증하지 않음")
    public void queryEvents() throws Exception {
        //given
//        IntStream.range(0,30).forEach(i -> {
//            generateEvent(i);
//        });
        // 람다식으로 전환
        IntStream.range(0,30).forEach(this::generateEvent);

        //when

        //then
        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .param("page", "1")
                        .param("size","10")
                        .param("sort", "name,DESC"))// 요청값 타입 설정 : json
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query_events",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("first").description("first page"),
                                linkWithRel("prev").description("previous page"),
                                linkWithRel("next").description("next page"),
                                linkWithRel("last").description("last page"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestParameters(
                                parameterWithName("size").description("한 페이지 당 개수"),
                                parameterWithName("page").description("페이지 번호 0페이지 부터 시작"),
                                parameterWithName("sort").description("검색 카테고리, 정렬")
                        ),
                        responseHeaders(
//                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("page.size").description("한 페이지 당 개수"),
                                fieldWithPath("page.totalElements").description("전체 개수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
                                fieldWithPath("page.number").description("페이지 수 0부터 시작"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.first.href").description("first page"),
                                fieldWithPath("_links.prev.href").description("previous page"),
                                fieldWithPath("_links.next.href").description("next page"),
                                fieldWithPath("_links.last.href").description("last page"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        ).and(subsectionWithPath("_embedded").type(JsonFieldType.OBJECT).description("이벤트 리스트")) // 생략하고 싶을 때
                ))
        ;
    }

    @Test
    @DisplayName("30개의 이벤트를 10개씩 두번재 페이지 조회하기, 사용자 인증")
    public void queryEventsWithAuthentication() throws Exception {
        //given
        IntStream.range(0,30).forEach(this::generateEvent);
        //when & then
        mockMvc.perform(get("/api/events")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .param("page", "1")
                        .param("size","10")
                        .param("sort", "name,DESC"))// 요청값 타입 설정 : json
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.create-event").exists())
                .andDo(document("query_events",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("first").description("first page"),
                                linkWithRel("prev").description("previous page"),
                                linkWithRel("next").description("next page"),
                                linkWithRel("last").description("last page"),
                                linkWithRel("profile").description("link to profile"),
                                linkWithRel("create-event").description("link to create event")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestParameters(
                                parameterWithName("size").description("한 페이지 당 개수"),
                                parameterWithName("page").description("페이지 번호 0페이지 부터 시작"),
                                parameterWithName("sort").description("검색 카테고리, 정렬")
                        ),
                        responseHeaders(
//                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("page.size").description("한 페이지 당 개수"),
                                fieldWithPath("page.totalElements").description("전체 개수"),
                                fieldWithPath("page.totalPages").description("전체 페이지 수"),
                                fieldWithPath("page.number").description("페이지 수 0부터 시작"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.first.href").description("first page"),
                                fieldWithPath("_links.prev.href").description("previous page"),
                                fieldWithPath("_links.next.href").description("next page"),
                                fieldWithPath("_links.last.href").description("last page"),
                                fieldWithPath("_links.profile.href").description("link to profile"),
                                fieldWithPath("_links.create-event.href").description("link to profile")
                        ).and(subsectionWithPath("_embedded").type(JsonFieldType.OBJECT).description("이벤트 리스트")) // 생략하고 싶을 때
                ))
        ;
    }


    @Test
    @DisplayName("기존에 이벤트를 하나 조회하기")
    public void getEvent() throws Exception {
       //given
        Event event = generateEvent(100);
        //when & then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/events/{id}", event.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get_event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("profile").description("link to profile")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        pathParameters(
                                parameterWithName("id").description("유저 id")
                        ),
                        responseHeaders(
//                                headerWithName(HttpHeaders.LOCATION).description("location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new event"),
                                fieldWithPath("name").description("Name of new event"),
                                fieldWithPath("description").description("description of new event"),
                                fieldWithPath("beginEnrollmentDateTime").description("enrollment time of begin of new event"),
                                fieldWithPath("closeEnrollmentDateTime").description("enrollment time of close of new event"),
                                fieldWithPath("beginEventDateTime").description("date time of begin of new event"),
                                fieldWithPath("endEventDateTime").description("date time of end of new event"),
                                fieldWithPath("location").description("location of new event"),
                                fieldWithPath("basePrice").description("base Price of new event"),
                                fieldWithPath("maxPrice").description("max Price of new event"),
                                fieldWithPath("limitOfEnrollment").description("limit Of Enrollment of new event"),
                                fieldWithPath("offline").description("it tells is this event is offline event or not"),
                                fieldWithPath("free").description("it tells is this event is free or not"),
                                fieldWithPath("eventStatus").description("event status"),
                                fieldWithPath("manager").description("manager 가 갑자기 생김"),
                                fieldWithPath("_links.self.href").description("link to self"),
                                fieldWithPath("_links.profile.href").description("link to profile")
                        )
                ))
        ;
    }

    @Test
    @DisplayName("없는 이벤트를 조회 했을 때 404 응답받기")
    public void getEvent404() throws Exception {
       //given

       //when
        mockMvc.perform(get("/api/events/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("이벤트를 정상적으로 수정하기")
    public void updateEvent() throws Exception {
       //given
        Event event = generateEvent(200);

        EventDto eventDto = modelMapper.map(event, EventDto.class);// event를 EventDto클래스에 담기
        String eventName = "Updated Event";
        eventDto.setName(eventName);

        //when & then
        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON) // 보내는 데이터 타입
                        .content(objectMapper.writeValueAsString(eventDto)) // 보내는 값
                        .accept(MediaTypes.HAL_JSON)) // 기대값
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(eventName))
        ;
    }

    @Test
    @DisplayName("입력값이 비어있는 경우에 이벤트 수정 실패")
    public void updateEvent400_Empty() throws Exception {
        //given
        Event event = generateEvent(200);

        EventDto eventDto = new EventDto();

        //when & then
        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON) // 보내는 데이터 타입
                        .content(objectMapper.writeValueAsString(eventDto)) // 보내는 값
                        .accept(MediaTypes.HAL_JSON)) // 기대값
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("입력값이 잘못된 경우에 이벤트 수정 실패")
    public void updateEvent400_Wrong() throws Exception {
        //given
        Event event = generateEvent(200);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(10000);

        //when & then
        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON) // 보내는 데이터 타입
                        .content(objectMapper.writeValueAsString(eventDto)) // 보내는 값
                        .accept(MediaTypes.HAL_JSON)) // 기대값
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("존재하지 않는 이벤트일 경우  이벤트 수정 실패")
    public void updateEvent404() throws Exception {
        //given
        Event event = generateEvent(200);

        EventDto eventDto = modelMapper.map(event, EventDto.class);
        eventDto.setBasePrice(20000);
        eventDto.setMaxPrice(10000);

        //when & then
        mockMvc.perform(RestDocumentationRequestBuilders.put("/api/events/999999")
                        .header(HttpHeaders.AUTHORIZATION, getBearerToken())
                        .contentType(MediaType.APPLICATION_JSON) // 보내는 데이터 타입
                        .content(objectMapper.writeValueAsString(eventDto)) // 보내는 값
                        .accept(MediaTypes.HAL_JSON)) // 기대값
                .andDo(print())
                .andExpect(status().isNotFound())
        ;
    }



    private Event generateEvent(int index) {
        Event event = Event.builder()
                .name("event " + index)
                .description("test event")
                .beginEnrollmentDateTime(LocalDateTime.of(2022, 3, 13, 21, 35))
                .closeEnrollmentDateTime(LocalDateTime.of(2022, 3, 14, 21, 35))
                .beginEventDateTime(LocalDateTime.of(2022, 3, 16, 21, 35))
                .endEventDateTime(LocalDateTime.of(2022, 3, 17, 21, 35))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타트업 팩토리")
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();
        return eventRepository.save(event);
    }

    private String getAccessToken() throws Exception {
        Account account = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        accountService.saveAccount(account);

        // when & then
        ResultActions perform = mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword())
                .param("grant_type", "password"));// 사용할 인증 타입
        MockHttpServletResponse response = perform.andReturn().getResponse();
        String responseBody = response.getContentAsString();
        Jackson2JsonParser parser = new Jackson2JsonParser();
        return parser.parseMap(responseBody).get("access_token").toString();
    }

    private String getBearerToken() throws Exception {
        return "Bearer " + getAccessToken();
    }


}
