package com.lowaltitude.resource.mapper;

import com.lowaltitude.resource.domain.Grid;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GridMapper {
    @Select("""
            SELECT id, grid_code AS gridCode, grid_name AS gridName, row_index AS rowIndex, col_index AS colIndex,
                   center_lon AS centerLon, center_lat AS centerLat, status, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM grid
            WHERE deleted = 0
              AND (#{status} IS NULL OR #{status} = '' OR status = #{status})
              AND (#{keyword} IS NULL OR #{keyword} = '' OR grid_code LIKE CONCAT('%', #{keyword}, '%') OR grid_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY row_index ASC, col_index ASC, id ASC
            """)
    List<Grid> list(@Param("keyword") String keyword, @Param("status") String status);

    @Select("""
            SELECT id, grid_code AS gridCode, grid_name AS gridName, row_index AS rowIndex, col_index AS colIndex,
                   center_lon AS centerLon, center_lat AS centerLat, status, description,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM grid WHERE id = #{id} AND deleted = 0
            """)
    Grid findById(Long id);

    @Select("SELECT COUNT(1) FROM grid WHERE grid_code = #{gridCode} AND deleted = 0")
    int countByCode(String gridCode);

    @Insert("""
            INSERT INTO grid(grid_code, grid_name, row_index, col_index, center_lon, center_lat, status, description)
            VALUES(#{gridCode}, #{gridName}, #{rowIndex}, #{colIndex}, #{centerLon}, #{centerLat}, #{status}, #{description})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Grid grid);

    @Update("""
            UPDATE grid
            SET grid_code = #{gridCode}, grid_name = #{gridName}, row_index = #{rowIndex}, col_index = #{colIndex},
                center_lon = #{centerLon}, center_lat = #{centerLat}, status = #{status}, description = #{description}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(Grid grid);

    @Update("UPDATE grid SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);
}
