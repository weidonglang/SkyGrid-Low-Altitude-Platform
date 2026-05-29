package com.lowaltitude.resource.mapper;

import com.lowaltitude.resource.domain.AltitudeLevel;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AltitudeLevelMapper {
    @Select("""
            SELECT id, level_code AS levelCode, level_name AS levelName,
                   min_altitude_m AS minAltitudeM, max_altitude_m AS maxAltitudeM,
                   sort_order AS sortOrder, enabled, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM altitude_level
            WHERE deleted = 0
              AND (#{enabled} IS NULL OR enabled = #{enabled})
              AND (#{keyword} IS NULL OR #{keyword} = '' OR level_code LIKE CONCAT('%', #{keyword}, '%') OR level_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY sort_order ASC, min_altitude_m ASC, id ASC
            """)
    List<AltitudeLevel> list(@Param("keyword") String keyword, @Param("enabled") Boolean enabled);

    @Select("""
            SELECT id, level_code AS levelCode, level_name AS levelName,
                   min_altitude_m AS minAltitudeM, max_altitude_m AS maxAltitudeM,
                   sort_order AS sortOrder, enabled, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM altitude_level WHERE id = #{id} AND deleted = 0
            """)
    AltitudeLevel findById(Long id);

    @Select("SELECT COUNT(1) FROM altitude_level WHERE level_code = #{levelCode} AND deleted = 0")
    int countByCode(String levelCode);

    @Insert("""
            INSERT INTO altitude_level(level_code, level_name, min_altitude_m, max_altitude_m, sort_order, enabled, description)
            VALUES(#{levelCode}, #{levelName}, #{minAltitudeM}, #{maxAltitudeM}, #{sortOrder}, #{enabled}, #{description})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AltitudeLevel level);

    @Update("""
            UPDATE altitude_level
            SET level_code = #{levelCode}, level_name = #{levelName}, min_altitude_m = #{minAltitudeM},
                max_altitude_m = #{maxAltitudeM}, sort_order = #{sortOrder}, enabled = #{enabled}, description = #{description}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(AltitudeLevel level);

    @Update("UPDATE altitude_level SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);
}
