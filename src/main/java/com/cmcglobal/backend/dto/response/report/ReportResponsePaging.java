package com.cmcglobal.backend.dto.response.report;

import com.cmcglobal.backend.dto.response.Metadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponsePaging {
    private Metadata metadata;
    private String exportDate;
    private List<ReportResponse> reportResponses;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportResponse {
        private String group;
        private String department;
        private String buildingName;
        private String floorName;
        private Integer numberOfAllocatedDot;
        private Integer numberOfOccupiedDot;
    }
}
