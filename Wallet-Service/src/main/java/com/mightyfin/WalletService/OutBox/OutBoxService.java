package com.mightyfin.WalletService.OutBox;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.AccessOptions.SetOptions.Propagation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
public class OutBoxService {

    @Autowired 
    private OutBoxEventRepo outBoxEventRepo;
    
    // @Transactional(propagation = Propagation.MANDATORY)
    public OutBoxEvent record(String eventType, String aggregateType, String aggregateId,
                             String partitionKey, Map<String, Object> payload) {
        
        // 1. Create using the default/no-args constructor
        OutBoxEvent event = new OutBoxEvent();
        
        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setPartitionKey(partitionKey);
        event.setPayload(payload);
        
        // 3. Store it in the repository and return
        return outBoxEventRepo.save(event);
    }
}
