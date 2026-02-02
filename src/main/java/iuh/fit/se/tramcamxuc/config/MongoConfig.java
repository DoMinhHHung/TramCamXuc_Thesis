package iuh.fit.se.tramcamxuc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "iuh.fit.se.tramcamxuc.modules.statistic.repository")
@EnableMongoAuditing
public class MongoConfig {
}
