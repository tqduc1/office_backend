package com.cmcglobal.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cmcglobal.backend.constant.Constant.YYYY_MM_DD_HH_MM_SS;

@Getter
@Setter
@NoArgsConstructor
public class Mail {
    private String mailToUsername;
    private String subject;
    private String action;
    private String createBy;
    private String updatedBy;
    private String timestamp;

    public Mail(String subject, String action, String createBy, LocalDateTime timestamp) {
        this.subject = subject;
        this.action = action;
        this.createBy = createBy;
        this.timestamp = timestamp.format(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
    }

    public Mail(String mailToUsername, String subject, String action, String createBy, String updatedBy, LocalDateTime timestamp) {
        this.mailToUsername = mailToUsername;
        this.subject = subject;
        this.action = action;
        this.createBy = createBy;
        this.updatedBy = updatedBy;
        this.timestamp = timestamp.format(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
    }
}
