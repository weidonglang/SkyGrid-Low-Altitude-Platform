package com.lowaltitude.booking.service;

import com.lowaltitude.booking.client.*;
import com.lowaltitude.booking.domain.BookingFlowRecord;
import com.lowaltitude.booking.domain.BookingRecord;
import com.lowaltitude.booking.domain.ConflictRecord;
import com.lowaltitude.booking.domain.ResourceOccupancy;
import com.lowaltitude.booking.dto.*;
import com.lowaltitude.booking.mapper.BookingMapper;
import com.lowaltitude.common.api.ErrorCode;
import com.lowaltitude.common.exception.BusinessException;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookingService {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String OCCUPIED = "OCCUPIED";

    private static final String GRID_ACTIVE = "ACTIVE";
    private static final String GRID_RISK = "RISK";
    private static final String GRID_NO_FLY = "NO_FLY";

    private static final String TYPE_HARD = "HARD";
    private static final String TYPE_RISK = "RISK";
    private static final String LEVEL_BLOCKING = "BLOCKING";
    private static final String LEVEL_RISK = "RISK";
    private static final String CONFLICT_ACTIVE = "ACTIVE";

    private static final int ALTITUDE_SAFE_INTERVAL_M = 20;

    private final BookingMapper mapper;
    private final ResourceApiClient resourceApiClient;
    private final OutboxService outboxService;

    public BookingService(BookingMapper mapper, ResourceApiClient resourceApiClient, OutboxService outboxService) {
        this.mapper = mapper;
        this.resourceApiClient = resourceApiClient;
        this.outboxService = outboxService;
    }

    public List<BookingRecord> list(String status, Long applicantUserId, LocalDate bookingDate, String keyword) {
        return mapper.list(normalizeStatus(status), applicantUserId, bookingDate, trimToNull(keyword));
    }

    public BookingRecord get(Long id) {
        BookingRecord booking = mapper.findById(id);
        if (booking == null) throw new BusinessException(ErrorCode.NOT_FOUND, "预约申请不存在: " + id);
        fillChildren(booking);
        return booking;
    }

    public List<BookingFlowRecord> listFlows(Long bookingId) {
        getWithoutChildren(bookingId);
        return mapper.listFlows(bookingId);
    }

    public List<ResourceOccupancy> listOccupancies(Long bookingId) {
        getWithoutChildren(bookingId);
        return mapper.listOccupancies(bookingId);
    }

    public List<ConflictRecord> listConflicts(Long bookingId, String conflictType, String status) {
        if (bookingId != null) getWithoutChildren(bookingId);
        return mapper.listConflicts(bookingId, trimToNull(conflictType), trimToNull(status));
    }

    public BookingPreCheckResult preCheck(BookingSubmitRequest request) {
        ResourceRouteTemplate route = requireRouteTemplate(request.getRouteTemplateId());
        requireEnabledLevel(request.getLevelId());
        List<Long> timeSlotIds = normalizeTimeSlotIds(request.getTimeSlotIds());
        for (Long timeSlotId : timeSlotIds) {
            requireEnabledTimeSlot(timeSlotId);
        }
        return checkCandidate(route, request.getLevelId(), request.getBookingDate(), timeSlotIds, null);
    }

    @Transactional
    public BookingRecord submit(BookingSubmitRequest request, String currentUserName) {
        ResourceRouteTemplate route = requireRouteTemplate(request.getRouteTemplateId());
        requireEnabledLevel(request.getLevelId());
        List<Long> timeSlotIds = normalizeTimeSlotIds(request.getTimeSlotIds());
        for (Long timeSlotId : timeSlotIds) {
            requireEnabledTimeSlot(timeSlotId);
        }

        BookingPreCheckResult preCheck = checkCandidate(route, request.getLevelId(), request.getBookingDate(), timeSlotIds, null);
        if (!preCheck.isCanSubmit()) {
            throw new BusinessException(ErrorCode.CONFLICT, "提交前冲突检测未通过: " + preCheck.getSummary());
        }

        BookingRecord booking = new BookingRecord();
        booking.setBookingNo(generateBookingNo());
        booking.setTaskName(request.getTaskName().trim());
        booking.setOrgId(request.getOrgId());
        booking.setApplicantUserId(request.getApplicantUserId() == null ? 1L : request.getApplicantUserId());
        booking.setApplicantName(firstNonBlank(request.getApplicantName(), currentUserName, "demo-applicant"));
        booking.setRouteTemplateId(route.getId());
        booking.setRouteTemplateName(route.getRouteName());
        booking.setLevelId(request.getLevelId());
        booking.setBookingDate(request.getBookingDate());
        booking.setStatus(STATUS_PENDING);
        booking.setApplyReason(request.getApplyReason());
        booking.setDescription(request.getDescription());
        mapper.insert(booking);

        for (Long timeSlotId : timeSlotIds) {
            mapper.insertBookingTimeSlot(booking.getId(), timeSlotId);
        }
        insertFlow(booking.getId(), "SUBMIT", null, STATUS_PENDING,
                booking.getApplicantUserId(), booking.getApplicantName(), "提交低空巡检预约申请");
        persistConflicts(booking, preCheck.getConflicts());
        return get(booking.getId());
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public BookingRecord approve(Long id, BookingApproveRequest request, String currentUserName) {
        BookingRecord booking = getWithoutChildren(id);
        if (!STATUS_PENDING.equals(booking.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "只有 PENDING 状态的申请可以审批通过，当前状态: " + booking.getStatus());
        }
        ResourceRouteTemplate route = requireRouteTemplate(booking.getRouteTemplateId());
        if (route.getGrids() == null || route.getGrids().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "航线模板没有配置经过网格，不能生成资源占用");
        }
        List<Long> timeSlotIds = mapper.listTimeSlotIds(id);
        if (timeSlotIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "预约申请没有时间片，不能生成资源占用");
        }

        BookingPreCheckResult approveCheck = checkCandidate(route, booking.getLevelId(), booking.getBookingDate(), timeSlotIds, booking.getId());
        if (!approveCheck.isCanApprove()) {
            persistConflicts(booking, approveCheck.getConflicts());
            throw new BusinessException(ErrorCode.CONFLICT, "审批前冲突检测未通过: " + approveCheck.getSummary());
        }
        persistConflicts(booking, approveCheck.getConflicts());

        String riskSuffix = approveCheck.getRiskCount() > 0 ? "；已记录 " + approveCheck.getRiskCount() + " 条风险提示" : "";
        String comment = firstNonBlank(request.getComment(), "审批通过") + riskSuffix;
        int updated = mapper.approve(id, STATUS_PENDING, STATUS_APPROVED, comment);
        if (updated == 0) throw new BusinessException(ErrorCode.CONFLICT, "审批状态已变化，请刷新后重试");

        int occupancyCount = 0;
        for (ResourceRouteTemplateGrid grid : route.getGrids()) {
            for (Long timeSlotId : timeSlotIds) {
                ResourceOccupancy occupancy = new ResourceOccupancy();
                occupancy.setBookingId(booking.getId());
                occupancy.setBookingNo(booking.getBookingNo());
                occupancy.setRouteTemplateId(booking.getRouteTemplateId());
                occupancy.setGridId(grid.getGridId());
                occupancy.setGridCode(grid.getGridCode());
                occupancy.setGridName(grid.getGridName());
                occupancy.setLevelId(booking.getLevelId());
                occupancy.setTimeSlotId(timeSlotId);
                occupancy.setBookingDate(booking.getBookingDate());
                occupancy.setStatus(OCCUPIED);
                mapper.insertOccupancy(occupancy);
                occupancyCount++;
            }
        }
        String operatorName = firstNonBlank(request.getOperatorName(), currentUserName, "approver");
        insertFlow(id, "APPROVE", STATUS_PENDING, STATUS_APPROVED,
                operatorId(request.getOperatorUserId()), operatorName, comment);
        BookingRecord eventBooking = mapper.findById(id);
        eventBooking.setTimeSlotIds(timeSlotIds);
        outboxService.createBookingApprovedEvent(eventBooking, timeSlotIds, operatorName, comment, occupancyCount);
        return get(id);
    }

    @Transactional
    public BookingRecord reject(Long id, BookingRejectRequest request, String currentUserName) {
        BookingRecord booking = getWithoutChildren(id);
        if (!STATUS_PENDING.equals(booking.getStatus())) {
            throw new BusinessException(ErrorCode.CONFLICT, "只有 PENDING 状态的申请可以驳回，当前状态: " + booking.getStatus());
        }
        int updated = mapper.reject(id, STATUS_PENDING, STATUS_REJECTED, request.getRejectReason());
        if (updated == 0) throw new BusinessException(ErrorCode.CONFLICT, "审批状态已变化，请刷新后重试");
        mapper.resolveConflicts(id);
        insertFlow(id, "REJECT", STATUS_PENDING, STATUS_REJECTED,
                operatorId(request.getOperatorUserId()), firstNonBlank(request.getOperatorName(), currentUserName, "approver"), request.getRejectReason());
        return get(id);
    }

    @Transactional
    public BookingRecord cancel(Long id, BookingCancelRequest request, String currentUserName) {
        BookingRecord booking = getWithoutChildren(id);
        String from = booking.getStatus();
        if (!STATUS_PENDING.equals(from) && !STATUS_APPROVED.equals(from)) {
            throw new BusinessException(ErrorCode.CONFLICT, "只有 PENDING 或 APPROVED 状态的申请可以取消，当前状态: " + from);
        }
        String reason = firstNonBlank(request.getCancelReason(), "取消预约申请");
        int updated = mapper.cancel(id, from, STATUS_CANCELLED, reason);
        if (updated == 0) throw new BusinessException(ErrorCode.CONFLICT, "预约状态已变化，请刷新后重试");
        if (STATUS_APPROVED.equals(from)) {
            mapper.releaseOccupancies(id);
        }
        mapper.resolveConflicts(id);
        String operatorName = firstNonBlank(request.getOperatorName(), currentUserName, "operator");
        insertFlow(id, "CANCEL", from, STATUS_CANCELLED,
                operatorId(request.getOperatorUserId()), operatorName, reason);
        if (STATUS_APPROVED.equals(from)) {
            BookingRecord eventBooking = mapper.findById(id);
            outboxService.createBookingCancelledEvent(eventBooking, from, operatorName, reason);
        }
        return get(id);
    }

    private BookingPreCheckResult checkCandidate(ResourceRouteTemplate route, Long levelId, LocalDate bookingDate,
                                                 List<Long> timeSlotIds, Long excludeBookingId) {
        if (route.getGrids() == null || route.getGrids().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "航线模板没有配置经过网格，不能检测冲突");
        }
        List<ConflictCheckItem> conflicts = new ArrayList<>();
        List<ResourceOccupancy> active = mapper.listActiveOccupanciesForDate(bookingDate, excludeBookingId);
        Map<Long, ResourceGrid> gridMap = listAllGrids().stream()
                .collect(Collectors.toMap(ResourceGrid::getId, Function.identity(), (a, b) -> a));
        Map<Long, ResourceAltitudeLevel> levelMap = listAllLevels().stream()
                .collect(Collectors.toMap(ResourceAltitudeLevel::getId, Function.identity(), (a, b) -> a));

        int candidateCount = 0;
        for (ResourceRouteTemplateGrid grid : route.getGrids()) {
            for (Long timeSlotId : timeSlotIds) {
                candidateCount++;
                if (GRID_NO_FLY.equalsIgnoreCase(nullToEmpty(grid.getGridStatus()))) {
                    conflicts.add(item(TYPE_HARD, LEVEL_BLOCKING, "NO_FLY_GRID", grid, levelId, timeSlotId, bookingDate,
                            null, null, "航线经过禁飞网格 " + grid.getGridCode() + "，不允许提交或审批"));
                }
                if (GRID_RISK.equalsIgnoreCase(nullToEmpty(grid.getGridStatus()))) {
                    conflicts.add(item(TYPE_RISK, LEVEL_RISK, "RISK_GRID", grid, levelId, timeSlotId, bookingDate,
                            null, null, "航线经过风险网格 " + grid.getGridCode() + "，建议审批员复核"));
                }
                for (ResourceOccupancy occ : active) {
                    if (!Objects.equals(occ.getTimeSlotId(), timeSlotId)) continue;
                    if (Objects.equals(occ.getGridId(), grid.getGridId()) && Objects.equals(occ.getLevelId(), levelId)) {
                        conflicts.add(item(TYPE_HARD, LEVEL_BLOCKING, "SAME_GRID_LEVEL_TIME", grid, levelId, timeSlotId, bookingDate,
                                occ.getBookingId(), occ.getBookingNo(),
                                "同一网格、同一高度层、同一时间片已被申请 " + occ.getBookingNo() + " 占用"));
                    } else if (Objects.equals(occ.getGridId(), grid.getGridId()) && isAltitudeTooClose(levelId, occ.getLevelId(), levelMap)) {
                        conflicts.add(item(TYPE_RISK, LEVEL_RISK, "ALTITUDE_SAFETY_INTERVAL", grid, levelId, timeSlotId, bookingDate,
                                occ.getBookingId(), occ.getBookingNo(),
                                "同一网格相邻高度层安全间隔不足，相关申请: " + occ.getBookingNo()));
                    } else if (Objects.equals(occ.getLevelId(), levelId) && isAdjacentGrid(grid, gridMap.get(occ.getGridId()))) {
                        conflicts.add(item(TYPE_RISK, LEVEL_RISK, "ADJACENT_GRID_OCCUPIED", grid, levelId, timeSlotId, bookingDate,
                                occ.getBookingId(), occ.getBookingNo(),
                                "相邻网格同高度同时间已有占用，相关申请: " + occ.getBookingNo()));
                    }
                }
            }
        }
        return buildCheckResult(candidateCount, conflicts);
    }

    private boolean isAdjacentGrid(ResourceRouteTemplateGrid a, ResourceGrid b) {
        if (a == null || b == null || a.getRowIndex() == null || a.getColIndex() == null || b.getRowIndex() == null || b.getColIndex() == null) return false;
        int distance = Math.abs(a.getRowIndex() - b.getRowIndex()) + Math.abs(a.getColIndex() - b.getColIndex());
        return distance == 1;
    }

    private boolean isAltitudeTooClose(Long requestedLevelId, Long occupiedLevelId, Map<Long, ResourceAltitudeLevel> levelMap) {
        if (Objects.equals(requestedLevelId, occupiedLevelId)) return false;
        ResourceAltitudeLevel a = levelMap.get(requestedLevelId);
        ResourceAltitudeLevel b = levelMap.get(occupiedLevelId);
        if (a == null || b == null || a.getMinAltitudeM() == null || a.getMaxAltitudeM() == null || b.getMinAltitudeM() == null || b.getMaxAltitudeM() == null) return false;
        int distance;
        if (a.getMaxAltitudeM() <= b.getMinAltitudeM()) {
            distance = b.getMinAltitudeM() - a.getMaxAltitudeM();
        } else if (b.getMaxAltitudeM() <= a.getMinAltitudeM()) {
            distance = a.getMinAltitudeM() - b.getMaxAltitudeM();
        } else {
            distance = 0;
        }
        return distance < ALTITUDE_SAFE_INTERVAL_M;
    }

    private BookingPreCheckResult buildCheckResult(int candidateCount, List<ConflictCheckItem> conflicts) {
        BookingPreCheckResult result = new BookingPreCheckResult();
        int blockingCount = (int) conflicts.stream().filter(ConflictCheckItem::isBlocking).count();
        int riskCount = conflicts.size() - blockingCount;
        result.setCandidateOccupancyCount(candidateCount);
        result.setBlockingCount(blockingCount);
        result.setRiskCount(riskCount);
        result.setCanSubmit(blockingCount == 0);
        result.setCanApprove(blockingCount == 0);
        result.setPassed(blockingCount == 0);
        result.setConflicts(conflicts);
        if (blockingCount > 0) {
            result.setSummary("存在 " + blockingCount + " 条阻断冲突，不能继续");
        } else if (riskCount > 0) {
            result.setSummary("不存在硬冲突，但存在 " + riskCount + " 条风险提示，需人工复核");
        } else {
            result.setSummary("未发现冲突，可继续申请或审批");
        }
        return result;
    }

    private ConflictCheckItem item(String type, String level, String ruleCode, ResourceRouteTemplateGrid grid,
                                   Long levelId, Long timeSlotId, LocalDate bookingDate,
                                   Long relatedBookingId, String relatedBookingNo, String message) {
        return new ConflictCheckItem(type, level, ruleCode, grid.getGridId(), grid.getGridCode(), grid.getGridName(),
                levelId, timeSlotId, bookingDate, relatedBookingId, relatedBookingNo, message);
    }

    private void persistConflicts(BookingRecord booking, List<ConflictCheckItem> items) {
        mapper.deleteActiveConflicts(booking.getId());
        if (items == null || items.isEmpty()) return;
        for (ConflictCheckItem item : items) {
            ConflictRecord record = new ConflictRecord();
            record.setBookingId(booking.getId());
            record.setBookingNo(booking.getBookingNo());
            record.setConflictType(item.getConflictType());
            record.setConflictLevel(item.getConflictLevel());
            record.setRuleCode(item.getRuleCode());
            record.setGridId(item.getGridId());
            record.setGridCode(item.getGridCode());
            record.setGridName(item.getGridName());
            record.setLevelId(item.getLevelId());
            record.setTimeSlotId(item.getTimeSlotId());
            record.setBookingDate(item.getBookingDate());
            record.setRelatedBookingId(item.getRelatedBookingId());
            record.setRelatedBookingNo(item.getRelatedBookingNo());
            record.setMessage(item.getMessage());
            record.setStatus(CONFLICT_ACTIVE);
            mapper.insertConflict(record);
        }
    }

    private BookingRecord getWithoutChildren(Long id) {
        BookingRecord booking = mapper.findById(id);
        if (booking == null) throw new BusinessException(ErrorCode.NOT_FOUND, "预约申请不存在: " + id);
        booking.setTimeSlotIds(mapper.listTimeSlotIds(id));
        return booking;
    }

    private void fillChildren(BookingRecord booking) {
        booking.setTimeSlotIds(mapper.listTimeSlotIds(booking.getId()));
        booking.setFlows(mapper.listFlows(booking.getId()));
        booking.setOccupancies(mapper.listOccupancies(booking.getId()));
    }

    private void insertFlow(Long bookingId, String action, String fromStatus, String toStatus,
                            Long operatorUserId, String operatorName, String comment) {
        BookingFlowRecord flow = new BookingFlowRecord();
        flow.setBookingId(bookingId);
        flow.setAction(action);
        flow.setFromStatus(fromStatus);
        flow.setToStatus(toStatus);
        flow.setOperatorUserId(operatorUserId);
        flow.setOperatorName(operatorName);
        flow.setComment(comment);
        mapper.insertFlow(flow);
    }

    private ResourceRouteTemplate requireRouteTemplate(Long routeTemplateId) {
        try {
            RemoteApiResponse<ResourceRouteTemplate> response = resourceApiClient.getRouteTemplate(routeTemplateId);
            ResourceRouteTemplate route = response == null ? null : response.getData();
            if (route == null) throw new BusinessException(ErrorCode.NOT_FOUND, "航线模板不存在: " + routeTemplateId);
            if (Boolean.FALSE.equals(route.getEnabled())) throw new BusinessException(ErrorCode.BAD_REQUEST, "航线模板已禁用: " + routeTemplateId);
            return route;
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "航线模板不存在: " + routeTemplateId);
        }
    }

    private void requireEnabledLevel(Long levelId) {
        try {
            RemoteApiResponse<ResourceAltitudeLevel> response = resourceApiClient.getLevel(levelId);
            ResourceAltitudeLevel level = response == null ? null : response.getData();
            if (level == null) throw new BusinessException(ErrorCode.NOT_FOUND, "高度层不存在: " + levelId);
            if (Boolean.FALSE.equals(level.getEnabled())) throw new BusinessException(ErrorCode.BAD_REQUEST, "高度层已禁用: " + levelId);
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "高度层不存在: " + levelId);
        }
    }

    private void requireEnabledTimeSlot(Long timeSlotId) {
        try {
            RemoteApiResponse<ResourceTimeSlot> response = resourceApiClient.getTimeSlot(timeSlotId);
            ResourceTimeSlot slot = response == null ? null : response.getData();
            if (slot == null) throw new BusinessException(ErrorCode.NOT_FOUND, "时间片不存在: " + timeSlotId);
            if (Boolean.FALSE.equals(slot.getEnabled())) throw new BusinessException(ErrorCode.BAD_REQUEST, "时间片已禁用: " + timeSlotId);
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "时间片不存在: " + timeSlotId);
        }
    }

    private List<ResourceGrid> listAllGrids() {
        RemoteApiResponse<List<ResourceGrid>> response = resourceApiClient.listGrids();
        return response == null || response.getData() == null ? List.of() : response.getData();
    }

    private List<ResourceAltitudeLevel> listAllLevels() {
        RemoteApiResponse<List<ResourceAltitudeLevel>> response = resourceApiClient.listLevels();
        return response == null || response.getData() == null ? List.of() : response.getData();
    }

    private List<Long> normalizeTimeSlotIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) throw new BusinessException(ErrorCode.BAD_REQUEST, "至少选择一个时间片");
        Set<Long> unique = new LinkedHashSet<>(ids);
        if (unique.contains(null)) throw new BusinessException(ErrorCode.BAD_REQUEST, "时间片 ID 不能为空");
        return List.copyOf(unique);
    }

    private String generateBookingNo() {
        String prefix = "BK" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-";
        for (int i = 0; i < 5; i++) {
            String no = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
            if (mapper.countByBookingNo(no) == 0) return no;
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "预约编号生成失败，请重试");
    }

    private String normalizeStatus(String status) {
        String s = trimToNull(status);
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }

    private Long operatorId(Long operatorUserId) {
        return operatorUserId == null ? 1L : operatorUserId;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return null;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
