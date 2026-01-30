package iuh.fit.se.tramcamxuc.modules.statistic.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChartResponse {
    private List<String> labels;
    private List<Long> data;
}
