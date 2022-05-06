package com.cmcglobal.backend.service.impl;

import com.cmcglobal.backend.constant.ErrorMessage;
import com.cmcglobal.backend.dto.response.Metadata;
import com.cmcglobal.backend.dto.response.dot.ReportDotResponse;
import com.cmcglobal.backend.dto.response.report.ReportResponsePaging;
import com.cmcglobal.backend.entity.Dot;
import com.cmcglobal.backend.entity.UserFlattened;
import com.cmcglobal.backend.entity.immutable.DotsManagementReport;
import com.cmcglobal.backend.excel.DotExcel;
import com.cmcglobal.backend.excel.ReportExcel;
import com.cmcglobal.backend.mapper.dot.DotResponseMapper;
import com.cmcglobal.backend.repository.DotRepository;
import com.cmcglobal.backend.repository.DotsManagementReportRepository;
import com.cmcglobal.backend.service.ReportService;
import com.cmcglobal.backend.utility.BaseResponse;
import com.cmcglobal.backend.utility.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl extends BaseService implements ReportService {

    @Autowired
    private DotRepository dotRepository;

    @Autowired
    private DotResponseMapper dotResponseMapper;

    @Autowired
    private DotsManagementReportRepository dotsManagementReportRepository;

//    @Override
//    public void export(HttpServletResponse response, Integer buildingId, List<Integer> floorIds, String department, String username, String fromDate, String toDate, List<String> status) throws IOException {
//        floorIds = super.getFloorIdByBuilding(floorIds, buildingId);
//        List<String> userIdInGroup = super.getUserIdInGroup(department);
//        String userId = super.getUsername(username);
//        fromDate = fromDate.equals("") ? null : fromDate;
//        toDate = toDate.equals("") ? null : toDate;
//        List<Dot> dots = dotRepository.findDotsReport(floorIds, userIdInGroup, userId, fromDate, toDate, status);
//        List<ReportDotResponse.DotDTO> dotDTOs = dotResponseMapper.toListDotReportDTO(dots);
//
//        response.setContentType("application/octet-stream");
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=dot.xlsx";
//        response.setHeader(headerKey, headerValue);
//        DotExcel excelExporter = new DotExcel(dotDTOs, fromDate, toDate);
//        excelExporter.export(response);
//    }

    @Override
    public ResponseEntity<BaseResponse<ReportDotResponse>> findReportDots(Integer buildingId, List<Integer> floorIds, String department, String username, String fromDate, String toDate, List<String> status, Integer page, Integer size) {
        floorIds = super.getFloorIdByBuilding(floorIds, buildingId);
        List<String> userIdInGroup = getUserIdInGroup(department);
        String userId = getUsername(username);
        fromDate = fromDate.equals("") ? null : fromDate;
        toDate = toDate.equals("") ? null : toDate;
        Pageable paging = PageRequest.of(page - 1, size);
        Page<Dot> dotResult = dotRepository.findDotsReport(floorIds, userIdInGroup, userId, fromDate, toDate, status, paging);
        List<ReportDotResponse.DotDTO> dotListDTO = dotResponseMapper.toListDotReportDTO(dotResult.getContent());
        return ResponseFactory.success(HttpStatus.OK, new ReportDotResponse(dotListDTO, Metadata.build(dotResult)), ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<ReportResponsePaging>> displayDotManagementReport(String exportDate, String department, Integer buildingId, List<Integer> floorIds, Integer page, Integer size) {

        // Create a view table with required information
        floorIds = super.getFloorIdByBuilding(floorIds, buildingId);
        List<String> userIdInGroup = getUserIdInGroup(department);
        exportDate = exportDate.equals("") ? null : exportDate;
        Pageable paging = PageRequest.of(page - 1, size);
        dotsManagementReportRepository.updateExportDateDotsManagementReport(exportDate);
        Page<DotsManagementReport> result = dotsManagementReportRepository.displayDotsManagementReport(floorIds, userIdInGroup, paging);

        List<ReportResponsePaging.ReportResponse> reportResponses = new ArrayList<>();
        for (DotsManagementReport dot: result) {
            ReportResponsePaging.ReportResponse stackReport = new ReportResponsePaging.ReportResponse();
            UserFlattened owner = poaService.getUserInfoByUsername(dot.getOwner());
            stackReport.setDepartment(owner.getDepartmentName());
            stackReport.setGroup(owner.getParentDepartmentName());
            stackReport.setBuildingName(dotRepository.getOne(dot.getFloorId()).getFloor().getBuilding().getBuildingName());
            stackReport.setFloorName(dotRepository.getOne(dot.getFloorId()).getFloor().getFloorName());
            stackReport.setNumberOfAllocatedDot(dot.getNumberOfAllocatedDots());
            stackReport.setNumberOfOccupiedDot(dot.getNumberOfOccupiedDots());
            reportResponses.add(stackReport);
        }
        return ResponseFactory.success(HttpStatus.OK, new ReportResponsePaging(Metadata.build(result), exportDate, reportResponses), ErrorMessage.SUCCESS);
    }

    @Override
    public void export(HttpServletResponse response, String exportDate, String department, Integer buildingId, List<Integer> floorIds) throws IOException {
        // Create a view table with required information
        floorIds = super.getFloorIdByBuilding(floorIds, buildingId);
        List<String> userIdInGroup = getUserIdInGroup(department);
        exportDate = exportDate.equals("") ? null : exportDate;
        dotsManagementReportRepository.updateExportDateDotsManagementReport(exportDate);
        List<DotsManagementReport> result = dotsManagementReportRepository.exportDotsManagementReport(floorIds, userIdInGroup);

        List<ReportResponsePaging.ReportResponse> reportResponses = new ArrayList<>();
        for (DotsManagementReport dot: result) {
            ReportResponsePaging.ReportResponse stackReport = new ReportResponsePaging.ReportResponse();
            UserFlattened owner = poaService.getUserInfoByUsername(dot.getOwner());
            stackReport.setDepartment(owner.getDepartmentName());
            stackReport.setGroup(owner.getParentDepartmentName());
            stackReport.setBuildingName(dotRepository.getOne(dot.getFloorId()).getFloor().getBuilding().getBuildingName());
            stackReport.setFloorName(dotRepository.getOne(dot.getFloorId()).getFloor().getFloorName());
            stackReport.setNumberOfAllocatedDot(dot.getNumberOfAllocatedDots());
            stackReport.setNumberOfOccupiedDot(dot.getNumberOfOccupiedDots());
            reportResponses.add(stackReport);
        }
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=dot.xlsx";
        response.setHeader(headerKey, headerValue);
        ReportExcel excelExporter = new ReportExcel(reportResponses, exportDate);
        excelExporter.export(response);
    }
}
