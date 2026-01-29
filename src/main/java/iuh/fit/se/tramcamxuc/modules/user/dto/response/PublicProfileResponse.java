package iuh.fit.se.tramcamxuc.modules.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PublicProfileResponse {
    private String id;
    private String name;
    private String avatar;
    private String bio;
    private boolean isVerified;
    private int followerCount;
}