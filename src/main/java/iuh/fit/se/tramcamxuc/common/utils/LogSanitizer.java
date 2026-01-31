package iuh.fit.se.tramcamxuc.common.utils;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing sensitive data in logs
 */
public final class LogSanitizer {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b\\d{10,}\\b"
    );
    
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
        "\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"
    );
    
    private LogSanitizer() {
        // Prevent instantiation
    }
    
    /**
     * Sanitize sensitive data in log messages
     * @param message Original message
     * @return Sanitized message
     */
    public static String sanitize(String message) {
        if (message == null) {
            return null;
        }
        
        String sanitized = message;
        
        // Mask email addresses
        sanitized = EMAIL_PATTERN.matcher(sanitized)
                .replaceAll("***@***.***");
        
        // Mask phone numbers
        sanitized = PHONE_PATTERN.matcher(sanitized)
                .replaceAll("***PHONE***");
        
        // Mask credit card numbers
        sanitized = CREDIT_CARD_PATTERN.matcher(sanitized)
                .replaceAll("****-****-****-****");
        
        return sanitized;
    }
    
    /**
     * Mask email partially (keep domain)
     * @param email Email address
     * @return Partially masked email
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        
        String[] parts = email.split("@");
        if (parts.length != 2) {
            return "***@***.***";
        }
        
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "**@" + domain;
        }
        
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
    }
}
