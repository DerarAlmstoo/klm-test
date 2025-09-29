package com.airfranceklm.fasttrack.assignment.service;

import com.airfranceklm.fasttrack.assignment.exception.BusinessRuleViolationException;
import com.airfranceklm.fasttrack.assignment.exception.NotFoundException;
import com.airfranceklm.fasttrack.assignment.model.Holiday;
import com.airfranceklm.fasttrack.assignment.repo.HolidayRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final WorkingDayCalculator workingDayCalculator;

    public HolidayService(HolidayRepository holidayRepository, WorkingDayCalculator workingDayCalculator) {
        this.holidayRepository = holidayRepository;
        this.workingDayCalculator = workingDayCalculator;
    }

    /** LIST (optionally by employee) */
    public List<Holiday> list(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            return holidayRepository.findAll()
                    .stream()
                    .sorted(Comparator.comparing(Holiday::getStartOfHoliday))
                    .toList();
        }
        return holidayRepository.findByEmployeeIdOrderByStartOfHolidayAsc(employeeId);
    }

    /** GET by id */
    public Holiday get(UUID id) {
        return holidayRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Holiday not found: " + id));
    }

    /** CREATE with all business rules */
    @Transactional
    public Holiday create(@Valid Holiday holiday) {
        validateDates(holiday.getStartOfHoliday(), holiday.getEndOfHoliday());
        validateLeadTime(holiday.getStartOfHoliday());
        validateNoOverlap(holiday.getStartOfHoliday(), holiday.getEndOfHoliday(), null);
        validateGapForEmployee(holiday.getEmployeeId(), holiday.getStartOfHoliday(), holiday.getEndOfHoliday(), null);
        return holidayRepository.save(holiday);
    }

    /** UPDATE with all business rules */
    @Transactional
    public Holiday update(UUID id, @Valid Holiday update) {
        Holiday existing = get(id);

        existing.setHolidayLabel(update.getHolidayLabel());
        existing.setEmployeeId(update.getEmployeeId());
        existing.setStartOfHoliday(update.getStartOfHoliday());
        existing.setEndOfHoliday(update.getEndOfHoliday());
        existing.setStatus(update.getStatus());

        validateDates(existing.getStartOfHoliday(), existing.getEndOfHoliday());
        validateLeadTime(existing.getStartOfHoliday());
        validateNoOverlap(existing.getStartOfHoliday(), existing.getEndOfHoliday(), id);
        validateGapForEmployee(existing.getEmployeeId(), existing.getStartOfHoliday(), existing.getEndOfHoliday(), id);

        return holidayRepository.save(existing);
    }

    /** DELETE (cancel rule applies) */
    @Transactional
    public void delete(UUID id) {
        Holiday existing = get(id);
        validateCancelable(existing.getStartOfHoliday());
        holidayRepository.deleteById(id);
    }

    // ---------------- Business Rules ----------------

    /** end must be strictly after start */
    private void validateDates(OffsetDateTime start, OffsetDateTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new BusinessRuleViolationException("endOfHoliday must be after startOfHoliday");
        }
    }

    /** must plan ≥ 5 working days before start */
    private void validateLeadTime(OffsetDateTime start) {
        LocalDate todayUtc = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        LocalDate startDate = start.toLocalDate();
        int wd = workingDayCalculator.workingDaysBetween(todayUtc, startDate);
        if (wd < 5) {
            throw new BusinessRuleViolationException(
                    "Holiday must be planned at least 5 working days before the start date");
        }
    }

    /** must cancel ≥ 5 working days before start */
    private void validateCancelable(OffsetDateTime start) {
        LocalDate todayUtc = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        LocalDate startDate = start.toLocalDate();
        int wd = workingDayCalculator.workingDaysBetween(todayUtc, startDate);
        if (wd < 5) {
            throw new BusinessRuleViolationException(
                    "Holiday must be cancelled at least 5 working days before the start date");
        }
    }

    /** no overlap across any employees; ignore current id when updating */
    private void validateNoOverlap(OffsetDateTime start, OffsetDateTime end, UUID ignoreId) {
        List<Holiday> overlaps = holidayRepository.findOverlapping(start, end);
        boolean conflict = overlaps.stream()
                .anyMatch(h -> ignoreId == null || !h.getHolidayId().equals(ignoreId));
        if (conflict) {
            throw new BusinessRuleViolationException(
                    "Holidays must not overlap (between any crew members)");
        }
    }

    /** gap ≥ 3 working days between holidays for the same employee (both sides) */
    private void validateGapForEmployee(
            String employeeId, OffsetDateTime start, OffsetDateTime end, UUID ignoreId) {

        List<Holiday> existing = holidayRepository.findByEmployeeIdOrderByStartOfHolidayAsc(employeeId);
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        for (Holiday h : existing) {
            if (ignoreId != null && h.getHolidayId().equals(ignoreId)) continue;

            LocalDate otherStart = h.getStartOfHoliday().toLocalDate();
            LocalDate otherEnd = h.getEndOfHoliday().toLocalDate();

            // Safety: same-employee overlaps (global overlap check already covers this)
            boolean overlaps = start.isBefore(h.getEndOfHoliday()) && h.getStartOfHoliday().isBefore(end);
            if (overlaps) {
                throw new BusinessRuleViolationException("Holidays must not overlap for the same employee");
            }

            // If the other holiday ends before this starts -> gap from otherEnd to this start
            if (!otherEnd.isAfter(startDate)) {
                int gap = workingDayCalculator.workingDaysBetween(otherEnd, startDate);
                if (gap < 3) {
                    throw new BusinessRuleViolationException(
                            "There should be a gap of at least 3 working days between holidays for the same employee");
                }
            }

            // If this ends before the other starts -> gap from this end to otherStart
            if (!endDate.isAfter(otherStart)) {
                int gap = workingDayCalculator.workingDaysBetween(endDate, otherStart);
                if (gap < 3) {
                    throw new BusinessRuleViolationException(
                            "There should be a gap of at least 3 working days between holidays for the same employee");
                }
            }
        }
    }
}
