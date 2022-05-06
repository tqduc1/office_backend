package com.cmcglobal.backend.service.impl;

import com.cmcglobal.backend.constant.Constant;
import com.cmcglobal.backend.config.SecurityContextUtils;
import com.cmcglobal.backend.constant.Constant;
import com.cmcglobal.backend.constant.Constant.StatusType;
import com.cmcglobal.backend.constant.Constant.TicketType;
import com.cmcglobal.backend.constant.ErrorMessage;
import com.cmcglobal.backend.dto.request.ticket.ReviewTicketRequest;
import com.cmcglobal.backend.dto.request.ticket.TicketCreateRequest;
import com.cmcglobal.backend.dto.response.Metadata;
import com.cmcglobal.backend.dto.response.ticket.GetTicketResponse;
import com.cmcglobal.backend.dto.response.ticket.TicketDetail;
import com.cmcglobal.backend.entity.*;
import com.cmcglobal.backend.entity.immutable.TicketDotView;
import com.cmcglobal.backend.exception.TicketException;
import com.cmcglobal.backend.mapper.dot.DotResponseMapper;
import com.cmcglobal.backend.mapper.ticket.TicketDotResponseMapper;
import com.cmcglobal.backend.mapper.ticket.TicketRequestMapper;
import com.cmcglobal.backend.repository.DotInfoByTimeRepository;
import com.cmcglobal.backend.repository.DotRepository;
import com.cmcglobal.backend.repository.TicketDotViewRepository;
import com.cmcglobal.backend.repository.TicketRepository;
import com.cmcglobal.backend.service.MailService;
import com.cmcglobal.backend.service.TicketService;
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
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.ws.rs.BadRequestException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cmcglobal.backend.constant.Constant.*;

@Service
@Slf4j
@Transactional(rollbackFor = {Exception.class, TicketException.class})
public class TicketServiceImpl extends BaseService implements TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private DotRepository dotRepository;

    @Autowired
    private DotInfoByTimeRepository dotInfoByTimeRepository;

    @Autowired
    private TicketDotViewRepository ticketDotViewRepository;

    @Autowired
    private TicketRequestMapper ticketRequestMapper;

    @Autowired
    private TicketDotResponseMapper ticketDotResponseMapper;

    @Autowired
    private DotResponseMapper dotResponseMapper;

    @Autowired
    private MailService mailService;

    @Override
    public ResponseEntity<BaseResponse<String>> createTicketOrderDot(TicketCreateRequest request) {
        if (Objects.equals(request.getType(), TicketType.BOOK)  && !dotInfoByTimeRepository.findAllByDotsAndTimeRange(request.getDotIds(), request.getFromDate().toString(), request.getToDate().toString()).isEmpty()){
            // If exists dotInfo of requested dots in the given time range -> seats are occupied
            return ResponseFactory.error(HttpStatus.BAD_REQUEST, ErrorMessage.SEATS_OCCUPIED, ErrorMessage.SEATS_OCCUPIED);
        } else if (Objects.equals(request.getType(), TicketType.CLAIM)){
            // check if in the same fromDate and toDate provided in the request having existing member -> 1 person cannot have 2 places at same time
            if (dotInfoByTimeRepository.existsByMemberAndFromDateAndToDateAndStatusIs(request.getOwner(), request.getFromDate(), request.getToDate(), StatusType.OCCUPIED)) {
                log.error("This user already has a seat:{}", request.getOwner());
                return ResponseFactory.error(HttpStatus.OK, ErrorMessage.UPDATE_FAILED, ErrorMessage.SEAT_EXISTED);
            }
            // check 1 person cannot have more than 1 book
            else if (dotInfoByTimeRepository.existsByMemberAndFromDateAndToDateAndStatusIs(request.getOwner(), request.getFromDate(), request.getToDate(), StatusType.BOOKED)) {
                return ResponseFactory.error(HttpStatus.OK, ErrorMessage.UPDATE_FAILED, ErrorMessage.TOO_MANY_CLAIMS);
            }
        }

        List<Dot> dotList = dotRepository.findAllById(request.getDotIds());
        Ticket ticket = ticketRequestMapper.toEntity(request);
        UserFlattened user = poaService.getUserInfoByUsername(request.getOwner());
        String username = user.getUserName();
        ticket.setOwner(username);
        ticket.setCreatedBy(username);
        ticket.setUpdatedBy(username);
        ticket.setDots(dotList);
        ticketRepository.save(ticket);

        if (Objects.equals(request.getType(), TicketType.BOOK)){
            // When book ticket is created -> create records of dotInformation
            for (Integer dotId: request.getDotIds()) {
                DotInfoByTime dotInfoByTime = new DotInfoByTime();
                dotInfoByTime.setFromDate(ticket.getFromDate());
                dotInfoByTime.setToDate(ticket.getToDate());
                dotInfoByTime.setStatus(StatusType.BOOKED);
                dotInfoByTime.setOwner(ticket.getOwner());
                dotInfoByTime.setMember(ticket.getOwner());
                dotInfoByTime.setDot(dotRepository.getOne(dotId));
                dotInfoByTimeRepository.save(dotInfoByTime);
            }
        } else if (Objects.equals(request.getType(), TicketType.CLAIM)) {
            // When claim ticket is created -> create records of dotInformation
            for (Integer dotId: request.getDotIds()) {
                DotInfoByTime allocatedDotInfo = dotInfoByTimeRepository.findByDot(dotRepository.getOne(dotId));
                DotInfoByTime dotInfoByTime = new DotInfoByTime();
                dotInfoByTime.setFromDate(ticket.getFromDate());
                dotInfoByTime.setToDate(ticket.getToDate());
                dotInfoByTime.setStatus(StatusType.BOOKED);
                dotInfoByTime.setOwner(allocatedDotInfo.getOwner());
                dotInfoByTime.setMember(ticket.getOwner());
                dotInfoByTime.setDot(dotRepository.getOne(dotId));
                dotInfoByTimeRepository.save(dotInfoByTime);
            }
        }
        // send mail after creating ticket
        if (!SecurityContextUtils.getUserRoles().contains(Role.ADMIN)) {
            try {
                mailService.sendMail(new Mail(request.getType(), Action.CREATE, request.getOwner(), LocalDateTime.now()));
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseFactory.error(HttpStatus.CREATED, ErrorMessage.CREATE_TICKET_SUCCESS, ErrorMessage.APPROVER_NOT_FOUND);
            }
        }
        return ResponseFactory.success(HttpStatus.CREATED, ErrorMessage.CREATE_TICKET_SUCCESS, ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<GetTicketResponse>> getTicketList(Integer buildingId, List<Integer> floorIds, String department, String date, String username, String role, List<String> status, Integer page, Integer size) {
        floorIds = getFloorIdByBuilding(floorIds, buildingId);
        List<String> managersUsername = new ArrayList<>();
        String adminUsername = null;
        if (Role.ADMIN.equals(role)) {
            if (username.equals("")) {
                adminUsername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
            managersUsername = getManagersUsername();
        }
        List<String> userIdInGroup = getUserIdInGroup(department);
        username = getUsername(username);
        String dateFilter = getParseDate(date);

        Pageable paging = PageRequest.of(page - 1, size);
        Page<TicketDotView> ticketDots = ticketDotViewRepository.findTicketsByConditions(floorIds, userIdInGroup, managersUsername, username, adminUsername, dateFilter, status, paging);

        return ResponseFactory.success(HttpStatus.OK, new GetTicketResponse(ticketDotResponseMapper.toListDTO(ticketDots.getContent()), Metadata.build(ticketDots)), ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> reviewTicket(ReviewTicketRequest request) {
        List<Integer> ticketIds = request.getTickets().stream().map(ReviewTicketRequest.TicketDTO::getId).collect(Collectors.toList());
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        if (tickets.size() != ticketIds.size()) {
            return ResponseFactory.success(HttpStatus.NOT_FOUND, ErrorMessage.NOT_FOUND, ErrorMessage.FAILED);
        }
        ticketRepository.updateTickets(ticketIds, request.getAction());
        if (Action.APPROVE.equals(request.getAction())) {
            for (ReviewTicketRequest.TicketDTO ticketDTO : request.getTickets()) {
                Ticket ticket = ticketRepository.getTicketByID(ticketDTO.getId());
                List<Integer> dotIds = ticket.getDots().stream().map(Dot::getId).collect(Collectors.toList());
                switch (ticketDTO.getType()) {
                    case TicketType.BOOK:
                        dotRepository.updateOwnerUserAndStatus(ticket.getOwner(), StatusType.ALLOCATED, dotIds, ticket.getFromDate().toString(), ticket.getToDate().toString());
                        // When booked ticket is approved -> update records of dotInformation
                        for (Integer dotId: dotIds) {
                            DotInfoByTime dotInfoByTime = dotInfoByTimeRepository.findByDotAndStatusAndFromDateAndToDate(dotRepository.getOne(dotId), StatusType.BOOKED, ticket.getFromDate(), ticket.getToDate());
                            dotInfoByTime.setStatus(StatusType.ALLOCATED);
                            dotInfoByTimeRepository.save(dotInfoByTime);
                        }
                        break;
                    case TicketType.CLAIM:
                        dotRepository.updateUserAndStatus(ticket.getOwner(), StatusType.OCCUPIED, dotIds, ticket.getFromDate().toString(), ticket.getToDate().toString());

                        for (Integer dotId: dotIds) {
                            // When claim ticket is approved -> update allocated info of dotInformation
                            DotInfoByTime dotInfoByTimeAllocated = dotInfoByTimeRepository.findByDotAndStatusAndFromDateAndToDate(dotRepository.getOne(dotId), StatusType.ALLOCATED, ticket.getFromDate(), ticket.getToDate());
                            dotInfoByTimeAllocated.setMember(ticket.getOwner());
                            dotInfoByTimeAllocated.setStatus(StatusType.OCCUPIED);
                            dotInfoByTimeRepository.save(dotInfoByTimeAllocated);
                            // When claim ticket is approved -> delete book info of dotInformation, and delete other book tickets
                            DotInfoByTime dotInfoByTimeBooked = dotInfoByTimeRepository.findByDotAndStatusAndFromDateAndToDate(dotRepository.getOne(dotId), StatusType.BOOKED, ticket.getFromDate(), ticket.getToDate());
                            dotInfoByTimeRepository.delete(dotInfoByTimeBooked);
                        }
                        break;
                    case TicketType.EXTEND:
                        dotRepository.updateDateRange(dotIds, ticket.getFromDate().toString(), ticket.getToDate().toString());
                        // When extend ticket is approved -> update records of dotInformation
                        for (Integer dotId: dotIds) {
                            DotInfoByTime dotInfoByTime = dotInfoByTimeRepository.findByDotAndFromDateAndToDate(dotRepository.getOne(dotId), ticket.getFromDate(), ticket.getToDate());
                            dotInfoByTime.setFromDate(ticket.getFromDate());
                            dotInfoByTime.setToDate(ticket.getToDate());
                            dotInfoByTimeRepository.save(dotInfoByTime);
                        }
                        break;
                    default:
                        log.error("Request has type: {}", ticketDTO.getType());
                        throw new BadRequestException();
                }
            }
            // When reject ticket -> delete dot info
        } else if ((Action.REJECT.equals(request.getAction()))) {
            for (ReviewTicketRequest.TicketDTO ticketDTO : request.getTickets()) {
                Ticket ticket = ticketRepository.getTicketByID(ticketDTO.getId());
                List<Integer> dotIds = ticket.getDots().stream().map(Dot::getId).collect(Collectors.toList());
                if (!Objects.equals(ticketDTO.getType(), TicketType.EXTEND)) {
                    // When book/claim ticket is rejected -> delete book info of dotInformation
                    for (Integer dotId : dotIds) {
                        DotInfoByTime dotInfoByTime = dotInfoByTimeRepository.findByDotAndStatusAndFromDateAndToDate(dotRepository.getOne(dotId), StatusType.BOOKED, ticket.getFromDate(), ticket.getToDate());
                        dotInfoByTimeRepository.delete(dotInfoByTime);
                    }
                }
            }
        }

        // Send mail after accept/reject ticket
        String updatedBy = SecurityContextUtils.getUsername();
        for (Ticket ticket : tickets) {
            try {
                if (!updatedBy.equals(ticket.getOwner())) {
                    mailService.sendMail(new Mail(ticket.getOwner(), ticket.getType(), request.getAction(), ticket.getOwner(), updatedBy, LocalDateTime.now()));
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        return ResponseFactory.success(HttpStatus.OK, ErrorMessage.SUCCESSFULLY, ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<TicketDetail>> viewTicket(Integer ticketId) {
        //Get ticket by ID
        Ticket ticket = ticketRepository.getOne(ticketId);

        //Send data to client
        TicketDetail ticketDetail = new TicketDetail();

        // Get all dots by ticket
        List<Dot> dots = ticket.getDots();
        ticketDetail.setDotDTOList(dotResponseMapper.toListDotDTO(dots));

        //Get floor background
        Floor floor = floorRepository.getFloorByDotListIn(ticket.getDots());
        ticketDetail.setFloorBackground(floor.getBackgroundFloor());
        return ResponseFactory.success(HttpStatus.OK, ticketDetail, ErrorMessage.SUCCESS);
    }

    @Override
    public ResponseEntity<BaseResponse<String>> deleteTicket(Integer id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketException(ErrorMessage.TICKET_NOT_FOUND);
        }
        ticketRepository.deleteById(id);
        return ResponseFactory.success(HttpStatus.OK, ErrorMessage.DELETE_TICKET_SUCCESSFULLY, ErrorMessage.SUCCESS);
    }
}
