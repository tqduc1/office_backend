package com.cmcglobal.backend.service.impl;

import com.cmcglobal.backend.config.SecurityContextUtils;
import com.cmcglobal.backend.config.ThymeleafConfig;
import com.cmcglobal.backend.constant.Constant;
import com.cmcglobal.backend.constant.Constant.Action;
import com.cmcglobal.backend.constant.Constant.Role;
import com.cmcglobal.backend.entity.Mail;
import com.cmcglobal.backend.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Service
public class MailServiceImpl implements MailService {

    private static final String EMAIL_SUFFIX = "@cmcglobal.vn";

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private String port;
    @Value("${spring.mail.username}")
    private String email;
    @Value("${spring.mail.password}")
    private String password;

    @Autowired
    private ThymeleafConfig thymeleafConfig;

    @Autowired
    private JavaMailSender emailSender;

    @Override
    public void sendMail(Mail mail) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", port);

        helper.addAttachment("logo.png", new ClassPathResource("/templates/logo.png"));
        String subject = "[OFFICE MANAGEMENT SYSTEM - ?]";
        helper.setSubject(subject.replace("?", mail.getSubject().concat(" TICKET REQUEST").toUpperCase()));

        if (mail.getAction().equals(Action.CREATE)) {
            Set<String> userRoles = SecurityContextUtils.getUserRoles();
            if (userRoles.contains(Role.DU_LEAD)) {
                mail.setMailToUsername("btlhuong");
            } else if (userRoles.contains(Role.MEMBER)) {
                mail.setMailToUsername(this.getDuLeadByUsername(SecurityContextUtils.getUsername()));
            }
        }
        String mailToAddress = mail.getMailToUsername().concat(EMAIL_SUFFIX);
//            helper.setTo(mailToAddress);

        helper.setTo("lthang1@cmcglobal.vn");

        helper.setText(thymeleafConfig.getContent(mail), true);
        helper.setFrom(email);
        emailSender.send(message);
    }

    private String getDuLeadByUsername(String username) {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        interceptors.add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer f336202f1d5a9d9f8427ab1e237099e8");
            return execution.execute(request, body);
        });
        ResponseEntity<Object> response = restTemplate.getForEntity("https://hrms.cmcglobal.com.vn/api/connector/connector/api/employee/get-approver-for-user?username="+username, Object.class);
        LinkedHashMap<String, String> linkedHashMap = (LinkedHashMap<String, String>) response.getBody();
        return linkedHashMap.get("user_name");
    }
}
