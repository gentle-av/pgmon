package avr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PGMon {
  public static void main(String[] args) {
    SpringApplication.run(PGMon.class, args);
  }
}
