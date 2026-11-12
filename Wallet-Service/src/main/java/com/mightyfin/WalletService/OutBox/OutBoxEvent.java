package com.mightyfin.WalletService.OutBox;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data 
public class OutBoxEvent {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, updatable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "partition_key", nullable = false)
    private String partitionKey;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "schema_version", nullable = false)
    private Short schemaVersion = 1;

    /** IDs and codes only. No names, phone numbers, or account numbers. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private OutBoxEventStatus status = OutBoxEventStatus.PENDING;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static OutBoxEvent create(String eventType, String aggregateType, String aggregateId,
                                     String partitionKey, Map<String, Object> payload) {
        OutBoxEvent e = new OutBoxEvent();
        e.eventId = UUID.randomUUID();
        e.eventType = eventType;
        e.aggregateType = aggregateType;
        e.aggregateId = aggregateId;
        e.partitionKey = partitionKey;
        e.payload = payload;
        return e;
    }

    public void markPublished(Instant now) {
        this.status = OutBoxEventStatus.PUBLISHED;
        this.publishedAt = now;
        this.nextRetryAt = null;
        this.lastError = null;
    }

    /** Linear backoff; after maxAttempts the row becomes 'dead' (table DLQ) and is never retried. */
    public void markFailed(String error, int maxAttempts, Duration backoff, Instant now) {
        this.attemptCount++;
        this.lastError = error == null ? "unknown error" : error.substring(0, Math.min(error.length(), 1000));
        if (this.attemptCount >= maxAttempts) {
            this.status = OutBoxEventStatus.DEAD;
            this.nextRetryAt = null;
        } else {
            this.nextRetryAt = now.plus(backoff.multipliedBy(this.attemptCount));
        }
    }
}
