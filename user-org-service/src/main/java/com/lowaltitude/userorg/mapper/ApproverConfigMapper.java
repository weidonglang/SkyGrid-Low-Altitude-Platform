package com.lowaltitude.userorg.mapper;

import com.lowaltitude.userorg.domain.ApproverConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApproverConfigMapper {
    @Select("""
            SELECT ac.id, ac.org_id AS orgId, o.org_name AS orgName,
                   ac.approver_user_id AS approverUserId, u.real_name AS approverRealName,
                   ac.level_order AS levelOrder, ac.enabled, ac.description,
                   ac.created_at AS createdAt, ac.updated_at AS updatedAt
            FROM approver_config ac
            LEFT JOIN organization o ON ac.org_id = o.id AND o.deleted = 0
            LEFT JOIN user_account u ON ac.approver_user_id = u.id AND u.deleted = 0
            WHERE ac.deleted = 0
              AND (#{orgId} IS NULL OR ac.org_id = #{orgId})
            ORDER BY ac.org_id ASC, ac.level_order ASC, ac.id ASC
            """)
    List<ApproverConfig> list(@Param("orgId") Long orgId);

    @Select("""
            SELECT ac.id, ac.org_id AS orgId, o.org_name AS orgName,
                   ac.approver_user_id AS approverUserId, u.real_name AS approverRealName,
                   ac.level_order AS levelOrder, ac.enabled, ac.description,
                   ac.created_at AS createdAt, ac.updated_at AS updatedAt
            FROM approver_config ac
            LEFT JOIN organization o ON ac.org_id = o.id AND o.deleted = 0
            LEFT JOIN user_account u ON ac.approver_user_id = u.id AND u.deleted = 0
            WHERE ac.id = #{id} AND ac.deleted = 0
            """)
    ApproverConfig findById(Long id);

    @Insert("""
            INSERT INTO approver_config(org_id, approver_user_id, level_order, enabled, description)
            VALUES(#{orgId}, #{approverUserId}, #{levelOrder}, #{enabled}, #{description})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ApproverConfig config);

    @Update("""
            UPDATE approver_config
            SET org_id = #{orgId}, approver_user_id = #{approverUserId}, level_order = #{levelOrder},
                enabled = #{enabled}, description = #{description}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(ApproverConfig config);

    @Update("UPDATE approver_config SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);
}
