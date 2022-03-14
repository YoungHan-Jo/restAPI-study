package me.jyh.restapi.events;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE) // HAL_JSON으로 변환해서 응답 객체를 보낼 것임
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping // requestMapping으로 생략
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
            // 에러를 바디에 담아서 보내면 보내질거 같지만 안보내짐(errors는 자바빈 스펙을 준수하고 있지 않아서 json으로 변환 할 수 없기때문)
            // 밑에 body(event)에서 event는 자바 bean 스팩을 준수하고 있기 때문에 objectMapper(BeanSerializer)를 사용해서 json으로 자동 변환 가능함
            // -> 수동으로 Serializer 클래스를 만들어서 @JsonComponent로 objectMapper에 등록 시키면 해결됨 

        }

        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        Event savedEvent = eventRepository.save(event);
        // URI 만들기 // methodOn 매서드 호출
//        URI createdUri = linkTo(methodOn(EventController.class).createEvent(null)).slash("{id}").toUri();
        URI createdUri = linkTo(EventController.class).slash(savedEvent.getId()).toUri();// @RequestMapping으로 생략가능

        return ResponseEntity.created(createdUri).body(event);
    }
}

