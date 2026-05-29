package com.lowaltitude.resource.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.resource.domain.RouteTemplate;
import com.lowaltitude.resource.dto.RouteTemplateGridRequest;
import com.lowaltitude.resource.dto.RouteTemplateRequest;
import com.lowaltitude.resource.service.RouteTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources/route-templates")
public class RouteTemplateController {
    private final RouteTemplateService service;
    public RouteTemplateController(RouteTemplateService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<RouteTemplate>> list(@RequestParam(name = "keyword", required = false) String keyword,
                                                 @RequestParam(name = "enabled", required = false) Boolean enabled) {
        return ApiResponse.ok(service.list(keyword, enabled));
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteTemplate> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @PostMapping
    public ApiResponse<RouteTemplate> create(@Valid @RequestBody RouteTemplateRequest request) { return ApiResponse.ok("route template created", service.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<RouteTemplate> update(@PathVariable("id") Long id, @Valid @RequestBody RouteTemplateRequest request) { return ApiResponse.ok("route template updated", service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("route template deleted", null);
    }

    @PostMapping("/{routeTemplateId}/grids")
    public ApiResponse<RouteTemplate> addGrid(@PathVariable("routeTemplateId") Long routeTemplateId, @Valid @RequestBody RouteTemplateGridRequest request) {
        return ApiResponse.ok("route grid added", service.addGrid(routeTemplateId, request));
    }

    @PutMapping("/{routeTemplateId}/grids/{routeGridId}")
    public ApiResponse<RouteTemplate> updateGrid(@PathVariable("routeTemplateId") Long routeTemplateId,
                                                 @PathVariable("routeGridId") Long routeGridId,
                                                 @Valid @RequestBody RouteTemplateGridRequest request) {
        return ApiResponse.ok("route grid updated", service.updateGrid(routeTemplateId, routeGridId, request));
    }

    @DeleteMapping("/{routeTemplateId}/grids/{routeGridId}")
    public ApiResponse<RouteTemplate> removeGrid(@PathVariable("routeTemplateId") Long routeTemplateId, @PathVariable("routeGridId") Long routeGridId) {
        return ApiResponse.ok("route grid removed", service.removeGrid(routeTemplateId, routeGridId));
    }
}
