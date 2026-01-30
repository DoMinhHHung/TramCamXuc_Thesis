package iuh.fit.se.tramcamxuc.modules.statistic.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class RevenueStatsResponse {
    private long totalRevenue;
    private long activeSubscribers;
    private double churnRate;

    private Map<String, Long> revenueByPlan;

    private String topPlanName;
    private long topPlanRevenue;
}