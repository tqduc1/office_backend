package com.cmcglobal.backend.service;

import com.cmcglobal.backend.dto.request.dotinfo.*;
import com.cmcglobal.backend.dto.response.dot.DotDTO;
import com.cmcglobal.backend.dto.response.dot.GetDotResponse;
import com.cmcglobal.backend.utility.BaseResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DotService {

    ResponseEntity<BaseResponse<List<DotDTO>>> getListDot(Integer floorId, String department, String username, String fromDate, String toDate);

    ResponseEntity<BaseResponse<List<DotDTO>>> getListDotsByDateAndFilters(Integer floorId, String department, String username, String date);

    ResponseEntity<BaseResponse<List<DotDTO>>> getListDotsByTimeRangeAndFilters(Integer floorId, String department, String username, String fromDate, String toDate);

    ResponseEntity<BaseResponse<String>> deleteDot(Integer dotId);

    ResponseEntity<BaseResponse<GetDotResponse>> findDots(Integer buildingId, List<Integer> floorIds, String department, String username, String fromDate, String toDate, List<String> status, Integer page, Integer size);

    ResponseEntity<BaseResponse<String>> reclaimDots(String role, UpdateDotInfoByTimeListRequest request);

    ResponseEntity<BaseResponse<String>> updateUsernameAndStatus(UpdateDotInfoMapRequest request);

    ResponseEntity<BaseResponse<String>> resetDot(Integer dotId);

    ResponseEntity<BaseResponse<List<DotDTO>>> findDotOccupiedByUsername(String username, String date);

    ResponseEntity<BaseResponse<String>> swapDot(Integer id, Integer id2);

    ResponseEntity<BaseResponse<String>> updateDotEnable(Integer dotId);
}
