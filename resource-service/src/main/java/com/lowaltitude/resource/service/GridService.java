package com.lowaltitude.resource.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.resource.domain.Grid;
import com.lowaltitude.resource.dto.GridRequest;
import com.lowaltitude.resource.mapper.GridMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class GridService {
    private static final Set<String> VALID_STATUS = Set.of("ACTIVE", "RISK", "NO_FLY", "DISABLED");
    private final GridMapper mapper;

    public GridService(GridMapper mapper) { this.mapper = mapper; }

    public List<Grid> list(String keyword, String status) { return mapper.list(keyword, status); }

    public Grid get(Long id) {
        Grid grid = mapper.findById(id);
        if (grid == null) throw new BusinessException(ErrorCode.NOT_FOUND, "网格不存在: " + id);
        return grid;
    }

    @Transactional
    public Grid create(GridRequest request) {
        if (mapper.countByCode(request.getGridCode()) > 0) throw new BusinessException(ErrorCode.CONFLICT, "网格编码已存在: " + request.getGridCode());
        Grid grid = toEntity(request);
        mapper.insert(grid);
        return get(grid.getId());
    }

    @Transactional
    public Grid update(Long id, GridRequest request) {
        get(id);
        Grid grid = toEntity(request);
        grid.setId(id);
        mapper.update(grid);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (mapper.softDelete(id) == 0) throw new BusinessException(ErrorCode.NOT_FOUND, "网格不存在: " + id);
    }

    private Grid toEntity(GridRequest request) {
        String status = request.getStatus() == null ? "ACTIVE" : request.getStatus().trim().toUpperCase();
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "网格状态只允许 ACTIVE / RISK / NO_FLY / DISABLED");
        }
        Grid grid = new Grid();
        grid.setGridCode(request.getGridCode());
        grid.setGridName(request.getGridName());
        grid.setRowIndex(request.getRowIndex());
        grid.setColIndex(request.getColIndex());
        grid.setCenterLon(request.getCenterLon());
        grid.setCenterLat(request.getCenterLat());
        grid.setStatus(status);
        grid.setDescription(request.getDescription());
        return grid;
    }
}
