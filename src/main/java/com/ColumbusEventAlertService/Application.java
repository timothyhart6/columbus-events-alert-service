package com.ColumbusEventAlertService;

import com.ColumbusEventAlertService.refactor.services.strategy.scraper.KembaLiveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.class})
@Slf4j
public class Application {

	public static void main(String[] args) throws Exception {
		log.info("Application starting...");

		/*
		ApplicationContext context = SpringApplication.run(Application.class, args);

		// Only run this block when you want to test locally
		if (args.length > 0 && args[0].equals("localRun")) {
			log.info("Running EventCollector locally...");

			TextMessageService textMessageService = context.getBean(TextMessageService.class);
			textMessageService.sendTodaysEvents();*/

		KembaLiveStrategy kembaLiveStrategy = new KembaLiveStrategy("KembaLive", "https://promowestlive.com/columbus/kemba-live");
		kembaLiveStrategy.fetchTodaysEvents();
	}
}