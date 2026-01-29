package iuh.fit.se.tramcamxuc.config;

import iuh.fit.se.tramcamxuc.modules.user.entity.User;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.AuthProvider;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.Role;
import iuh.fit.se.tramcamxuc.modules.user.entity.enums.UserStatus;
import iuh.fit.se.tramcamxuc.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        User admin = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> User.builder()
                        .email(adminEmail)
                        .username("admin_tramcamxuc")
                        .build());

        admin.setFullName("Super Administrator Tram Cam Xuc");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setIsActive(UserStatus.ACTIVE);
        admin.setProvider(AuthProvider.LOCAL);

        if (admin.getDob() == null) {
            admin.setDob(LocalDate.of(2003, 3, 4));
        }

        userRepository.save(admin);
    }
}