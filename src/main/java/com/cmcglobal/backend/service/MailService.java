package com.cmcglobal.backend.service;

import com.cmcglobal.backend.entity.Mail;

import javax.mail.MessagingException;

public interface MailService {
    void sendMail(Mail mail) throws MessagingException;
}
