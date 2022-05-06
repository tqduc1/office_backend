package com.cmcglobal.backend.controller.impl;

import com.cmcglobal.backend.controller.DotController;
import com.cmcglobal.backend.dto.request.dotinfo.*;
import com.cmcglobal.backend.dto.response.dot.DotDTO;
import com.cmcglobal.backend.dto.response.dot.GetDotResponse;
import com.cmcglobal.backend.service.DotService;
import com.cmcglobal.backend.utility.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DotControllerImpl implements DotController {

    @Autowired
    private DotService dotService;

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> getListDot(Integer floorId, String department, String username, String fromDate, String toDate) {
        return dotService.getListDot(floorId, department, username, fromDate, toDate);
    }

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> getListDotsByDateAndFilters(Integer floorId, String department, String username, String date) {
        return dotService.getListDotsByDateAndFilters(floorId, department, username, date);
    }

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> getListDotsByTimeRangeAndFilters(Integer floorId, String department, String username, String fromDate, String toDate) {
        return dotService.getListDotsByTimeRangeAndFilters(floorId, department, username, fromDate, toDate);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> updateUsernameAndStatus(UpdateDotInfoMapRequest updateDotInfoMapRequest) {
        return dotService.updateUsernameAndStatus(updateDotInfoMapRequest);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> resetDot(Integer dotId) {
        return dotService.resetDot(dotId);
    }

    @Override
    public ResponseEntity<BaseResponse<GetDotResponse>> getListDot(Integer buildingId, List<Integer> floorIds, String department, String username, String fromDate, String toDate, List<String> status, Integer page, Integer size) {
        return dotService.findDots(buildingId, floorIds, department, username, fromDate, toDate, status, page, size);
    }

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> searchDotOccupied(String username, String date) {
        return dotService.findDotOccupiedByUsername(username, date);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> swapDot(Integer id1, Integer id2) {
        return dotService.swapDot(id1, id2);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> reclaimDots(String role, UpdateDotInfoByTimeListRequest updateDotInfoByTimeListRequest) {
        return dotService.reclaimDots(role, updateDotInfoByTimeListRequest);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> updateDotEnable(Integer dotId) {
        return dotService.updateDotEnable(dotId);
    }
}
