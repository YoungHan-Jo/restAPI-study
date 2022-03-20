package me.jyh.restapi.accounts;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    private String email;

    private String password;

    @ElementCollection(fetch = FetchType.EAGER) // Set 이라서 여러개, + 바로 가져와야할 가능성이 높아서 즉시로딩
    @Enumerated(EnumType.STRING)
    private Set<AccountRole> roles;
}
