package com.apesource.lession2.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.ChronoUnit

object SemesterCalculator {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun parseDate(dateStr: String): LocalDate? {
        return try {
            LocalDate.parse(dateStr, dateFormatter)
        } catch (_: Exception) {
            null
        }
    }

    fun formatDate(date: LocalDate?): String {
        return date?.format(dateFormatter) ?: ""
    }

    private fun getSemesterStart(now: LocalDate, customStartDate: LocalDate? = null): LocalDate {
        return if (customStartDate != null) {
            customStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        } else {
            guessSemesterStart(now)
        }
    }

    private fun guessSemesterStart(now: LocalDate): LocalDate {
        val year = now.year
        val month = now.monthValue

        val candidate = if (month in 2..7) {
            LocalDate.of(year, 2, 24)
        } else if (month >= 9) {
            LocalDate.of(year, 9, 1)
        } else {
            LocalDate.of(year - 1, 9, 1)
        }

        return candidate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun getCurrentWeek(): Int {
        return getWeekForDate(LocalDate.now(), null)
    }

    fun getWeekForDate(date: LocalDate, customStartDate: LocalDate? = null): Int {
        val start = getSemesterStart(date, customStartDate)
        val diff = ChronoUnit.DAYS.between(start, date)
        val week = (diff / 7).toInt() + 1
        return week.coerceAtLeast(1)
    }

    fun getDateForWeek(week: Int, customStartDate: LocalDate? = null): LocalDate {
        val now = LocalDate.now()
        val dayOfWeek = now.dayOfWeek
        val semesterStart = getSemesterStart(now, customStartDate)
        val mondayOfWeek = semesterStart.plusWeeks((week - 1).toLong())
        val dayIndex = dayOfWeek.value - DayOfWeek.MONDAY.value
        return mondayOfWeek.plusDays(dayIndex.coerceAtLeast(0).toLong())
    }

    fun calculateStartDateFromWeek(currentWeek: Int, referenceDate: LocalDate = LocalDate.now()): LocalDate {
        val mondayOfCurrentWeek = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return mondayOfCurrentWeek.minusWeeks((currentWeek - 1).toLong())
    }

    fun calculateWeekFromStart(startDateStr: String, referenceDate: LocalDate = LocalDate.now()): Int {
        val start = parseDate(startDateStr)
        val monday = start?.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) ?: return 1
        val diff = ChronoUnit.DAYS.between(monday, referenceDate)
        return ((diff / 7).toInt() + 1).coerceAtLeast(1)
    }
}
