package com.lowaltitude.resource.mapper;

import com.lowaltitude.resource.domain.TimeSlot;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TimeSlotMapper {
    @Select("""
            SELECT id, slot_code AS slotCode, slot_name AS slotName,
                   start_time AS startTime, end_time AS endTime,
                   sort_order AS sortOrder, enabled, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM time_slot
            WHERE deleted = 0
              AND (#{enabled} IS NULL OR enabled = #{enabled})
              AND (#{keyword} IS NULL OR #{keyword} = '' OR slot_code LIKE CONCAT('%', #{keyword}, '%') OR slot_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY sort_order ASC, start_time ASC, id ASC
            """)
    List<TimeSlot> list(@Param("keyword") String keyword, @Param("enabled") Boolean enabled);

    @Select("""
            SELECT id, slot_code AS slotCode, slot_name AS slotName,
                   start_time AS startTime, end_time AS endTime,
                   sort_order AS sortOrder, enabled, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM time_slot WHERE id = #{id} AND deleted = 0
            """)
    TimeSlot findById(Long id);

    @Select("SELECT COUNT(1) FROM time_slot WHERE slot_code = #{slotCode} AND deleted = 0")
    int countByCode(String slotCode);

    @Insert("""
            INSERT INTO time_slot(slot_code, slot_name, start_time, end_time, sort_order, enabled, description)
            VALUES(#{slotCode}, #{slotName}, #{startTime}, #{endTime}, #{sortOrder}, #{enabled}, #{description})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TimeSlot slot);

    @Update("""
            UPDATE time_slot
            SET slot_code = #{slotCode}, slot_name = #{slotName}, start_time = #{startTime}, end_time = #{endTime},
                sort_order = #{sortOrder}, enabled = #{enabled}, description = #{description}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(TimeSlot slot);

    @Update("UPDATE time_slot SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);
}
