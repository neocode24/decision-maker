package com.neocode24.lotto.decision.service;

import com.neocode24.lotto.decision.domain.LottoDomain;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
public class LottoUtils {

    // 로또 번호 1 ~ 45
    private static final Set<Integer> LOTTO_NUMBERS = Stream.iterate(1, n -> n + 1).limit(45).collect(Collectors.toSet());

    /**
     * 로또 리스트 정보에서 중복된 숫자를 제외
     * @param lottoDomains
     * @return
     */
    public static Set<Integer> getDeduplicatedNumbers(List<LottoDomain> lottoDomains) {

        Set<Integer> deduplicatedNumbers = new TreeSet<>();
        for ( LottoDomain domain : lottoDomains ) {
            deduplicatedNumbers.add(domain.getNumber1());
            deduplicatedNumbers.add(domain.getNumber2());
            deduplicatedNumbers.add(domain.getNumber3());
            deduplicatedNumbers.add(domain.getNumber4());
            deduplicatedNumbers.add(domain.getNumber5());
            deduplicatedNumbers.add(domain.getNumber6());
        }


        log.debug("Lotto 구입 번호 (같은 번호 제외) : " + deduplicatedNumbers);
        return deduplicatedNumbers;
    }

    /**
     * 로또 번호 생성. 없는 번호이고, 생성도 한번만 한 번호로 만듬.
     * @param deduplicatedNumbers
     * @return
     */
    public static LottoDomain getLottoDomain(Set<Integer> deduplicatedNumbers) {

        // 번호 객체 생성
        List<Integer> numbers = new ArrayList<>();

        // 없는 번호이어야 하고, 번호 객체 중에 생성된 적 없는 번호 이어야 함.
        while ( true ) {
            if ( numbers.size() == 6 ) break;

            numbers.add(getNumber(deduplicatedNumbers, numbers));
        }

        Collections.sort(numbers);
        log.debug("List:" + numbers);

        return
            LottoDomain.builder()
                    .number1(numbers.get(0))
                    .number2(numbers.get(1))
                    .number3(numbers.get(2))
                    .number4(numbers.get(3))
                    .number5(numbers.get(4))
                    .number6(numbers.get(5))
                    .build();

    }

    /**
     * 랜덤 숫자를 생성함.
     * 이미 뽑은 번호는 빼고, 중복 제외한 번호도 빼고 만듬
     * @param deduplicatedNumbers
     * @param numbers
     * @return
     */
    private static Integer getNumber(Set<Integer> deduplicatedNumbers, List<Integer> numbers) {

        Random random = new Random();
        random.setSeed(System.nanoTime());

        while ( true ) {
            int number = random.nextInt(44) + 1;

            // 이미 뽑은 번호면 다시 뽑음.
            if ( numbers.contains(number)) continue;

            // 없는 번호 일 경우 획득
            if ( !deduplicatedNumbers.contains(number) ) return number;
        }
    }
}
