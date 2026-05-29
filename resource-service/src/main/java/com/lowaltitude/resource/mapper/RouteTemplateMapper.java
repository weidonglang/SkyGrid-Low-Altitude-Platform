package com.lowaltitude.resource.mapper;

import com.lowaltitude.resource.domain.RouteTemplate;
import com.lowaltitude.resource.domain.RouteTemplateGrid;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RouteTemplateMapper {
    @Select("""
            SELECT id, route_code AS routeCode, route_name AS routeName, description, enabled, created_by AS createdBy,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM route_template
            WHERE deleted = 0
              AND (#{enabled} IS NULL OR enabled = #{enabled})
              AND (#{keyword} IS NULL OR #{keyword} = '' OR route_code LIKE CONCAT('%', #{keyword}, '%') OR route_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY id ASC
            """)
    List<RouteTemplate> list(@Param("keyword") String keyword, @Param("enabled") Boolean enabled);

    @Select("""
            SELECT id, route_code AS routeCode, route_name AS routeName, description, enabled, created_by AS createdBy,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM route_template WHERE id = #{id} AND deleted = 0
            """)
    RouteTemplate findById(Long id);

    @Select("SELECT COUNT(1) FROM route_template WHERE route_code = #{routeCode} AND deleted = 0")
    int countByCode(String routeCode);

    @Insert("""
            INSERT INTO route_template(route_code, route_name, description, enabled, created_by)
            VALUES(#{routeCode}, #{routeName}, #{description}, #{enabled}, #{createdBy})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RouteTemplate route);

    @Update("""
            UPDATE route_template
            SET route_code = #{routeCode}, route_name = #{routeName}, description = #{description},
                enabled = #{enabled}, created_by = #{createdBy}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(RouteTemplate route);

    @Update("UPDATE route_template SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);

    @Select("""
            SELECT rtg.id, rtg.route_template_id AS routeTemplateId, rtg.grid_id AS gridId,
                   g.grid_code AS gridCode, g.grid_name AS gridName, g.status AS gridStatus,
                   g.row_index AS rowIndex, g.col_index AS colIndex,
                   rtg.sequence_no AS sequenceNo, rtg.planned_duration_minutes AS plannedDurationMinutes
            FROM route_template_grid rtg
            INNER JOIN grid g ON rtg.grid_id = g.id AND g.deleted = 0
            WHERE rtg.route_template_id = #{routeTemplateId}
            ORDER BY rtg.sequence_no ASC, rtg.id ASC
            """)
    List<RouteTemplateGrid> listRouteGrids(Long routeTemplateId);

    @Insert("""
            INSERT INTO route_template_grid(route_template_id, grid_id, sequence_no, planned_duration_minutes)
            VALUES(#{routeTemplateId}, #{gridId}, #{sequenceNo}, #{plannedDurationMinutes})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRouteGrid(RouteTemplateGrid routeGrid);

    @Update("""
            UPDATE route_template_grid
            SET grid_id = #{gridId}, sequence_no = #{sequenceNo}, planned_duration_minutes = #{plannedDurationMinutes}
            WHERE id = #{id}
            """)
    int updateRouteGrid(RouteTemplateGrid routeGrid);

    @Delete("DELETE FROM route_template_grid WHERE id = #{routeGridId} AND route_template_id = #{routeTemplateId}")
    int deleteRouteGrid(@Param("routeTemplateId") Long routeTemplateId, @Param("routeGridId") Long routeGridId);

    @Delete("DELETE FROM route_template_grid WHERE route_template_id = #{routeTemplateId}")
    int deleteAllRouteGrids(Long routeTemplateId);
}
