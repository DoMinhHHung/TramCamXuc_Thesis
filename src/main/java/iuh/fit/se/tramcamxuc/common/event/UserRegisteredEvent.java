package iuh.fit.se.tramcamxuc.common.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {
    private final String email;
    private final String fullName;
    private final String otp;
}
