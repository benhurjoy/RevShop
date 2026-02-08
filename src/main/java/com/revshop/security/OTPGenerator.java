package com.revshop.security;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class OTPGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 5;

    public static String generateOTP() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public static LocalDateTime generateExpiryTime() {
        return LocalDateTime.now().plus(EXPIRY_MINUTES, ChronoUnit.MINUTES);
    }

    public static boolean isOTPExpired(LocalDateTime expiryTime) {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}