package com.cmcglobal.backend.dto.request.dotinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDotInfoMapRequest {
    private Integer id;
    private String username;
    private String status;
}
