package com.community.communityproject.service.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormatDate {

    public String formatRegDate(LocalDateTime regDate) {
        LocalDateTime now = LocalDateTime.now();

        long years = ChronoUnit.YEARS.between(regDate, now);
        long months = ChronoUnit.MONTHS.between(regDate, now) % 12; // Only months part, years excluded
        long days = ChronoUnit.DAYS.between(regDate, now) % 30; // Approximation, considering an average month
        long hours = ChronoUnit.HOURS.between(regDate, now) % 24;
        long minutes = ChronoUnit.MINUTES.between(regDate, now) % 60;
        long seconds = ChronoUnit.SECONDS.between(regDate, now) % 60;

        String unit = "seconds";  // default unit
        long value = seconds;     // default value

        // 1년 11달 이라고 해도 1년 전이 됨
        if (years > 0) {
            unit = "years";
            value = years;
        } else if (months > 0) {
            unit = "months";
            value = months;
        } else if (days > 0) {
            unit = "days";
            value = days;
        } else if (hours > 0) {
            unit = "hours";
            value = hours;
        } else if (minutes > 0) {
            unit = "minutes";
            value = minutes;
        }

        return switch (unit) {
            case "seconds" -> value + "초 전";
            case "minutes" -> value + "분 전";
            case "hours" -> value + "시간 전";
            case "days" -> value + "일 전";
            case "months" -> value + "달 전";
            case "years" -> value + "년 전";
            default -> "Unknown date"; // fallback
        };
    }
}
