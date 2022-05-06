package com.cmcglobal.backend.config;

import com.cmcglobal.backend.constant.Constant;
import com.cmcglobal.backend.constant.Constant.Action;
import com.cmcglobal.backend.entity.Mail;
import com.cmcglobal.backend.entity.UserFlattened;
import com.cmcglobal.backend.repository.UserFlattenedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Component
public class ThymeleafConfig {
    private static final String MAIL_TEMPLATE_BASE_NAME = "mail/MailMessages";
    private static final String MAIL_TEMPLATE_PREFIX = "/templates/";
    private static final String MAIL_TEMPLATE_SUFFIX = ".html";
    private static final String UTF_8 = "UTF-8";

    private static final String TEMPLATE_NAME = "mail-template";

    private static final TemplateEngine templateEngine;

    @Autowired
    private UserFlattenedRepository userFlattenedRepository;


    static {
        templateEngine = emailTemplateEngine();
    }

    private static TemplateEngine emailTemplateEngine() {
        final SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(htmlTemplateResolver());
        templateEngine.setTemplateEngineMessageSource(emailMessageSource());
        return templateEngine;
    }


    private static ResourceBundleMessageSource emailMessageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename(MAIL_TEMPLATE_BASE_NAME);
        return messageSource;
    }

    private static ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix(MAIL_TEMPLATE_PREFIX);
        templateResolver.setSuffix(MAIL_TEMPLATE_SUFFIX);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding(UTF_8);
        templateResolver.setCacheable(false);
        return templateResolver;
    }


    public String getContent(Mail mail) {
        UserFlattened user = userFlattenedRepository.findByUserName(mail.getMailToUsername());

        UserFlattened createdBy = userFlattenedRepository.findByUserName(mail.getCreateBy());
        if (user == null) {
            throw new RuntimeException("error");
        }
        final Context context = new Context();
        context.setVariable("fullName", user.getFullName());
        context.setVariable("action", mail.getAction().equals("approve") ? "approved" : "rejected");
        context.setVariable("createdBy", createdBy.getFullName());
        context.setVariable("timestamp", mail.getTimestamp());
        if (!mail.getAction().equals(Action.CREATE)) {
            UserFlattened updatedBy = userFlattenedRepository.findByUserName(mail.getUpdatedBy());
            context.setVariable("updatedBy", updatedBy.getFullName());
        }

        String template = mail.getAction().equals(Action.CREATE) ? "create_ticket" : "review_ticket";
        return templateEngine.process(template, context);
    }
}
