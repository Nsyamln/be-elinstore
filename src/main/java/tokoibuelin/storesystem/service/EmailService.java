package tokoibuelin.storesystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tokoibuelin.storesystem.model.request.SendEmailReq;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender emailSender;

    public void sendPasswordResetEmail(String to, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reset Password - Toko Ibu Elin");
        message.setText("Untuk mereset password Anda, silakan klik link berikut:\n\n" +
                "http://localhost:5173/reset-password?token=" + resetToken + "\n\n" +
                "Link ini akan kadaluarsa dalam 24 jam.\n\n" +
                "Jika Anda tidak meminta reset password, abaikan email ini.");
        
        emailSender.send(message);
    }

    public void sendContactFormEmail(SendEmailReq req) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("ibuelintoko@gmail.com");
        message.setSubject("[KONTAK FORM] " + req.subject());
        message.setText(
            "Nama: " + req.name() + "\n" +
            "Email: " + req.email() + "\n\n" +
            "Pesan:\n" + req.message()
        );
        emailSender.send(message);
    }
} 