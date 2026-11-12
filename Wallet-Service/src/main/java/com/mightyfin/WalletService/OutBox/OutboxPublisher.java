package com.mightyfin.WalletService.OutBox;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import com.mightyfin.WalletService.Config.OutboxProperties;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor 
@Slf4j 
public class OutboxPublisher {


    private final OutBoxEventRepo repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxProperties props;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${wallet.outbox.poll-interval-ms:1000}")
    @Transactional 
    public void publishPending() {
        List<OutBoxEvent> batch = repository.lockPendingBatch(props.getBatchSize());
        for (OutBoxEvent event : batch) {
            Instant now = Instant.now(clock);
            try {
                kafkaTemplate.send(props.getTopic(), event.getPartitionKey(), toEnvelope(event))
                        .get(props.getSendTimeoutSeconds(), TimeUnit.SECONDS);
                event.markPublished(now);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                event.markFailed(e.getMessage(), props.getMaxAttempts(),
                        Duration.ofSeconds(props.getRetryBackoffSeconds()), now);
                if (event.getStatus() == OutBoxEventStatus.DEAD) {
                    log.error("Outbox event {} ({}) is dead after {} attempts: {}",
                            event.getEventId(), event.getEventType(), event.getAttemptCount(), e.getMessage());
                } else {
                    log.warn("Outbox event {} failed (attempt {}): {}",
                            event.getEventId(), event.getAttemptCount(), e.getMessage());
                }
            }
        }
    }

    private String toEnvelope(OutBoxEvent e)  {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", e.getEventId().toString());
        envelope.put("eventType", e.getEventType());
        envelope.put("aggregateType", e.getAggregateType());
        envelope.put("aggregateId", e.getAggregateId());
        envelope.put("correlationId", e.getCorrelationId());
        envelope.put("schemaVersion", e.getSchemaVersion());
        envelope.put("occurredAt", e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
        envelope.put("payload", e.getPayload());
        return objectMapper.writeValueAsString(envelope);
    }
}
