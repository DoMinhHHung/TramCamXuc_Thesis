package iuh.fit.se.tramcamxuc.common.validator;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private PasswordValidator passwordValidator;

    @BeforeEach
    void setUp() {
        passwordValidator = new PasswordValidator();
    }

    // ========== VALID PASSWORD TESTS ==========

    @Test
    @DisplayName("Should validate correct password")
    void validate_ValidPassword_NoException() {
        // Given
        String validPassword = "SecureP@ss123";

        // When & Then
        assertDoesNotThrow(() -> passwordValidator.validate(validPassword));
    }

    @Test
    @DisplayName("Should validate password with minimum requirements")
    void validate_MinimumRequirements_NoException() {
        // Given
        String password = "Abc123!@";

        // When & Then
        assertDoesNotThrow(() -> passwordValidator.validate(password));
    }

    @Test
    @DisplayName("Should validate password with all special characters")
    void validate_AllSpecialCharacters_NoException() {
        // Given
        String password = "P@ssw0rd!#$%";

        // When & Then
        assertDoesNotThrow(() -> passwordValidator.validate(password));
    }

    // ========== NULL AND EMPTY TESTS ==========

    @Test
    @DisplayName("Should throw exception when password is null")
    void validate_NullPassword_ThrowsException() {
        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(null));
        assertEquals("Mật khẩu không được để trống", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password is empty")
    void validate_EmptyPassword_ThrowsException() {
        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(""));
        assertEquals("Mật khẩu không được để trống", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password is only whitespace")
    void validate_WhitespacePassword_ThrowsException() {
        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate("   "));
        assertEquals("Mật khẩu không được để trống", exception.getMessage());
    }

    // ========== LENGTH TESTS ==========

    @Test
    @DisplayName("Should throw exception when password is too short")
    void validate_TooShort_ThrowsException() {
        // Given
        String shortPassword = "Ab1!";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(shortPassword));
        assertEquals("Mật khẩu phải có ít nhất 8 ký tự", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password is too long")
    void validate_TooLong_ThrowsException() {
        // Given
        String longPassword = "A".repeat(130) + "b1!";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(longPassword));
        assertEquals("Mật khẩu không được quá 128 ký tự", exception.getMessage());
    }

    // ========== CHARACTER TYPE TESTS ==========

    @Test
    @DisplayName("Should throw exception when missing uppercase letter")
    void validate_NoUppercase_ThrowsException() {
        // Given
        String password = "password123!";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(password));
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ hoa", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when missing lowercase letter")
    void validate_NoLowercase_ThrowsException() {
        // Given
        String password = "PASSWORD123!";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(password));
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ thường", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when missing digit")
    void validate_NoDigit_ThrowsException() {
        // Given
        String password = "Password!@#";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(password));
        assertEquals("Mật khẩu phải chứa ít nhất 1 chữ số", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when missing special character")
    void validate_NoSpecialChar_ThrowsException() {
        // Given
        String password = "Password123";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(password));
        assertEquals("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt", exception.getMessage());
    }

    // ========== COMMON PASSWORD TESTS ==========

    @Test
    @DisplayName("Should throw exception when password contains 'password'")
    void validate_ContainsPasswordWord_ThrowsException() {
        // Given
        String password = "MyPassword123!";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(password));
        assertEquals("Mật khẩu quá đơn giản, vui lòng chọn mật khẩu khác", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password contains '123456'")
    void validate_Contains123456_ThrowsException() {
        // Given
        String password = "Abc123456!";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(password));
        assertEquals("Mật khẩu quá đơn giản, vui lòng chọn mật khẩu khác", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when password contains 'qwerty'")
    void validate_ContainsQwerty_ThrowsException() {
        // Given
        String password = "Qwerty123!";

        // When & Then
        AppException exception = assertThrows(AppException.class,
                () -> passwordValidator.validate(password));
        assertEquals("Mật khẩu quá đơn giản, vui lòng chọn mật khẩu khác", exception.getMessage());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should validate password with exactly 8 characters")
    void validate_Exactly8Characters_NoException() {
        // Given
        String password = "Abc123!@";

        // When & Then
        assertDoesNotThrow(() -> passwordValidator.validate(password));
    }

    @Test
    @DisplayName("Should validate password with exactly 128 characters")
    void validate_Exactly128Characters_NoException() {
        // Given
        String password = "A" + "b".repeat(124) + "1!@";

        // When & Then
        assertDoesNotThrow(() -> passwordValidator.validate(password));
    }

    @Test
    @DisplayName("Should validate password with multiple special characters")
    void validate_MultipleSpecialChars_NoException() {
        // Given
        String password = "P@ssw0rd!#$%^&*()";

        // When & Then
        assertDoesNotThrow(() -> passwordValidator.validate(password));
    }

    @Test
    @DisplayName("Should validate password with unicode characters")
    void validate_UnicodeCharacters_NoException() {
        // Given
        String password = "Pässw0rd!123";

        // When & Then
        assertDoesNotThrow(() -> passwordValidator.validate(password));
    }
}
