package com.neocode24.lotto.common.service;

import com.neocode24.lotto.decision.service.DecisionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DecisionServiceTest {

    @Autowired
    private DecisionService decisionService;

    @Test
    void getLottoHtmlDataFromServer() {

        try {
            decisionService.getLottoHtmlDataFromServer("http://m.dhlottery.co.kr/?v=0944m010308111226q031528323444q112325293133q162830333942q0114224042451250641811", "127.0.0.1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}