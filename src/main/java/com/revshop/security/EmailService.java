package com.revshop.security;

import com.revshop.env.EnvLoader;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private static final String SMTP_HOST = EnvLoader.get("SMTP_HOST");
    private static final String SMTP_PORT = EnvLoader.get("SMTP_PORT");
    private static final String SMTP_EMAIL = EnvLoader.get("SMTP_EMAIL");
    private static final String SMTP_PASSWORD = EnvLoader.get("SMTP_PASSWORD");

    public static boolean sendOTPEmail(String recipientEmail, String otp) {
        return sendEmail(recipientEmail, "RevShop - Email Verification OTP", createOTPEmailBody(otp));
    }

    public static boolean sendResetEmail(String recipientEmail, String subject, String body) {
        return sendEmail(recipientEmail, subject, body);
    }

    private static boolean sendEmail(String recipientEmail, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_EMAIL, SMTP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html");

            Transport.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email to " + recipientEmail + ": " + e.getMessage());
            return false;
        }
    }

    private static String createOTPEmailBody(String otp) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 30px;">
                        <h2 style="color: #333; text-align: center;">RevShop Email Verification</h2>
                        <p style="font-size: 16px; color: #555;">
                            Thank you for registering with RevShop! Please use the following OTP to verify your email address:
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <span style="font-size: 24px; font-weight: bold; letter-spacing: 10px; color: #2c3e50; background: #f8f9fa; padding: 15px 30px; border-radius: 5px; border: 2px dashed #3498db;">
                                %s
                            </span>
                        </div>
                        <p style="font-size: 14px; color: #777;">
                            This OTP is valid for 5 minutes. If you didn't request this, please ignore this email.
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            © 2024 RevShop Electronics Marketplace. All rights reserved.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(otp);
    }
}