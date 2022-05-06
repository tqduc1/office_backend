package com.cmcglobal.backend.dto.response.dotInfoByTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DotInfoByTimeDTO {
    private Integer id;

    private String status;

    private LocalDate fromDate;

    private LocalDate toDate;

    private String userId;

    private String username;

    private String fullName;

    private String member;

    private String owner;

    private String ownerFullName;

    private String department;

    private String group;

    private Integer dotId;
}
