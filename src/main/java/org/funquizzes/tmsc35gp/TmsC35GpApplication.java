package org.funquizzes.tmsc35gp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TmsC35GpApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsC35GpApplication.class, args);
    }

}
