package com.huly.backend.infrastructure.email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailPortJavaMailImplTest {

    @Mock private JavaMailSender mailSender;

    @InjectMocks private EmailPortJavaMailImpl emailPort;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendWelcomeLead_shouldCallMailSenderSend() {
        emailPort.sendWelcomeLead("user@example.com", "TestUser");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeLead_shouldSetCorrectRecipient() throws MessagingException {
        emailPort.sendWelcomeLead("user@example.com", "TestUser");

        assertThat(mimeMessage.getRecipients(Message.RecipientType.TO))
                .isNotEmpty()
                .anyMatch(a -> a.toString().contains("user@example.com"));
    }

    @Test
    void sendWelcomeLead_shouldSetSubjectContainingNickname() throws MessagingException {
        emailPort.sendWelcomeLead("user@example.com", "TestUser");

        assertThat(mimeMessage.getSubject()).contains("TestUser");
    }

    @Test
    void sendWelcomeLead_shouldSetFromAsHulyAddress() throws MessagingException {
        emailPort.sendWelcomeLead("user@example.com", "TestUser");

        assertThat(mimeMessage.getFrom())
                .isNotEmpty()
                .anyMatch(a -> a.toString().contains("hulycomunicaciones@gmail.com"));
    }

    @Test
    void sendWelcomeLead_shouldNotPropagateException_whenSendFails() {
        doThrow(new MailSendException("SMTP error"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> emailPort.sendWelcomeLead("user@example.com", "TestUser"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendWelcomeLead_shouldNotCallSend_whenMessageCreationFails() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("session error"));

        emailPort.sendWelcomeLead("user@example.com", "TestUser");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
