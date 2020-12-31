package com.neocode24.lotto.decision.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("sequences")
public class DatabaseSequence {

    /**
     * Id
     */
    @Id
    private String id;

    /**
     * Seq
     */
    private Long seq;
}
