package com.cmcglobal.backend.dto.request.ticket;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TicketCreateRequest {
    private List<Integer> dotIds;
    private String type;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String owner;
}
