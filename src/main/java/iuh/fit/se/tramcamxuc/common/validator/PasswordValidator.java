package iuh.fit.se.tramcamxuc.common.validator;

import iuh.fit.se.tramcamxuc.common.exception.AppException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    public void validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new AppException("Mật khẩu không được để trống");
        }
        
        if (password.length() < MIN_LENGTH) {
            throw new AppException("Mật khẩu phải có ít nhất " + MIN_LENGTH + " ký tự");
        }
        
        if (password.length() > MAX_LENGTH) {
            throw new AppException("Mật khẩu không được quá " + MAX_LENGTH + " ký tự");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            throw new AppException("Mật khẩu phải chứa ít nhất 1 chữ hoa");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new AppException("Mật khẩu phải chứa ít nhất 1 chữ thường");
        }
        
        if (!password.matches(".*[0-9].*")) {
            throw new AppException("Mật khẩu phải chứa ít nhất 1 chữ số");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new AppException("Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt");
        }
        
        String lowerPassword = password.toLowerCase();
        if (lowerPassword.contains("password") || 
            lowerPassword.contains("123456") ||
            lowerPassword.contains("qwerty")) {
            throw new AppException("Mật khẩu quá đơn giản, vui lòng chọn mật khẩu khác");
        }
    }
}
