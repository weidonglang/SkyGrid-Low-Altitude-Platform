package com.lowaltitude.userorg.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.userorg.domain.Role;
import com.lowaltitude.userorg.dto.RoleRequest;
import com.lowaltitude.userorg.mapper.RoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {
    private final RoleMapper mapper;

    public RoleService(RoleMapper mapper) { this.mapper = mapper; }

    public List<Role> list(String keyword) { return mapper.list(keyword); }

    public Role get(Long id) {
        Role role = mapper.findById(id);
        if (role == null) throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在: " + id);
        return role;
    }

    @Transactional
    public Role create(RoleRequest request) {
        if (mapper.countByCode(request.getRoleCode()) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在: " + request.getRoleCode());
        }
        Role role = toEntity(request);
        mapper.insert(role);
        return get(role.getId());
    }

    @Transactional
    public Role update(Long id, RoleRequest request) {
        get(id);
        Role role = toEntity(request);
        role.setId(id);
        mapper.update(role);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (mapper.softDelete(id) == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在: " + id);
    }

    private Role toEntity(RoleRequest request) {
        Role role = new Role();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setDescription(request.getDescription());
        role.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        return role;
    }
}
