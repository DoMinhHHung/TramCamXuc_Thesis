package iuh.fit.se.tramcamxuc.modules.advertisement.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdGrowthResponse {
    private List<String> labels;
    private List<Long> impressions;
    private List<Long> clicks;
}
