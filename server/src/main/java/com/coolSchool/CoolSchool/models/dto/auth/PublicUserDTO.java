package com.coolSchool.CoolSchool.models.dto.auth;

import com.coolSchool.CoolSchool.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {
    private Long id;
    private String firstname;

    @JsonProperty("username")
    private String usernameField;
    private String email;
    private Role role;
    private String description;
}
