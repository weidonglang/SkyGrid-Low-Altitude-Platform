package com.lowaltitude.userorg.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.userorg.domain.Organization;
import com.lowaltitude.userorg.dto.OrganizationRequest;
import com.lowaltitude.userorg.mapper.OrganizationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrganizationService {
    private final OrganizationMapper mapper;

    public OrganizationService(OrganizationMapper mapper) { this.mapper = mapper; }

    public List<Organization> list(String keyword) { return mapper.list(keyword); }

    public Organization get(Long id) {
        Organization organization = mapper.findById(id);
        if (organization == null) throw new BusinessException(ErrorCode.NOT_FOUND, "组织不存在: " + id);
        return organization;
    }

    @Transactional
    public Organization create(OrganizationRequest request) {
        if (mapper.countByCode(request.getOrgCode()) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "组织编码已存在: " + request.getOrgCode());
        }
        Organization organization = toEntity(request);
        mapper.insert(organization);
        return get(organization.getId());
    }

    @Transactional
    public Organization update(Long id, OrganizationRequest request) {
        get(id);
        Organization organization = toEntity(request);
        organization.setId(id);
        mapper.update(organization);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (mapper.softDelete(id) == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "组织不存在: " + id);
    }

    private Organization toEntity(OrganizationRequest request) {
        Organization organization = new Organization();
        organization.setOrgCode(request.getOrgCode());
        organization.setOrgName(request.getOrgName());
        organization.setParentId(request.getParentId());
        organization.setContactName(request.getContactName());
        organization.setContactPhone(request.getContactPhone());
        organization.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        organization.setDescription(request.getDescription());
        return organization;
    }
}
