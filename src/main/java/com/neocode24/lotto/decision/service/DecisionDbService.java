package com.neocode24.lotto.decision.service;

import com.neocode24.lotto.decision.domain.LottoDecisionDomain;
import com.neocode24.lotto.decision.mapper.DecisionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
public class DecisionDbService extends AbstractMongoEventListener<LottoDecisionDomain> {

    @Autowired
    private DecisionMapper decisionMapper;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    /**
     * DB Insert
     * @param lottoDecisionDomain
     * @return
     */
    public LottoDecisionDomain insert(LottoDecisionDomain lottoDecisionDomain) {
        return decisionMapper.insert(lottoDecisionDomain);
    }


    /**
     * 데이터 저장 전에 자동 채번 됨.
     * @param event
     */
    @Override
    public void onBeforeConvert(BeforeConvertEvent<LottoDecisionDomain> event) {
        if (event.getSource().getId() == null || event.getSource().getId() < 1) {
            long sequence = sequenceGeneratorService.generateSequence(LottoDecisionDomain.SEQUENCE_NAME);
            event.getSource().setId(sequence);
        }
    }


}
