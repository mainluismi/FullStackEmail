package com.example.EjercicioEmailLogin.EmailLoginLuismi.Email;

import ch.qos.logback.core.net.SyslogOutputStream;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.security.SecureRandom;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String emailSender;

    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String enviarCodigoVerificacion(String email) {
        String codigoVerificacion = generarCodigoVerificacion();
        enviarCorreo(email, "Código de Verificación", "Su código de verificación es: " + codigoVerificacion);
        return codigoVerificacion;
    }

    private void enviarCorreo(String email, String asunto, String cuerpo) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        try {
            helper.setFrom(emailSender);
            helper.setTo(email);
            helper.setSubject(asunto);
            helper.setText(cuerpo);
            javaMailSender.send(mimeMessage);
            System.out.println("Correo enviado");
        } catch (jakarta.mail.MessagingException e) {
            System.out.println("Correo no enviado");

        }
    }

    private String generarCodigoVerificacion() {
        // Genera un código aleatorio de 5 dígitos
        SecureRandom random = new SecureRandom();
        int codigo = 10000 + random.nextInt(90000);
        return String.valueOf(codigo);
    }
}


