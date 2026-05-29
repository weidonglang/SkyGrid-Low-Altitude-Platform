package com.lowaltitude.booking.mapper;

import com.lowaltitude.booking.domain.BookingFlowRecord;
import com.lowaltitude.booking.domain.BookingRecord;
import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.domain.ResourceOccupancy;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BookingMapper {
    @Select("""
            SELECT id, booking_no AS bookingNo, task_name AS taskName, org_id AS orgId,
                   applicant_user_id AS applicantUserId, applicant_name AS applicantName,
                   route_template_id AS routeTemplateId, route_template_name AS routeTemplateName,
                   level_id AS levelId, booking_date AS bookingDate, status,
                   apply_reason AS applyReason, approval_comment AS approvalComment,
                   reject_reason AS rejectReason, cancel_reason AS cancelReason, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM booking_record
            WHERE deleted = 0
              AND (#{status} IS NULL OR status = #{status})
              AND (#{applicantUserId} IS NULL OR applicant_user_id = #{applicantUserId})
              AND (#{bookingDate} IS NULL OR booking_date = #{bookingDate})
              AND (#{keyword} IS NULL OR #{keyword} = '' OR booking_no LIKE CONCAT('%', #{keyword}, '%') OR task_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY id DESC
            """)
    List<BookingRecord> list(@Param("status") String status,
                             @Param("applicantUserId") Long applicantUserId,
                             @Param("bookingDate") LocalDate bookingDate,
                             @Param("keyword") String keyword);

    @Select("""
            SELECT id, booking_no AS bookingNo, task_name AS taskName, org_id AS orgId,
                   applicant_user_id AS applicantUserId, applicant_name AS applicantName,
                   route_template_id AS routeTemplateId, route_template_name AS routeTemplateName,
                   level_id AS levelId, booking_date AS bookingDate, status,
                   apply_reason AS applyReason, approval_comment AS approvalComment,
                   reject_reason AS rejectReason, cancel_reason AS cancelReason, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM booking_record
            WHERE id = #{id} AND deleted = 0
            """)
    BookingRecord findById(Long id);

    @Select("SELECT COUNT(1) FROM booking_record WHERE booking_no = #{bookingNo}")
    int countByBookingNo(String bookingNo);

    @Insert("""
            INSERT INTO booking_record(booking_no, task_name, org_id, applicant_user_id, applicant_name,
                                       route_template_id, route_template_name, level_id, booking_date, status,
                                       apply_reason, description)
            VALUES(#{bookingNo}, #{taskName}, #{orgId}, #{applicantUserId}, #{applicantName},
                   #{routeTemplateId}, #{routeTemplateName}, #{levelId}, #{bookingDate}, #{status},
                   #{applyReason}, #{description})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BookingRecord booking);

    @Update("""
            UPDATE booking_record
            SET status = #{toStatus}, approval_comment = #{approvalComment}
            WHERE id = #{id} AND deleted = 0 AND status = #{fromStatus}
            """)
    int approve(@Param("id") Long id,
                @Param("fromStatus") String fromStatus,
                @Param("toStatus") String toStatus,
                @Param("approvalComment") String approvalComment);

    @Update("""
            UPDATE booking_record
            SET status = #{toStatus}, reject_reason = #{rejectReason}
            WHERE id = #{id} AND deleted = 0 AND status = #{fromStatus}
            """)
    int reject(@Param("id") Long id,
               @Param("fromStatus") String fromStatus,
               @Param("toStatus") String toStatus,
               @Param("rejectReason") String rejectReason);

    @Update("""
            UPDATE booking_record
            SET status = #{toStatus}, cancel_reason = #{cancelReason}
            WHERE id = #{id} AND deleted = 0 AND status = #{fromStatus}
            """)
    int cancel(@Param("id") Long id,
               @Param("fromStatus") String fromStatus,
               @Param("toStatus") String toStatus,
               @Param("cancelReason") String cancelReason);

    @Insert("""
            INSERT INTO booking_time_slot(booking_id, time_slot_id)
            VALUES(#{bookingId}, #{timeSlotId})
            """)
    int insertBookingTimeSlot(@Param("bookingId") Long bookingId, @Param("timeSlotId") Long timeSlotId);

    @Select("SELECT time_slot_id FROM booking_time_slot WHERE booking_id = #{bookingId} ORDER BY time_slot_id ASC")
    List<Long> listTimeSlotIds(Long bookingId);

    @Insert("""
            INSERT INTO booking_flow_record(booking_id, action, from_status, to_status, operator_user_id, operator_name, comment)
            VALUES(#{bookingId}, #{action}, #{fromStatus}, #{toStatus}, #{operatorUserId}, #{operatorName}, #{comment})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertFlow(BookingFlowRecord flow);

    @Select("""
            SELECT id, booking_id AS bookingId, action, from_status AS fromStatus, to_status AS toStatus,
                   operator_user_id AS operatorUserId, operator_name AS operatorName, comment, created_at AS createdAt
            FROM booking_flow_record
            WHERE booking_id = #{bookingId}
            ORDER BY id ASC
            """)
    List<BookingFlowRecord> listFlows(Long bookingId);

    @Insert("""
            INSERT INTO resource_occupancy(booking_id, booking_no, route_template_id, grid_id, grid_code, grid_name,
                                           level_id, time_slot_id, booking_date, status)
            VALUES(#{bookingId}, #{bookingNo}, #{routeTemplateId}, #{gridId}, #{gridCode}, #{gridName},
                   #{levelId}, #{timeSlotId}, #{bookingDate}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOccupancy(ResourceOccupancy occupancy);

    @Select("""
            SELECT id, booking_id AS bookingId, booking_no AS bookingNo, route_template_id AS routeTemplateId,
                   grid_id AS gridId, grid_code AS gridCode, grid_name AS gridName,
                   level_id AS levelId, time_slot_id AS timeSlotId, booking_date AS bookingDate, status,
                   created_at AS createdAt, released_at AS releasedAt
            FROM resource_occupancy
            WHERE booking_id = #{bookingId}
            ORDER BY id ASC
            """)
    List<ResourceOccupancy> listOccupancies(Long bookingId);

    @Select("""
            SELECT id, booking_id AS bookingId, booking_no AS bookingNo, route_template_id AS routeTemplateId,
                   grid_id AS gridId, grid_code AS gridCode, grid_name AS gridName,
                   level_id AS levelId, time_slot_id AS timeSlotId, booking_date AS bookingDate, status,
                   created_at AS createdAt, released_at AS releasedAt
            FROM resource_occupancy
            WHERE status = 'OCCUPIED'
              AND booking_date = #{bookingDate}
              AND (#{excludeBookingId} IS NULL OR booking_id <> #{excludeBookingId})
            """)
    List<ResourceOccupancy> listActiveOccupanciesForDate(@Param("bookingDate") LocalDate bookingDate,
                                                         @Param("excludeBookingId") Long excludeBookingId);

    @Update("""
            UPDATE resource_occupancy
            SET status = 'RELEASED', released_at = CURRENT_TIMESTAMP
            WHERE booking_id = #{bookingId} AND status = 'OCCUPIED'
            """)
    int releaseOccupancies(Long bookingId);

    @Insert("""
            INSERT INTO conflict_record(booking_id, booking_no, conflict_type, conflict_level, rule_code,
                                        grid_id, grid_code, grid_name, level_id, time_slot_id, booking_date,
                                        related_booking_id, related_booking_no, message, status)
            VALUES(#{bookingId}, #{bookingNo}, #{conflictType}, #{conflictLevel}, #{ruleCode},
                   #{gridId}, #{gridCode}, #{gridName}, #{levelId}, #{timeSlotId}, #{bookingDate},
                   #{relatedBookingId}, #{relatedBookingNo}, #{message}, #{status})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertConflict(ConflictRecord record);

    @Select("""
            SELECT id, booking_id AS bookingId, booking_no AS bookingNo,
                   conflict_type AS conflictType, conflict_level AS conflictLevel, rule_code AS ruleCode,
                   grid_id AS gridId, grid_code AS gridCode, grid_name AS gridName,
                   level_id AS levelId, time_slot_id AS timeSlotId, booking_date AS bookingDate,
                   related_booking_id AS relatedBookingId, related_booking_no AS relatedBookingNo,
                   message, status, created_at AS createdAt, resolved_at AS resolvedAt
            FROM conflict_record
            WHERE (#{bookingId} IS NULL OR booking_id = #{bookingId})
              AND (#{conflictType} IS NULL OR conflict_type = #{conflictType})
              AND (#{status} IS NULL OR status = #{status})
            ORDER BY id DESC
            """)
    List<ConflictRecord> listConflicts(@Param("bookingId") Long bookingId,
                                       @Param("conflictType") String conflictType,
                                       @Param("status") String status);

    @Update("""
            UPDATE conflict_record
            SET status = 'RESOLVED', resolved_at = CURRENT_TIMESTAMP
            WHERE booking_id = #{bookingId} AND status = 'ACTIVE'
            """)
    int resolveConflicts(Long bookingId);

    @Delete("DELETE FROM conflict_record WHERE booking_id = #{bookingId} AND status = 'ACTIVE'")
    int deleteActiveConflicts(Long bookingId);

    @Select("SELECT COUNT(1) FROM booking_record WHERE deleted = 0 AND status = #{status}")
    int countBookingByStatus(String status);

    @Select("SELECT COUNT(1) FROM resource_occupancy WHERE status = #{status}")
    int countOccupancyByStatus(String status);

    @Select("SELECT COUNT(1) FROM conflict_record WHERE status = #{status}")
    int countConflictByStatus(String status);
}
