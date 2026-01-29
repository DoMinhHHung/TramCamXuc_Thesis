package iuh.fit.se.tramcamxuc.modules.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class OnboardingRequest {
    @NotEmpty(message = "Vui lòng chọn ít nhất 1 thể loại")
    @Size(min = 1, max = 5, message = "Chỉ được chọn tối đa 5 thể loại")
    private Set<UUID> genreIds;
}