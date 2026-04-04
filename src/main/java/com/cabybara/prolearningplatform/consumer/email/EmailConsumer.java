package com.cabybara.prolearningplatform.consumer.email;

import com.cabybara.prolearningplatform.dto.request.email.EmailMessage;
import com.rabbitmq.client.Channel;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @RabbitListener(queues = "${app.rabbitmq.queues.transactional}")
    public void consume(
            EmailMessage message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        log.info("Email dequeued | to={} template={}", message.getTo(), message.getTemplate());

        try {
            sendMail(message);
            channel.basicAck(deliveryTag, false);
            log.info("Email sent & acked | to={}", message.getTo());

        } catch (Exception e) {
            log.error("Email failed | to={} reason={}", message.getTo(), e.getMessage());
            // requeue=false → Spring retry 3 lần → sau đó vào dead-letter queue
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void sendMail(EmailMessage message)
            throws MessagingException, UnsupportedEncodingException {

        String html = renderTemplate(message);

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(new InternetAddress(fromEmail, fromName));
        helper.setTo(message.getTo());
        helper.setSubject(message.getSubject());
        helper.setText(html, true);

        mailSender.send(mime);
    }

    private String renderTemplate(EmailMessage message) {
        Context ctx = new Context();
        if (message.getVariables() != null) {
            ctx.setVariables(message.getVariables());
        }
        return templateEngine.process(message.getTemplate(), ctx);
    }
}