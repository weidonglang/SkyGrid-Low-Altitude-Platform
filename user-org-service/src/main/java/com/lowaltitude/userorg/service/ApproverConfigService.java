package com.lowaltitude.userorg.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.userorg.domain.ApproverConfig;
import com.lowaltitude.userorg.dto.ApproverConfigRequest;
import com.lowaltitude.userorg.mapper.ApproverConfigMapper;
import com.lowaltitude.userorg.mapper.OrganizationMapper;
import com.lowaltitude.userorg.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApproverConfigService {
    private final ApproverConfigMapper mapper;
    private final OrganizationMapper organizationMapper;
    private final UserAccountMapper userMapper;

    public ApproverConfigService(ApproverConfigMapper mapper, OrganizationMapper organizationMapper, UserAccountMapper userMapper) {
        this.mapper = mapper;
        this.organizationMapper = organizationMapper;
        this.userMapper = userMapper;
    }

    public List<ApproverConfig> list(Long orgId) { return mapper.list(orgId); }

    public ApproverConfig get(Long id) {
        ApproverConfig config = mapper.findById(id);
        if (config == null) throw new BusinessException(ErrorCode.NOT_FOUND, "审批配置不存在: " + id);
        return config;
    }

    @Transactional
    public ApproverConfig create(ApproverConfigRequest request) {
        validateRefs(request);
        ApproverConfig config = toEntity(request);
        mapper.insert(config);
        return get(config.getId());
    }

    @Transactional
    public ApproverConfig update(Long id, ApproverConfigRequest request) {
        get(id);
        validateRefs(request);
        ApproverConfig config = toEntity(request);
        config.setId(id);
        mapper.update(config);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (mapper.softDelete(id) == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "审批配置不存在: " + id);
    }

    private void validateRefs(ApproverConfigRequest request) {
        if (organizationMapper.findById(request.getOrgId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "组织不存在: " + request.getOrgId());
        }
        if (userMapper.findById(request.getApproverUserId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "审批员不存在: " + request.getApproverUserId());
        }
    }

    private ApproverConfig toEntity(ApproverConfigRequest request) {
        ApproverConfig config = new ApproverConfig();
        config.setOrgId(request.getOrgId());
        config.setApproverUserId(request.getApproverUserId());
        config.setLevelOrder(request.getLevelOrder());
        config.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        config.setDescription(request.getDescription());
        return config;
    }
}
