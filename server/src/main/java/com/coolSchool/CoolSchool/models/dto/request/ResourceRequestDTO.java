package com.coolSchool.CoolSchool.models.dto.request;

import com.coolSchool.CoolSchool.models.dto.common.ResourceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequestDTO extends ResourceDTO {
    private Long fileId;
    private Long subsectionId;
}
