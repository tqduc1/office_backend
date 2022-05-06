package com.cmcglobal.backend.dto.request.dotinfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDotInfoByTimeListRequest {
    private List<Integer> listDotInfoByTimeIds;
    private String toDate;
    private String status;
    private String fromDate;
}
