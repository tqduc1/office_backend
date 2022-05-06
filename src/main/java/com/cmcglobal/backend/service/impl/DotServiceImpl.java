package com.cmcglobal.backend.service.impl;

import com.cmcglobal.backend.constant.Constant;
import com.cmcglobal.backend.constant.ErrorMessage;
import com.cmcglobal.backend.dto.request.dot.UpdateDotListRequest;
import com.cmcglobal.backend.dto.request.dot.UpdateDotMapRequest;
import com.cmcglobal.backend.dto.request.dotinfo.UpdateDotInfoByTimeListRequest;
import com.cmcglobal.backend.dto.request.dotinfo.UpdateDotInfoMapRequest;
import com.cmcglobal.backend.dto.response.Metadata;
import com.cmcglobal.backend.dto.response.dot.DotDTO;
import com.cmcglobal.backend.dto.response.dot.GetDotResponse;
import com.cmcglobal.backend.entity.Dot;
import com.cmcglobal.backend.entity.DotInfoByTime;
import com.cmcglobal.backend.entity.UserFlattened;
import com.cmcglobal.backend.mapper.dot.DotResponseMapper;
import com.cmcglobal.backend.repository.DotInfoByTimeRepository;
import com.cmcglobal.backend.repository.DotRepository;
import com.cmcglobal.backend.repository.UserFlattenedRepository;
import com.cmcglobal.backend.service.DotService;
import com.cmcglobal.backend.utility.BaseResponse;
import com.cmcglobal.backend.utility.ResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.cmcglobal.backend.constant.Constant.YYYY_MM_DD;

@Service
@Slf4j
public class DotServiceImpl extends BaseService implements DotService {
    @Autowired
    private DotRepository dotRepository;

    @Autowired
    private DotInfoByTimeRepository dotInfoByTimeRepository;

    @Autowired
    private DotResponseMapper dotResponseMapper;

    @Autowired
    private UserFlattenedRepository userFlattenedRepository;

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> getListDot(Integer floorId, String department, String username, String fromDate, String toDate) {
        List<String> userIdInGroup = super.getUserIdInGroup(department);
        String userId = super.getUsername(username);
        String fromDateFilter = super.getParseDate(fromDate);
        String toDateFilter = super.getParseDate(toDate);
        List<Dot> dotList = dotRepository.findDotsByConditions(floorId, userIdInGroup, fromDateFilter, toDateFilter, userId);
        return ResponseFactory.success(HttpStatus.OK, dotResponseMapper.toListDotDTO(dotList), ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> getListDotsByDateAndFilters(Integer floorId, String department, String username, String date) {
        List<String> userIdInGroup = super.getUserIdInGroup(department);
        String userId = super.getUsername(username);
        String dateFilter = super.getParseDate(date);
        List<Dot> dotList = dotRepository.findDotsByDateAndFilters(floorId, userIdInGroup, dateFilter, userId);
        List<Dot> dotResultNoDuplicates = dotList.stream().distinct().collect(Collectors.toList());

        for (Dot dot: dotResultNoDuplicates){
            Integer dotId = dot.getId();
            dot.setListDotInfoByTime(dotInfoByTimeRepository.findAllByDotIdByDate(dateFilter, dotId));
        }
        return ResponseFactory.success(HttpStatus.OK, dotResponseMapper.toListDotDTO(dotResultNoDuplicates), ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> getListDotsByTimeRangeAndFilters(Integer floorId, String department, String username, String fromDate, String toDate) {
        List<String> userIdInGroup = super.getUserIdInGroup(department);
        String userId = super.getUsername(username);
        String fromDateFilter = super.getParseDate(fromDate);
        String toDateFilter = super.getParseDate(toDate);
//        List<Dot> dotList = dotRepository.findDotsByConditionsInTimeRange(floorId, userIdInGroup, fromDateFilter, toDateFilter, userId);
        List<Dot> dotList = dotRepository.findDotsByFilters(floorId, userIdInGroup, userId);
        List<Dot> dotResultNoDuplicates = dotList.stream().distinct().collect(Collectors.toList());
        for (Dot dot: dotResultNoDuplicates){
            Integer dotId = dot.getId();
            dot.setListDotInfoByTime(dotInfoByTimeRepository.findAllByDotIdInTimeRange(fromDateFilter, toDateFilter, dotId));
        }
        return ResponseFactory.success(HttpStatus.OK, dotResponseMapper.toListDotDTO(dotResultNoDuplicates), ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> deleteDot(Integer dotId) {
        if (dotRepository.existsById(dotId)) {
            dotRepository.deleteById(dotId);
            return ResponseFactory.success(HttpStatus.OK, ErrorMessage.DELETE_DOT_SUCCESSFULLY, ErrorMessage.SUCCESS);
        }
        return ResponseFactory.error(HttpStatus.OK, ErrorMessage.DELETE_DOT_FAIL, ErrorMessage.FAILED);
    }

    @Override
    public ResponseEntity<BaseResponse<GetDotResponse>> findDots(Integer buildingId, List<Integer> floorIds, String department, String username, String fromDate, String toDate, List<String> status, Integer page, Integer size) {
        floorIds = super.getFloorIdByBuilding(floorIds, buildingId);
        List<String> userIdInGroup = super.getUserIdInGroup(department);
        String userId = super.getUsername(username);
        String fromDateFilter = super.getParseDate(fromDate);
        String toDateFilter = super.getParseDate(toDate);
        Pageable paging = PageRequest.of(page - 1, size);
        Page<Dot> dotResult = dotRepository.findDotInfoInListScreen(floorIds, userIdInGroup, userId, fromDateFilter, toDateFilter, status, paging);
        List<Dot> dotResultNoDuplicates = dotResult.stream().distinct().collect(Collectors.toList());

        // Mapping Dot info into DotDTO (TODO: Refactor)
        List<DotDTO> dotListDTO = new ArrayList<>();
        for (Dot dot: dotResultNoDuplicates){
            List<DotInfoByTime> listDotInfoByTime = dot.getListDotInfoByTime();

            for (DotInfoByTime dotInfoByTime: listDotInfoByTime){
                DotDTO dotDTO = new DotDTO();
                // IMPORTANT: id = dot_info_by_time id (not dot id)
                dotDTO.setId(dotInfoByTime.getId());
                UserFlattened user = userFlattenedRepository.findByUserName(dotInfoByTime.getMember());
                if (user != null) {
                    dotDTO.setUserId(user.getId());
                    dotDTO.setUsername(user.getUserName());
                    dotDTO.setFullName(user.getFullName());
                }
                UserFlattened owner = userFlattenedRepository.findByUserName(dotInfoByTime.getOwner());
                if (owner != null) {
                    dotDTO.setOwner(owner.getFullName());
                    dotDTO.setDepartment(owner.getDepartmentName());
                    dotDTO.setGroup(owner.getParentDepartmentName() != null ? owner.getParentDepartmentName() : owner.getDepartmentName());
                }
                dotDTO.setFloorName(dot.getFloor().getFloorName());
                dotDTO.setFloorId(dot.getFloor().getId());
                dotDTO.setBuildingName(dot.getFloor().getBuilding().getBuildingName());
                dotDTO.setBuildingId(dot.getFloor().getBuilding().getId());
                dotDTO.setCoordinateX(dot.getCoordinateX());
                dotDTO.setCoordinateY(dot.getCoordinateY());
                if (dotInfoByTime.getFromDate() != null) {
                    dotDTO.setFromDate(dotInfoByTime.getFromDate().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)));
                }
                if (dotInfoByTime.getToDate() != null) {
                    dotDTO.setToDate(dotInfoByTime.getToDate().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)));
                }
                dotDTO.setPrice(dot.getFloor().getDotPricePerMonth());
                dotDTO.setStatus(dotInfoByTime.getStatus());
                dotDTO.setType(dot.getType());
                dotDTO.setIsActive(dot.getIsActive());

                dotListDTO.add(dotDTO);
            }
        }
        return ResponseFactory.success(HttpStatus.OK, new GetDotResponse(dotListDTO, Metadata.build(dotResult)), ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> reclaimDots(String role, UpdateDotInfoByTimeListRequest request) {
        if (Objects.equals(role, Constant.Role.ADMIN)) {
            dotInfoByTimeRepository.deleteDotInfoByTimeByListId(request.getListDotInfoByTimeIds());
        } else if (Objects.equals(role, Constant.Role.DU_LEAD)){
            UserFlattened userLog = poaService.getUserInfoByUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            if (userLog == null) {
                return ResponseFactory.success(HttpStatus.NOT_FOUND, ErrorMessage.USER_NOT_FOUND, ErrorMessage.FAILED);
            }
            dotInfoByTimeRepository.updateDotInfoByTimeByListId(request.getListDotInfoByTimeIds());
        }
        return ResponseFactory.success(HttpStatus.OK, ErrorMessage.UPDATE_SUCCESSFULLY, ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> updateUsernameAndStatus(UpdateDotInfoMapRequest request) {
        if (!dotInfoByTimeRepository.existsById(request.getId())) {
            log.error("Id dot doesn't exist: {}", request.getId());
            return ResponseFactory.error(HttpStatus.OK, ErrorMessage.UPDATE_FAILED, ErrorMessage.FAILED);
        }
        // check if in the same fromDate and toDate provided in the request having existing member -> 1 person cannot have 2 places at same time
        DotInfoByTime dotInfoByTime = dotInfoByTimeRepository.getOne(request.getId());
        if (dotInfoByTimeRepository.existsByMemberAndFromDateAndToDateAndStatusIs(request.getUsername(), dotInfoByTime.getFromDate(), dotInfoByTime.getToDate(), Constant.StatusType.OCCUPIED)) {
            log.error("This user already has a seat:{}", request.getUsername());
            return ResponseFactory.error(HttpStatus.OK, ErrorMessage.UPDATE_FAILED, ErrorMessage.SEAT_EXISTED);
        }
        UserFlattened user = poaService.getUserInfoByUsername(request.getUsername());
        if (user == null) {
            return ResponseFactory.error(HttpStatus.NOT_FOUND, ErrorMessage.USER_NOT_FOUND, ErrorMessage.FAILED);
        }
        dotInfoByTimeRepository.updateUserAndStatus(user.getUserName(), request.getStatus(), request.getId());
        return ResponseFactory.success(HttpStatus.OK, ErrorMessage.UPDATE_SUCCESSFULLY, ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> resetDot(Integer dotId) {
        dotRepository.resetDots(Collections.singletonList(dotId));
        return ResponseFactory.success(HttpStatus.OK, ErrorMessage.DELETE_DOT_SUCCESSFULLY, ErrorMessage.SUCCESS);
    }

//    @Override
//    public ResponseEntity<BaseResponse<List<DotDTO>>> findDotOccupiedByUsername(String username) {
//        List<UserFlattened> users = poaService.getListUserInfoByUsername(username);
//        List<String> usernames = users.stream().map(UserFlattened::getUserName).collect(Collectors.toList());
//        List<Dot> dots = dotRepository.findAllByMemberInAndStatusIs(usernames, Constant.StatusType.OCCUPIED);
//        return ResponseFactory.success(HttpStatus.OK, dotResponseMapper.toListDotDTO(dots), ErrorMessage.SUCCESS);
//    }

    @Override
    public ResponseEntity<BaseResponse<List<DotDTO>>> findDotOccupiedByUsername(String username, String date) {
        List<UserFlattened> users = poaService.getListUserInfoByUsername(username);
        List<String> usernames = users.stream().map(UserFlattened::getUserName).collect(Collectors.toList());
        String dateFilter = super.getParseDate(date);

//        List<Dot> dotList = dotRepository.findAllByMemberInAndStatusIs(usernames, Constant.StatusType.OCCUPIED);
//        List<Dot> dots = dotInfoByTimeRepository.findAllByMemberInAndStatusIs(usernames, Constant.StatusType.OCCUPIED);

        // Mapping Dot info into DotDTO (TODO: Refactor)
        List<DotInfoByTime> dotInfoByTimeList = dotInfoByTimeRepository.findAllByMemberInAndStatusIsAndByDate(usernames, Constant.StatusType.OCCUPIED, dateFilter);
        List<DotDTO> dotListDTO = new ArrayList<>();
        for (DotInfoByTime dotInfoByTime: dotInfoByTimeList){
            DotDTO dotDTO = new DotDTO();
            Dot dot = dotRepository.getOne(dotInfoByTime.getDot().getId());
            dotDTO.setId(dot.getId());
            UserFlattened user = userFlattenedRepository.findByUserName(dotInfoByTime.getMember());
            if (user != null) {
                dotDTO.setUserId(user.getId());
                dotDTO.setUsername(user.getUserName());
                dotDTO.setFullName(user.getFullName());
            }
            UserFlattened owner = userFlattenedRepository.findByUserName(dotInfoByTime.getOwner());
            if (owner != null) {
                dotDTO.setOwner(owner.getFullName());
                dotDTO.setDepartment(owner.getDepartmentName());
                dotDTO.setGroup(owner.getParentDepartmentName() != null ? owner.getParentDepartmentName() : owner.getDepartmentName());
            }
            dotDTO.setFloorName(dot.getFloor().getFloorName());
            dotDTO.setFloorId(dot.getFloor().getId());
            dotDTO.setBuildingName(dot.getFloor().getBuilding().getBuildingName());
            dotDTO.setBuildingId(dot.getFloor().getBuilding().getId());
            dotDTO.setCoordinateX(dot.getCoordinateX());
            dotDTO.setCoordinateY(dot.getCoordinateY());
            if (dotInfoByTime.getFromDate() != null) {
                dotDTO.setFromDate(dotInfoByTime.getFromDate().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)));
            }
            if (dotInfoByTime.getToDate() != null) {
                dotDTO.setToDate(dotInfoByTime.getToDate().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)));
            }
            dotDTO.setPrice(dot.getFloor().getDotPricePerMonth());
            dotDTO.setStatus(dotInfoByTime.getStatus());
            dotDTO.setType(dot.getType());
            dotDTO.setIsActive(dot.getIsActive());
            dotListDTO.add(dotDTO);
        }
        return ResponseFactory.success(HttpStatus.OK, dotListDTO, ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> swapDot(Integer id1, Integer id2) {
        Dot dot1 = dotRepository.findById(id1).orElseThrow(() -> new NotFoundException("Dot id not found:" + id1));
        Dot dot2 = dotRepository.findById(id2).orElseThrow(() -> new NotFoundException("Dot id not found:" + id2));
        if (dot1.getMember() == null || dot2.getMember() == null) {
            throw new BadRequestException("These dot don't have member");
        }
        String member1 = dot1.getMember();
        String member2 = dot2.getMember();
        dot1.setMember(member2);
        dotRepository.save(dot1);
        dot2.setMember(member1);
        dotRepository.save(dot2);

        return ResponseFactory.success(HttpStatus.OK, ErrorMessage.SWAP_SUCCESSFULLY, ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> updateDotEnable(Integer dotId) {
        Dot updatingDot = dotRepository.getOne(dotId);
        updatingDot.setIsActive(!updatingDot.getIsActive());
        dotRepository.save(updatingDot);
        return ResponseFactory.success(HttpStatus.OK, ErrorMessage.UPDATE_SUCCESSFULLY, ErrorMessage.SUCCESS);
    }
}
