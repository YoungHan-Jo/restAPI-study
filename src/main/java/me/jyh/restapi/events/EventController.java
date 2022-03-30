package me.jyh.restapi.events;

import lombok.RequiredArgsConstructor;
import me.jyh.restapi.accounts.Account;
import me.jyh.restapi.accounts.AccountAdapter;
import me.jyh.restapi.accounts.CurrentUser;
import me.jyh.restapi.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE) // HAL_JSON으로 변환해서 응답 객체를 보낼 것임
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping // requestMapping으로 생략
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {
        if (errors.hasErrors()) {
            return badRequest(errors);
            // 에러를 바디에 담아서 보내면 보내질거 같지만 안보내짐(errors는 자바빈 스펙을 준수하고 있지 않아서 json으로 변환 할 수 없기때문)
            // 밑에 body(event)에서 event는 자바 bean 스팩을 준수하고 있기 때문에 objectMapper(BeanSerializer)를 사용해서 json으로 자동 변환 가능함
            // -> 수동으로 Serializer 클래스를 만들어서 @JsonComponent로 objectMapper에 등록 시키면 해결됨
        }
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class); // eventDto에 있는걸 Event클래스로 담기
        event.update();
        event.setManager(currentUser); // 매니저 설정 가능
        Event savedEvent = eventRepository.save(event);
        // URI 만들기 // methodOn 매서드 호출
//        URI createdUri = linkTo(methodOn(EventController.class).createEvent(null)).slash("{id}").toUri();
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(savedEvent.getId()); // @RequestMapping으로 생략가능
        URI createdUri = selfLinkBuilder.toUri();

        EntityModel<Event> eventEntityModel = EntityModel.of(event,
                selfLinkBuilder.withSelfRel(), // self 링크
                linkTo(EventController.class).withRel("query-events"),
                selfLinkBuilder.withRel("update-event"),
                Link.of("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(eventEntityModel);
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                      PagedResourcesAssembler<Event> assembler,
                                      @CurrentUser Account account) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User user = (User)authentication.getPrincipal(); // @AuthenticationPrincipal User user로 바로 받아와짐

        Page<Event> page = eventRepository.findAll(pageable);
        PagedModel<EntityModel<Event>> entityModels = assembler.toModel(page, e -> new EventResource(e));
        entityModels.add(Link.of("/docs/index.html#resources-events-list").withRel("profile"));

        if (account != null) { // 인증 받았을 경우 이벤트 생성 링크 추가
            entityModels.add(linkTo(EventController.class).withRel("create-event"));
        }

        return ResponseEntity.ok(entityModels);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEntity(@PathVariable Long id,
                                    @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));

        if (event.getManager().equals(currentUser)) { // 본인이 쓴 글일때만 업데이트 링크 추가
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }

        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvents(@PathVariable Long id,
                                       @RequestBody @Valid EventDto eventDto,
                                       Errors errors,
                                       @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        this.eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event existingEvent = optionalEvent.get();

        if (!existingEvent.getManager().equals(currentUser)) { // 작성자가 아니면 권한 없음 응답
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        this.modelMapper.map(eventDto, existingEvent); // map(A,B) A에서 B로 전부 Setter
        Event savedEvent = this.eventRepository.save(existingEvent);

        EntityModel<Event> eventResource = EntityModel.of(savedEvent,
                Link.of("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventResource);

    }


    private ResponseEntity<EntityModel<Errors>> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

}

