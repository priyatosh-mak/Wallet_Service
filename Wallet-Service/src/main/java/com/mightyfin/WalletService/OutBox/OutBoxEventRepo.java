
package com.mightyfin.WalletService.OutBox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutBoxEventRepo extends JpaRepository<OutBoxEvent, Long> {

    @Query(value = """
            SELECT * FROM outbox_event
            WHERE status = 'PENDING'
              AND (next_retry_at IS NULL OR next_retry_at <= now())
            ORDER BY created_at, id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutBoxEvent> lockPendingBatch(@Param("limit") int limit);
}
