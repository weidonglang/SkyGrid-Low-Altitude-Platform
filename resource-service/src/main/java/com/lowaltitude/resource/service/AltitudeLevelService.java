package com.lowaltitude.resource.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.resource.domain.AltitudeLevel;
import com.lowaltitude.resource.dto.AltitudeLevelRequest;
import com.lowaltitude.resource.mapper.AltitudeLevelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AltitudeLevelService {
    private final AltitudeLevelMapper mapper;

    public AltitudeLevelService(AltitudeLevelMapper mapper) { this.mapper = mapper; }

    public List<AltitudeLevel> list(String keyword, Boolean enabled) { return mapper.list(keyword, enabled); }

    public AltitudeLevel get(Long id) {
        AltitudeLevel level = mapper.findById(id);
        if (level == null) throw new BusinessException(ErrorCode.NOT_FOUND, "高度层不存在: " + id);
        return level;
    }

    @Transactional
    public AltitudeLevel create(AltitudeLevelRequest request) {
        validateRange(request);
        if (mapper.countByCode(request.getLevelCode()) > 0) throw new BusinessException(ErrorCode.CONFLICT, "高度层编码已存在: " + request.getLevelCode());
        AltitudeLevel level = toEntity(request);
        mapper.insert(level);
        return get(level.getId());
    }

    @Transactional
    public AltitudeLevel update(Long id, AltitudeLevelRequest request) {
        get(id);
        validateRange(request);
        AltitudeLevel level = toEntity(request);
        level.setId(id);
        mapper.update(level);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (mapper.softDelete(id) == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "高度层不存在: " + id);
    }

    private void validateRange(AltitudeLevelRequest request) {
        if (request.getMaxAltitudeM() <= request.getMinAltitudeM()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "最高高度必须大于最低高度");
        }
    }

    private AltitudeLevel toEntity(AltitudeLevelRequest request) {
        AltitudeLevel level = new AltitudeLevel();
        level.setLevelCode(request.getLevelCode());
        level.setLevelName(request.getLevelName());
        level.setMinAltitudeM(request.getMinAltitudeM());
        level.setMaxAltitudeM(request.getMaxAltitudeM());
        level.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        level.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        level.setDescription(request.getDescription());
        return level;
    }
}
