package com.cmcglobal.backend.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constant {

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DD_MM_YYYY = "dd-MM-yyyy";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DotType {
        public static final String SEAT_DOT = "seat";
        public static final String ROOM_DOT = "room";

    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StatusType {
        public static final String AVAILABLE = "available";
        public static final String BOOKED = "booked";
        public static final String ALLOCATED = "allocated";
        public static final String OCCUPIED = "occupied";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Action {
        public static final String APPROVE = "approve";
        public static final String REJECT = "reject";
        public static final String PENDING = "pending";
        public static final String CREATE = "create";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TicketType {
        public static final String EXTEND = "extend";
        public static final String CLAIM = "claim";
        public static final String BOOK = "book";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Role {
        public static final String ADMIN = "OFFICE-ADMIN";
        public static final String DU_LEAD = "OFFICE-DULead";
        public static final String MEMBER = "OFFICE-MEMBER";
    }
}
