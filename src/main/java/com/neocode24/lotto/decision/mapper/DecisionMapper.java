package com.neocode24.lotto.decision.mapper;

import com.neocode24.lotto.decision.domain.LottoDecisionDomain;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DecisionMapper extends MongoRepository<LottoDecisionDomain, Long> {

}
