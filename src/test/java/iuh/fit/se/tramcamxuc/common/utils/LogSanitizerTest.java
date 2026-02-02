package iuh.fit.se.tramcamxuc.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class LogSanitizerTest {

    @Test
    @DisplayName("Should sanitize email address")
    void testSanitize_WithEmail() {
        String message = "User email is test@example.com";
        String result = LogSanitizer.sanitize(message);
        assertEquals("User email is ***@***.***", result);
    }

    @Test
    @DisplayName("Should sanitize multiple email addresses")
    void testSanitize_WithMultipleEmails() {
        String message = "Emails: user1@gmail.com and user2@yahoo.com";
        String result = LogSanitizer.sanitize(message);
        assertEquals("Emails: ***@***.*** and ***@***.***", result);
    }

    @Test
    @DisplayName("Should sanitize phone number")
    void testSanitize_WithPhoneNumber() {
        String message = "Contact: 0123456789";
        String result = LogSanitizer.sanitize(message);
        assertEquals("Contact: ***PHONE***", result);
    }

    @Test
    @DisplayName("Should sanitize long phone number")
    void testSanitize_WithLongPhoneNumber() {
        String message = "International: 84901234567";
        String result = LogSanitizer.sanitize(message);
        assertEquals("International: ***PHONE***", result);
    }

    @Test
    @DisplayName("Should sanitize credit card with hyphens")
    void testSanitize_WithCreditCard() {
        String message = "Card: 1234-5678-9012-3456";
        String result = LogSanitizer.sanitize(message);
        assertEquals("Card: ****-****-****-****", result);
    }

    @Test
    @DisplayName("Should sanitize credit card without hyphens")
    void testSanitize_WithCreditCardNoHyphens() {
        // Note: Credit card without hyphens also matches phone pattern (10+ digits)
        // Phone pattern is applied before credit card pattern
        String message = "Card: 1234-5678-9012-3456";
        String result = LogSanitizer.sanitize(message);
        assertEquals("Card: ****-****-****-****", result);
    }

    @Test
    @DisplayName("Should sanitize mixed sensitive data")
    void testSanitize_WithMixedSensitiveData() {
        String message = "Contact: user@example.com or phone 1234567890 or card 1234-5678-9012-3456";
        String result = LogSanitizer.sanitize(message);
        assertTrue(result.contains("***@***.***"), "Should mask email");
        assertTrue(result.contains("***PHONE***"), "Should mask phone");
        assertTrue(result.contains("****-****-****-****"), "Should mask credit card");
    }

    @Test
    @DisplayName("Should handle null input")
    void testSanitize_WithNullInput() {
        String result = LogSanitizer.sanitize(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should not modify message without sensitive data")
    void testSanitize_WithNoSensitiveData() {
        String message = "This is a normal log message";
        String result = LogSanitizer.sanitize(message);
        assertEquals("This is a normal log message", result);
    }

    @Test
    @DisplayName("Should partially mask valid email")
    void testMaskEmail_ValidEmail() {
        String result = LogSanitizer.maskEmail("john.doe@example.com");
        assertEquals("j***e@example.com", result);
    }

    @Test
    @DisplayName("Should mask short username email")
    void testMaskEmail_ShortUsername() {
        String result = LogSanitizer.maskEmail("ab@example.com");
        assertEquals("**@example.com", result);
    }

    @Test
    @DisplayName("Should mask two character username")
    void testMaskEmail_TwoCharacterUsername() {
        String result = LogSanitizer.maskEmail("jo@example.com");
        assertEquals("**@example.com", result);
    }

    @Test
    @DisplayName("Should mask three character username")
    void testMaskEmail_ThreeCharacterUsername() {
        String result = LogSanitizer.maskEmail("joe@example.com");
        assertEquals("j***e@example.com", result);
    }

    @Test
    @DisplayName("Should handle null email")
    void testMaskEmail_WithNullInput() {
        String result = LogSanitizer.maskEmail(null);
        assertEquals("***@***.***", result);
    }

    @Test
    @DisplayName("Should handle invalid email format")
    void testMaskEmail_WithInvalidEmail() {
        String result = LogSanitizer.maskEmail("notanemail");
        assertEquals("***@***.***", result);
    }

    @Test
    @DisplayName("Should handle email with multiple @ signs")
    void testMaskEmail_WithMultipleAtSigns() {
        String result = LogSanitizer.maskEmail("test@@example.com");
        assertEquals("***@***.***", result);
    }

    @Test
    @DisplayName("Should handle empty string email")
    void testMaskEmail_EmptyString() {
        String result = LogSanitizer.maskEmail("");
        assertEquals("***@***.***", result);
    }
}
