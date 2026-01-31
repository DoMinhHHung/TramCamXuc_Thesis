package iuh.fit.se.tramcamxuc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.executor")
public class ExecutorProperties {
    private int corePoolSize = 10;
    private int maxPoolSize = 20;
    private int queueCapacity = 1000;
    private String threadNamePrefix = "TramCamXuc-Thread-";
}
