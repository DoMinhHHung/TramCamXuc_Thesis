package iuh.fit.se.tramcamxuc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TramCamXucThesisApplication {

    public static void main(String[] args) {
        SpringApplication.run(TramCamXucThesisApplication.class, args);
    }

}
