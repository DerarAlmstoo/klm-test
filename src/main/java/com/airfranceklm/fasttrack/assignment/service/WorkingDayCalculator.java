package com.airfranceklm.fasttrack.assignment.service;


import org.springframework.stereotype.Component;


import java.time.DayOfWeek;
import java.time.LocalDate;


@Component
public class WorkingDayCalculator {


    public int workingDaysBetween(LocalDate startInclusive, LocalDate endExclusive) {
        if (endExclusive.isBefore(startInclusive)) return -workingDaysBetween(endExclusive, startInclusive);
        int days = 0;
        LocalDate d = startInclusive;
        while (d.isBefore(endExclusive)) {
            if (isWorkingDay(d)) days++;
            d = d.plusDays(1);
        }
        return days;
    }


    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
    }


    public LocalDate plusWorkingDays(LocalDate date, int workingDays) {
        LocalDate d = date;
        int added = 0;
        while (added < workingDays) {
            d = d.plusDays(1);
            if (isWorkingDay(d)) added++;
        }
        return d;
    }
}