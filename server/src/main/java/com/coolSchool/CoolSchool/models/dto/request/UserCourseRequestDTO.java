package com.coolSchool.CoolSchool.models.dto.request;

import com.coolSchool.CoolSchool.models.dto.common.UserCourseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseRequestDTO extends UserCourseDTO {
    private Long userId;
    private Long courseId;
}
