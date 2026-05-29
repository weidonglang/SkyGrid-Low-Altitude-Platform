package com.lowaltitude.booking.controller;

import com.lowaltitude.booking.domain.BookingFlowRecord;
import com.lowaltitude.booking.domain.BookingRecord;
import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.domain.ResourceOccupancy;
import com.lowaltitude.booking.dto.*;
import com.lowaltitude.booking.service.BookingService;
import com.lowaltitude.common.api.ApiResponse;
import com.lowaltitude.common.web.RequestHeaderNames;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<BookingRecord>> list(@RequestParam(name = "status", required = false) String status,
                                                 @RequestParam(name = "applicantUserId", required = false) Long applicantUserId,
                                                 @RequestParam(name = "bookingDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                                                 @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.ok(service.list(status, applicantUserId, bookingDate, keyword));
    }

    @PostMapping("/pre-check")
    public ApiResponse<BookingPreCheckResult> preCheck(@Valid @RequestBody BookingSubmitRequest request) {
        return ApiResponse.ok("pre-check finished", service.preCheck(request));
    }

    @GetMapping("/conflicts")
    public ApiResponse<List<ConflictRecord>> conflicts(@RequestParam(name = "bookingId", required = false) Long bookingId,
                                                       @RequestParam(name = "conflictType", required = false) String conflictType,
                                                       @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(service.listConflicts(bookingId, conflictType, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingRecord> get(@PathVariable("id") Long id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping
    public ApiResponse<BookingRecord> submit(@Valid @RequestBody BookingSubmitRequest request,
                                             @RequestHeader(name = RequestHeaderNames.USER_NAME, required = false) String currentUserName) {
        return ApiResponse.ok("booking submitted", service.submit(request, currentUserName));
    }

    @PostMapping("/{id}/approve")
    public ApiResponse<BookingRecord> approve(@PathVariable("id") Long id,
                                              @RequestBody(required = false) BookingApproveRequest request,
                                              @RequestHeader(name = RequestHeaderNames.USER_NAME, required = false) String currentUserName) {
        return ApiResponse.ok("booking approved", service.approve(id, request == null ? new BookingApproveRequest() : request, currentUserName));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<BookingRecord> reject(@PathVariable("id") Long id,
                                             @Valid @RequestBody BookingRejectRequest request,
                                             @RequestHeader(name = RequestHeaderNames.USER_NAME, required = false) String currentUserName) {
        return ApiResponse.ok("booking rejected", service.reject(id, request, currentUserName));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookingRecord> cancel(@PathVariable("id") Long id,
                                             @RequestBody(required = false) BookingCancelRequest request,
                                             @RequestHeader(name = RequestHeaderNames.USER_NAME, required = false) String currentUserName) {
        return ApiResponse.ok("booking cancelled", service.cancel(id, request == null ? new BookingCancelRequest() : request, currentUserName));
    }

    @GetMapping("/{id}/flows")
    public ApiResponse<List<BookingFlowRecord>> flows(@PathVariable("id") Long id) {
        return ApiResponse.ok(service.listFlows(id));
    }

    @GetMapping("/{id}/occupancies")
    public ApiResponse<List<ResourceOccupancy>> occupancies(@PathVariable("id") Long id) {
        return ApiResponse.ok(service.listOccupancies(id));
    }

    @GetMapping("/{id}/conflicts")
    public ApiResponse<List<ConflictRecord>> bookingConflicts(@PathVariable("id") Long id) {
        return ApiResponse.ok(service.listConflicts(id, null, null));
    }
}
