package com.ColumbusEventAlertService.refactor.services.strategy.scraper;

import org.junit.jupiter.api.Test;

public class KembaLiveStrategyTest {

    @Test
    public void test() throws Exception {
        KembaLiveStrategy kembaLiveStrategy = new KembaLiveStrategy("Kemba Live", "KembaLiveUrl");

        kembaLiveStrategy.fetchTodaysEvents();


    }
}
