// Copyright (c) Dolittle. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package io.dolittle.azure.ad.reader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = {"io.dolittle.azure.ad.reader.config.console"})
@Slf4j
public class ReaderApplication {

	public static void main(String[] args) {
		log.info("************ Azure AD Reader STARTED **************");
		SpringApplication app = new SpringApplication(ReaderApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);
		log.info("************ Azure AD Reader ENDED **************");
	}

}
