// DateTimeUtils.java
package com.fitter.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class DateTimeUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 날짜 문자열 포맷팅
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    // 시간 문자열 포맷팅
    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    // 날짜시간 문자열 포맷팅
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }

    // 날짜 범위 생성 (시작일부터 종료일까지의 모든 날짜)
    public static List<LocalDate> createDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            dates.add(date);
            date = date.plusDays(1);
        }

        return dates;
    }

    // 현재 주의 시작일 (월요일) 구하기
    public static LocalDate getStartOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    // 현재 주의 종료일 (일요일) 구하기
    public static LocalDate getEndOfWeek() {
        return LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }

    // 현재 월의 시작일 구하기
    public static LocalDate getStartOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
    }

    // 현재 월의 종료일 구하기
    public static LocalDate getEndOfMonth() {
        return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
    }

    // 두 날짜 사이의 일수 계산
    public static long getDaysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    // 두 시간 사이의 분 계산
    public static long getMinutesBetween(LocalTime startTime, LocalTime endTime) {
        return ChronoUnit.MINUTES.between(startTime, endTime);
    }

    // 운동 시간대 추천 (아침/점심/저녁)
    public static LocalTime recommendExerciseTime(int preferredHour) {
        if (preferredHour >= 5 && preferredHour < 11) {
            return LocalTime.of(7, 0); // 아침 운동
        } else if (preferredHour >= 11 && preferredHour < 17) {
            return LocalTime.of(14, 0); // 점심 운동
        } else {
            return LocalTime.of(19, 0); // 저녁 운동
        }
    }

    // 휴식 시간 추천 (운동 시간 기반)
    public static int recommendRestTime(int exerciseDurationMinutes) {
        if (exerciseDurationMinutes <= 30) {
            return 1; // 1분 휴식
        } else if (exerciseDurationMinutes <= 60) {
            return 2; // 2분 휴식
        } else {
            return 3; // 3분 휴식
        }
    }
}