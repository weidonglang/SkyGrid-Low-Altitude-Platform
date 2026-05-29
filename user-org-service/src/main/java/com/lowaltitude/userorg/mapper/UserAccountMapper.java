package com.lowaltitude.userorg.mapper;

import com.lowaltitude.userorg.domain.UserAccount;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserAccountMapper {
    @Select("""
            SELECT u.id, u.username, u.password_hash AS passwordHash, u.real_name AS realName,
                   u.phone, u.email, u.org_id AS orgId, o.org_name AS orgName,
                   u.enabled, u.created_at AS createdAt, u.updated_at AS updatedAt
            FROM user_account u
            LEFT JOIN organization o ON u.org_id = o.id AND o.deleted = 0
            WHERE u.deleted = 0
              AND (#{keyword} IS NULL OR #{keyword} = '' OR u.username LIKE CONCAT('%', #{keyword}, '%') OR u.real_name LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY u.id ASC
            """)
    List<UserAccount> list(@Param("keyword") String keyword);

    @Select("""
            SELECT u.id, u.username, u.password_hash AS passwordHash, u.real_name AS realName,
                   u.phone, u.email, u.org_id AS orgId, o.org_name AS orgName,
                   u.enabled, u.created_at AS createdAt, u.updated_at AS updatedAt
            FROM user_account u
            LEFT JOIN organization o ON u.org_id = o.id AND o.deleted = 0
            WHERE u.id = #{id} AND u.deleted = 0
            """)
    UserAccount findById(Long id);

    @Select("SELECT COUNT(1) FROM user_account WHERE username = #{username} AND deleted = 0")
    int countByUsername(String username);

    @Insert("""
            INSERT INTO user_account(username, password_hash, real_name, phone, email, org_id, enabled)
            VALUES(#{username}, #{passwordHash}, #{realName}, #{phone}, #{email}, #{orgId}, #{enabled})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAccount user);

    @Update("""
            UPDATE user_account
            SET username = #{username}, password_hash = #{passwordHash}, real_name = #{realName},
                phone = #{phone}, email = #{email}, org_id = #{orgId}, enabled = #{enabled}
            WHERE id = #{id} AND deleted = 0
            """)
    int update(UserAccount user);

    @Update("UPDATE user_account SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int softDelete(Long id);

    @Insert("INSERT IGNORE INTO user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int assignRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int removeRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Select("""
            SELECT r.id, r.role_code AS roleCode, r.role_name AS roleName, r.description, r.enabled,
                   r.created_at AS createdAt, r.updated_at AS updatedAt
            FROM sys_role r
            INNER JOIN user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId} AND r.deleted = 0
            ORDER BY r.id ASC
            """)
    List<com.lowaltitude.userorg.domain.Role> listUserRoles(Long userId);
}
