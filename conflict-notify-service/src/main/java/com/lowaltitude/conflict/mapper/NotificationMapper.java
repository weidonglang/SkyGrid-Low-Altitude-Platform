package com.lowaltitude.conflict.mapper;

import com.lowaltitude.conflict.domain.AuditLog;
import com.lowaltitude.conflict.domain.IdempotentRecord;
import com.lowaltitude.conflict.domain.NotifyRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {
    @Insert("""
            INSERT IGNORE INTO idempotent_record(message_key, event_type, consumer_name, status, processed_at)
            VALUES(#{messageKey}, #{eventType}, #{consumerName}, #{status}, CURRENT_TIMESTAMP)
            """)
    int insertIdempotent(@Param("messageKey") String messageKey,
                         @Param("eventType") String eventType,
                         @Param("consumerName") String consumerName,
                         @Param("status") String status);

    @Select("""
            SELECT id, message_key AS messageKey, event_type AS eventType, consumer_name AS consumerName,
                   status, processed_at AS processedAt, created_at AS createdAt
            FROM idempotent_record
            WHERE (#{messageKey} IS NULL OR message_key = #{messageKey})
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<IdempotentRecord> listIdempotent(@Param("messageKey") String messageKey, @Param("limit") int limit);

    @Insert("""
            INSERT INTO notify_record(message_key, event_type, booking_id, booking_no, recipient_type, recipient,
                                      channel, subject, content, status, sent_at, error_message)
            VALUES(#{messageKey}, #{eventType}, #{bookingId}, #{bookingNo}, #{recipientType}, #{recipient},
                   #{channel}, #{subject}, #{content}, #{status}, #{sentAt}, #{errorMessage})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertNotify(NotifyRecord record);

    @Select("""
            SELECT id, message_key AS messageKey, event_type AS eventType, booking_id AS bookingId,
                   booking_no AS bookingNo, recipient_type AS recipientType, recipient, channel,
                   subject, content, status, sent_at AS sentAt, error_message AS errorMessage,
                   created_at AS createdAt
            FROM notify_record
            WHERE (#{bookingNo} IS NULL OR booking_no = #{bookingNo})
              AND (#{messageKey} IS NULL OR message_key = #{messageKey})
              AND (#{eventType} IS NULL OR event_type = #{eventType})
              AND (#{status} IS NULL OR status = #{status})
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<NotifyRecord> listNotify(@Param("bookingNo") String bookingNo,
                                  @Param("messageKey") String messageKey,
                                  @Param("eventType") String eventType,
                                  @Param("status") String status,
                                  @Param("limit") int limit);

    @Insert("""
            INSERT INTO audit_log(message_key, event_type, booking_id, booking_no, action, detail)
            VALUES(#{messageKey}, #{eventType}, #{bookingId}, #{bookingNo}, #{action}, #{detail})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAudit(AuditLog log);

    @Select("""
            SELECT id, message_key AS messageKey, event_type AS eventType, booking_id AS bookingId,
                   booking_no AS bookingNo, action, detail, created_at AS createdAt
            FROM audit_log
            WHERE (#{bookingNo} IS NULL OR booking_no = #{bookingNo})
              AND (#{messageKey} IS NULL OR message_key = #{messageKey})
              AND (#{eventType} IS NULL OR event_type = #{eventType})
              AND (#{action} IS NULL OR action = #{action})
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<AuditLog> listAudit(@Param("bookingNo") String bookingNo,
                             @Param("messageKey") String messageKey,
                             @Param("eventType") String eventType,
                             @Param("action") String action,
                             @Param("limit") int limit);

    @Select("SELECT COUNT(1) FROM notify_record WHERE status = #{status}")
    int countNotifyByStatus(String status);

    @Select("SELECT COUNT(1) FROM audit_log")
    int countAuditAll();

    @Select("SELECT COUNT(1) FROM idempotent_record WHERE status = #{status}")
    int countIdempotentByStatus(String status);
}
