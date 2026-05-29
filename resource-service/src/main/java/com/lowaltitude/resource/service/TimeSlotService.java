package com.lowaltitude.resource.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.resource.domain.TimeSlot;
import com.lowaltitude.resource.dto.TimeSlotRequest;
import com.lowaltitude.resource.mapper.TimeSlotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TimeSlotService {
    private final TimeSlotMapper mapper;

    public TimeSlotService(TimeSlotMapper mapper) { this.mapper = mapper; }

    public List<TimeSlot> list(String keyword, Boolean enabled) { return mapper.list(keyword, enabled); }

    public TimeSlot get(Long id) {
        TimeSlot slot = mapper.findById(id);
        if (slot == null) throw new BusinessException(ErrorCode.NOT_FOUND, "时间片不存在: " + id);
        return slot;
    }

    @Transactional
    public TimeSlot create(TimeSlotRequest request) {
        validateRange(request);
        if (mapper.countByCode(request.getSlotCode()) > 0) throw new BusinessException(ErrorCode.CONFLICT, "时间片编码已存在: " + request.getSlotCode());
        TimeSlot slot = toEntity(request);
        mapper.insert(slot);
        return get(slot.getId());
    }

    @Transactional
    public TimeSlot update(Long id, TimeSlotRequest request) {
        get(id);
        validateRange(request);
        TimeSlot slot = toEntity(request);
        slot.setId(id);
        mapper.update(slot);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (mapper.softDelete(id) == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "时间片不存在: " + id);
    }

    private void validateRange(TimeSlotRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "结束时间必须晚于开始时间；跨天时间片请拆分建模");
        }
    }

    private TimeSlot toEntity(TimeSlotRequest request) {
        TimeSlot slot = new TimeSlot();
        slot.setSlotCode(request.getSlotCode());
        slot.setSlotName(request.getSlotName());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        slot.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        slot.setDescription(request.getDescription());
        return slot;
    }
}
