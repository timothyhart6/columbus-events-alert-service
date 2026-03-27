package com.ColumbusEventAlertService;

import com.ColumbusEventAlertService.services.TextMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class})
@Slf4j
public class Application {

	public static void main(String[] args) throws Exception {
		log.info("Application starting...");

		ApplicationContext context = SpringApplication.run(Application.class, args);

		// Only run this block when you want to test locally
		if (args.length > 0 && args[0].equals("localRun")) {
			log.info("Running EventCollector locally...");

			TextMessageService textMessageService = context.getBean(TextMessageService.class);
			textMessageService.sendTodaysEvents();

		}
	}
}