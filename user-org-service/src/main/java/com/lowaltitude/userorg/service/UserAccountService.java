package com.lowaltitude.userorg.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.userorg.domain.Role;
import com.lowaltitude.userorg.domain.UserAccount;
import com.lowaltitude.userorg.dto.UserAccountRequest;
import com.lowaltitude.userorg.mapper.OrganizationMapper;
import com.lowaltitude.userorg.mapper.RoleMapper;
import com.lowaltitude.userorg.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAccountService {
    private final UserAccountMapper userMapper;
    private final OrganizationMapper organizationMapper;
    private final RoleMapper roleMapper;

    public UserAccountService(UserAccountMapper userMapper, OrganizationMapper organizationMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.organizationMapper = organizationMapper;
        this.roleMapper = roleMapper;
    }

    public List<UserAccount> list(String keyword) { return userMapper.list(keyword); }

    public UserAccount get(Long id) {
        UserAccount user = userMapper.findById(id);
        if (user == null) throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + id);
        return user;
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("user", get(id));
        result.put("roles", userMapper.listUserRoles(id));
        return result;
    }

    @Transactional
    public UserAccount create(UserAccountRequest request) {
        if (userMapper.countByUsername(request.getUsername()) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在: " + request.getUsername());
        }
        validateOrg(request.getOrgId());
        UserAccount user = toEntity(request);
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash("DEV_NOT_ENCRYPTED_CHANGE_IN_PHASE_AUTH");
        }
        userMapper.insert(user);
        return get(user.getId());
    }

    @Transactional
    public UserAccount update(Long id, UserAccountRequest request) {
        UserAccount old = get(id);
        validateOrg(request.getOrgId());
        UserAccount user = toEntity(request);
        user.setId(id);
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(old.getPasswordHash());
        }
        userMapper.update(user);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (userMapper.softDelete(id) == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在: " + id);
    }

    @Transactional
    public List<Role> assignRole(Long userId, Long roleId) {
        get(userId);
        if (roleMapper.findById(roleId) == null) throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在: " + roleId);
        userMapper.assignRole(userId, roleId);
        return userMapper.listUserRoles(userId);
    }

    @Transactional
    public List<Role> removeRole(Long userId, Long roleId) {
        get(userId);
        userMapper.removeRole(userId, roleId);
        return userMapper.listUserRoles(userId);
    }

    private void validateOrg(Long orgId) {
        if (orgId != null && organizationMapper.findById(orgId) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "组织不存在: " + orgId);
        }
    }

    private UserAccount toEntity(UserAccountRequest request) {
        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPasswordHash(request.getPasswordHash());
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setOrgId(request.getOrgId());
        user.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        return user;
    }
}
