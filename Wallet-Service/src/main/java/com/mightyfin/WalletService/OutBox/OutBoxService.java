
package com.mightyfin.WalletService.OutBox;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OutBoxService {

    private final OutBoxEventRepo outBoxEventRepo;

    @Transactional
    public OutBoxEvent record(
            String eventType,
            String aggregateType,
            String aggregateId,
            String partitionKey,
            Map<String, Object> payload) {

        OutBoxEvent event = OutBoxEvent.create(
                eventType,
                aggregateType,
                aggregateId,
                partitionKey,
                payload
        );

        return outBoxEventRepo.save(event);
    }
}
