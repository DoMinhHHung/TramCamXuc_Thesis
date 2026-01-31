package iuh.fit.se.tramcamxuc;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TramCamXucThesisApplicationTests {

    @BeforeAll
    static void setup() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            System.out.println("Warning: Could not load .env file - " + e.getMessage());
        }
    }

    @Test
    void contextLoads() {
    }

}
