package it.gov.pagopa.pu.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class PuMigrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PuMigrationApplication.class, args);
	}

}
