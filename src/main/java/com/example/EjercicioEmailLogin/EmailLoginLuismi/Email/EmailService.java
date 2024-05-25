package com.example.EjercicioEmailLogin.EmailLoginLuismi.Email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String emailSender;

    private final JavaMailSenderImpl javaMailSender;

    public EmailService(JavaMailSenderImpl javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String enviarCodigoVerificacion(String email) {
        String codigoVerificacion = generarCodigoVerificacion();
        enviarCorreo(email, "Código de Verificación", "Su código de verificación es: " + codigoVerificacion);
        System.out.println("Se ha enviado el correo perfectamente: "+codigoVerificacion);
        return codigoVerificacion;
    }

    public void enviarCorreo(String email, String asunto, String cuerpo) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            helper = new MimeMessageHelper(mimeMessage, true);

            if (emailSender == null) {
                System.err.println("Email sender address is null");
            } else {
                helper.setFrom(emailSender);
            }
            helper.setFrom(emailSender);  // Asegúrate de que emailSender no sea null
            helper.setTo(email);
            helper.setSubject(asunto);
            helper.setText(cuerpo);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // Maneja la excepción adecuadamente en tu aplicación
        }
    }



    private String generarCodigoVerificacion() {
        // Genera un código aleatorio de 5 dígitos
        SecureRandom random = new SecureRandom();
        int codigo = 10000 + random.nextInt(90000);
        return String.valueOf(codigo);
    }
}



