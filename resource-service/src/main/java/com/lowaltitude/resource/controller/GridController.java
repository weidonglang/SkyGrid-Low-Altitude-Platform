package com.lowaltitude.resource.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.resource.domain.Grid;
import com.lowaltitude.resource.dto.GridRequest;
import com.lowaltitude.resource.service.GridService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources/grids")
public class GridController {
    private final GridService service;
    public GridController(GridService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<Grid>> list(@RequestParam(name = "keyword", required = false) String keyword,
                                        @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(service.list(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<Grid> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @PostMapping
    public ApiResponse<Grid> create(@Valid @RequestBody GridRequest request) { return ApiResponse.ok("grid created", service.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<Grid> update(@PathVariable("id") Long id, @Valid @RequestBody GridRequest request) { return ApiResponse.ok("grid updated", service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("grid deleted", null);
    }
}
