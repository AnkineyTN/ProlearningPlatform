package com.cabybara.prolearningplatform;

import com.cabybara.prolearningplatform.configuration.DotenvApplicationInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProlearningplatformApplication {

	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(ProlearningplatformApplication.class);

		application.addInitializers(new DotenvApplicationInitializer());

		application.run(args);
	}

}
