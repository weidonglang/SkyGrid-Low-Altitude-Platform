package com.lowaltitude.booking.mapper;

import com.lowaltitude.booking.domain.OutboxMessage;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OutboxMapper {
    @Insert("""
            INSERT INTO outbox_message(message_key, event_type, aggregate_type, aggregate_id, routing_key,
                                       payload, status, retry_count, max_retry_count, next_retry_at)
            VALUES(#{messageKey}, #{eventType}, #{aggregateType}, #{aggregateId}, #{routingKey},
                   #{payload}, #{status}, #{retryCount}, #{maxRetryCount}, #{nextRetryAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OutboxMessage message);

    @Select("""
            SELECT id, message_key AS messageKey, event_type AS eventType, aggregate_type AS aggregateType,
                   aggregate_id AS aggregateId, routing_key AS routingKey, payload, status,
                   retry_count AS retryCount, max_retry_count AS maxRetryCount,
                   next_retry_at AS nextRetryAt, sent_at AS sentAt, last_error AS lastError,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM outbox_message
            WHERE (#{status} IS NULL OR status = #{status})
              AND (#{aggregateId} IS NULL OR aggregate_id = #{aggregateId})
              AND (#{eventType} IS NULL OR event_type = #{eventType})
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<OutboxMessage> list(@Param("status") String status,
                             @Param("aggregateId") Long aggregateId,
                             @Param("eventType") String eventType,
                             @Param("limit") int limit);

    @Select("""
            SELECT id, message_key AS messageKey, event_type AS eventType, aggregate_type AS aggregateType,
                   aggregate_id AS aggregateId, routing_key AS routingKey, payload, status,
                   retry_count AS retryCount, max_retry_count AS maxRetryCount,
                   next_retry_at AS nextRetryAt, sent_at AS sentAt, last_error AS lastError,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM outbox_message
            WHERE id = #{id}
            """)
    OutboxMessage findById(Long id);

    @Select("""
            SELECT id, message_key AS messageKey, event_type AS eventType, aggregate_type AS aggregateType,
                   aggregate_id AS aggregateId, routing_key AS routingKey, payload, status,
                   retry_count AS retryCount, max_retry_count AS maxRetryCount,
                   next_retry_at AS nextRetryAt, sent_at AS sentAt, last_error AS lastError,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM outbox_message
            WHERE status IN ('PENDING', 'FAILED')
              AND retry_count < max_retry_count
              AND next_retry_at <= CURRENT_TIMESTAMP
            ORDER BY id ASC
            LIMIT #{limit}
            """)
    List<OutboxMessage> listReadyForDispatch(int limit);

    @Update("""
            UPDATE outbox_message
            SET status = 'SENT', sent_at = CURRENT_TIMESTAMP, last_error = NULL
            WHERE id = #{id} AND status IN ('PENDING', 'FAILED')
            """)
    int markSent(Long id);

    @Update("""
            UPDATE outbox_message
            SET status = 'FAILED', retry_count = retry_count + 1, next_retry_at = #{nextRetryAt}, last_error = #{lastError}
            WHERE id = #{id} AND status IN ('PENDING', 'FAILED')
            """)
    int markFailed(@Param("id") Long id,
                   @Param("nextRetryAt") LocalDateTime nextRetryAt,
                   @Param("lastError") String lastError);

    @Update("""
            UPDATE outbox_message
            SET status = 'PENDING', next_retry_at = CURRENT_TIMESTAMP, last_error = NULL
            WHERE id = #{id} AND status = 'FAILED'
            """)
    int requeueFailed(Long id);

    @Select("SELECT COUNT(1) FROM outbox_message WHERE status = #{status}")
    int countByStatus(String status);

    @Select("SELECT COUNT(1) FROM outbox_message")
    int countAll();
}
