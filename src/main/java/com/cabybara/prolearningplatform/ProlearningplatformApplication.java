package com.cabybara.prolearningplatform;

import com.cabybara.prolearningplatform.configuration.DotenvApplicationInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class ProlearningplatformApplication {

	public static void main(String[] args) {

		// Pin the JVM timezone to a name PostgreSQL accepts. The default may resolve to the
		// "Asia/Saigon" alias, which some PostgreSQL servers reject ("invalid value for
		// parameter TimeZone") because the driver forwards the JVM timezone at connection time.
		// "Asia/Ho_Chi_Minh" is the canonical IANA name (same UTC+7 offset).
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

		SpringApplication application = new SpringApplication(ProlearningplatformApplication.class);

		application.addInitializers(new DotenvApplicationInitializer());

		application.run(args);
	}
}
