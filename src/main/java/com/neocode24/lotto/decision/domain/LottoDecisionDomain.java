package com.neocode24.lotto.decision.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("lottoPaperDomain")
public class LottoDecisionDomain implements Serializable {

    @Transient
    public static final String SEQUENCE_NAME = "lottoDecisionDomain";

    /**
     * Id
     */
    @Id
    private Long id;

    /**
     * Client IP
     */
    private String clientIp;

    /**
     * 생성 시간
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createDateTime;

    /**
     * 구입한 중복 제외한 번호들
     */
    private Set<Integer> deduplicatedNumbers;

    /**
     * List Lotto Domain
     */
    private List<LottoDomain> lottoDomains;
}
