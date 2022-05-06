package com.cmcglobal.backend.service;

import com.cmcglobal.backend.dto.response.dot.ReportDotResponse;
import com.cmcglobal.backend.dto.response.report.ReportResponsePaging;
import com.cmcglobal.backend.utility.BaseResponse;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface ReportService {
//    void export(HttpServletResponse response, Integer buildingId, List<Integer> floorIds, String department, String username, String fromDate, String toDate, List<String> status) throws IOException;

    void export(HttpServletResponse response, String exportDate, String department, Integer buildingId, List<Integer> floorIds) throws IOException;

    ResponseEntity<BaseResponse<ReportDotResponse>> findReportDots(Integer buildingId, List<Integer> floorIds, String department, String username, String fromDate, String toDate, List<String> status, Integer page, Integer size);

    ResponseEntity<BaseResponse<ReportResponsePaging>> displayDotManagementReport(String exportDate, String department, Integer buildingId, List<Integer> floorIds, Integer page, Integer size);
}
