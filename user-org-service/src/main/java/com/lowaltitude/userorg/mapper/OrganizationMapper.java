package com.lowaltitude.userorg.mapper;

import com.lowaltitude.userorg.domain.Organization;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrganizationMapper {
    @Select("""
            SELECT id, org_code AS orgCode, org_name AS orgName, parent_id AS parentId,
                   contact_name AS contactName, contact_phone AS contactPhone, enabled,
                   description, created_at AS createdAt, updated_at AS updatedAt
            FROM organization
            WHERE deleted = 0
              AND (#{keyword} IS NULL OR #{keyword} = '' OR org_code LIKE CONCAT('%', #{keyword}, '%') OR org_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY id ASC
            """)
    List<Organization> list(@Param("keyword") String keyword);

    @Select("""
            SELECT id, org_code AS orgCode, org_name AS orgName, parent_id AS parentId,
                   contact_name AS contactName, contact_phone AS contactPhone, enabled,
                   description, created_at AS createdAt, updated_at AS updatedAt
            FROM organization WHERE id = #{id} AND deleted = 0
            """)
    Organization findById(Long id);

    @Select("SELECT COUNT(1) FROM organization WHERE org_code = #{orgCode} AND deleted = 0")
    int countByCode(String orgCode);

    @Insert("""
            INSERT INTO organization(org_code, org_name, parent_id, contact_name, contact_phone, enabled, description)
            VALUES(#{orgCode}, #{orgName}, #{parentId}, #{contactName}, #{contactPhone}, #{enabled}, #{description})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Organization organization);

    @Update("""
            UPDATE organization
            SET org_code = #{orgCode}, org_name = #{orgName}, parent_id = #{parentId},
                contact_name = #{contactName}, contact_phone = #{contactPhone}, enabled = #{enabled},
                description = #{description}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(Organization organization);

    @Update("UPDATE organization SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);
}
