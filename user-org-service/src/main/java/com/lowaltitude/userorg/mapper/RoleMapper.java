package com.lowaltitude.userorg.mapper;

import com.lowaltitude.userorg.domain.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {
    @Select("""
            SELECT id, role_code AS roleCode, role_name AS roleName, description, enabled,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM sys_role
            WHERE deleted = 0
              AND (#{keyword} IS NULL OR #{keyword} = '' OR role_code LIKE CONCAT('%', #{keyword}, '%') OR role_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY id ASC
            """)
    List<Role> list(@Param("keyword") String keyword);

    @Select("""
            SELECT id, role_code AS roleCode, role_name AS roleName, description, enabled,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM sys_role WHERE id = #{id} AND deleted = 0
            """)
    Role findById(Long id);

    @Select("SELECT COUNT(1) FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0")
    int countByCode(String roleCode);

    @Insert("""
            INSERT INTO sys_role(role_code, role_name, description, enabled)
            VALUES(#{roleCode}, #{roleName}, #{description}, #{enabled})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    @Update("""
            UPDATE sys_role SET role_code = #{roleCode}, role_name = #{roleName},
                description = #{description}, enabled = #{enabled}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(Role role);

    @Update("UPDATE sys_role SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);
}
