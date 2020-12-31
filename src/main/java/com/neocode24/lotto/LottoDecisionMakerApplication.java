package com.neocode24.lotto;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableEncryptableProperties
@SpringBootApplication
public class LottoDecisionMakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LottoDecisionMakerApplication.class, args);
    }

}
