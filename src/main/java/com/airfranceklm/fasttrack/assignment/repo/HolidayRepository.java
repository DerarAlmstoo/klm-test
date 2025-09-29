package com.airfranceklm.fasttrack.assignment.repo;


import com.airfranceklm.fasttrack.assignment.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    List<Holiday> findByEmployeeIdOrderByStartOfHolidayAsc(String employeeId);


    @Query("select h from Holiday h where h.startOfHoliday < :end and h.endOfHoliday > :start")
    List<Holiday> findOverlapping(@Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);


    @Query("select h from Holiday h where h.employeeId = :employeeId and h.startOfHoliday < :end and h.endOfHoliday > :start")
    List<Holiday> findOverlappingForEmployee(@Param("employeeId") String employeeId, @Param("start") OffsetDateTime start, @Param("end") OffsetDateTime end);
}