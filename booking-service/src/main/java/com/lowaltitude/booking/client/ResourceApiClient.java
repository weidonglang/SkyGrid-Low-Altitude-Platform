package com.lowaltitude.booking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "resource-service")
public interface ResourceApiClient {
    @GetMapping("/api/resources/route-templates/{id}")
    RemoteApiResponse<ResourceRouteTemplate> getRouteTemplate(@PathVariable("id") Long id);

    @GetMapping("/api/resources/levels/{id}")
    RemoteApiResponse<ResourceAltitudeLevel> getLevel(@PathVariable("id") Long id);

    @GetMapping("/api/resources/levels")
    RemoteApiResponse<List<ResourceAltitudeLevel>> listLevels();

    @GetMapping("/api/resources/time-slots/{id}")
    RemoteApiResponse<ResourceTimeSlot> getTimeSlot(@PathVariable("id") Long id);

    @GetMapping("/api/resources/grids")
    RemoteApiResponse<List<ResourceGrid>> listGrids();
}
