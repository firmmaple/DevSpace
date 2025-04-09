package org.jeffery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@Slf4j
@SpringBootApplication
public class DevSpaceApplication implements ApplicationRunner {
    @Value("${server.port:8080}")
    private Integer webPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application started successfully. Check it out at: http://localhost:{}", webPort);
    }

    public static void main(String[] args) {
        SpringApplication.run(DevSpaceApplication.class, args);
    }
}
