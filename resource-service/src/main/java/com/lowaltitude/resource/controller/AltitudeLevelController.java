package com.lowaltitude.resource.controller;

import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.resource.domain.AltitudeLevel;
import com.lowaltitude.resource.dto.AltitudeLevelRequest;
import com.lowaltitude.resource.service.AltitudeLevelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources/levels")
public class AltitudeLevelController {
    private final AltitudeLevelService service;
    public AltitudeLevelController(AltitudeLevelService service) { this.service = service; }

    @GetMapping
    public ApiResponse<List<AltitudeLevel>> list(@RequestParam(name = "keyword", required = false) String keyword,
                                                 @RequestParam(name = "enabled", required = false) Boolean enabled) {
        return ApiResponse.ok(service.list(keyword, enabled));
    }

    @GetMapping("/{id}")
    public ApiResponse<AltitudeLevel> get(@PathVariable("id") Long id) { return ApiResponse.ok(service.get(id)); }

    @PostMapping
    public ApiResponse<AltitudeLevel> create(@Valid @RequestBody AltitudeLevelRequest request) { return ApiResponse.ok("level created", service.create(request)); }

    @PutMapping("/{id}")
    public ApiResponse<AltitudeLevel> update(@PathVariable("id") Long id, @Valid @RequestBody AltitudeLevelRequest request) { return ApiResponse.ok("level updated", service.update(id, request)); }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ApiResponse.ok("level deleted", null);
    }
}
