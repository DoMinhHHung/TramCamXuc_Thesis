package iuh.fit.se.tramcamxuc.modules.user.dto.request;

import iuh.fit.se.tramcamxuc.modules.user.entity.enums.Gender;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateProfileRequest {
    private String fullName;
    private Gender gender;
    private LocalDate dob;
}