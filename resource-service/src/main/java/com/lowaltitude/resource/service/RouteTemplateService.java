package com.lowaltitude.resource.service;

import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import com.lowaltitude.resource.domain.RouteTemplate;
import com.lowaltitude.resource.domain.RouteTemplateGrid;
import com.lowaltitude.resource.dto.RouteTemplateGridRequest;
import com.lowaltitude.resource.dto.RouteTemplateRequest;
import com.lowaltitude.resource.mapper.GridMapper;
import com.lowaltitude.resource.mapper.RouteTemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteTemplateService {
    private final RouteTemplateMapper routeMapper;
    private final GridMapper gridMapper;

    public RouteTemplateService(RouteTemplateMapper routeMapper, GridMapper gridMapper) {
        this.routeMapper = routeMapper;
        this.gridMapper = gridMapper;
    }

    public List<RouteTemplate> list(String keyword, Boolean enabled) { return routeMapper.list(keyword, enabled); }

    public RouteTemplate get(Long id) {
        RouteTemplate route = routeMapper.findById(id);
        if (route == null) throw new BusinessException(ErrorCode.NOT_FOUND, "航线模板不存在: " + id);
        route.setGrids(routeMapper.listRouteGrids(id));
        return route;
    }

    @Transactional
    public RouteTemplate create(RouteTemplateRequest request) {
        if (routeMapper.countByCode(request.getRouteCode()) > 0) throw new BusinessException(ErrorCode.CONFLICT, "航线模板编码已存在: " + request.getRouteCode());
        RouteTemplate route = toEntity(request);
        routeMapper.insert(route);
        return get(route.getId());
    }

    @Transactional
    public RouteTemplate update(Long id, RouteTemplateRequest request) {
        get(id);
        RouteTemplate route = toEntity(request);
        route.setId(id);
        routeMapper.update(route);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        get(id);
        routeMapper.deleteAllRouteGrids(id);
        routeMapper.softDelete(id);
    }

    @Transactional
    public RouteTemplate addGrid(Long routeTemplateId, RouteTemplateGridRequest request) {
        get(routeTemplateId);
        if (gridMapper.findById(request.getGridId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "网格不存在: " + request.getGridId());
        }
        RouteTemplateGrid routeGrid = new RouteTemplateGrid();
        routeGrid.setRouteTemplateId(routeTemplateId);
        routeGrid.setGridId(request.getGridId());
        routeGrid.setSequenceNo(request.getSequenceNo());
        routeGrid.setPlannedDurationMinutes(request.getPlannedDurationMinutes() == null ? 5 : request.getPlannedDurationMinutes());
        routeMapper.insertRouteGrid(routeGrid);
        return get(routeTemplateId);
    }

    @Transactional
    public RouteTemplate updateGrid(Long routeTemplateId, Long routeGridId, RouteTemplateGridRequest request) {
        get(routeTemplateId);
        if (gridMapper.findById(request.getGridId()) == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "网格不存在: " + request.getGridId());
        }
        RouteTemplateGrid routeGrid = new RouteTemplateGrid();
        routeGrid.setId(routeGridId);
        routeGrid.setRouteTemplateId(routeTemplateId);
        routeGrid.setGridId(request.getGridId());
        routeGrid.setSequenceNo(request.getSequenceNo());
        routeGrid.setPlannedDurationMinutes(request.getPlannedDurationMinutes() == null ? 5 : request.getPlannedDurationMinutes());
        routeMapper.updateRouteGrid(routeGrid);
        return get(routeTemplateId);
    }

    @Transactional
    public RouteTemplate removeGrid(Long routeTemplateId, Long routeGridId) {
        get(routeTemplateId);
        routeMapper.deleteRouteGrid(routeTemplateId, routeGridId);
        return get(routeTemplateId);
    }

    private RouteTemplate toEntity(RouteTemplateRequest request) {
        RouteTemplate route = new RouteTemplate();
        route.setRouteCode(request.getRouteCode());
        route.setRouteName(request.getRouteName());
        route.setDescription(request.getDescription());
        route.setEnabled(request.getEnabled() == null ? Boolean.TRUE : request.getEnabled());
        route.setCreatedBy(request.getCreatedBy());
        return route;
    }
}
