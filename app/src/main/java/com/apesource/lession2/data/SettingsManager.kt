package com.apesource.lession2.data

import android.content.Context
import android.content.SharedPreferences
import com.apesource.lession2.ui.theme.ThemeMode
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SettingsManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("schedule_settings", Context.MODE_PRIVATE)

    private val KEY_PERIOD_PREFIX = "period_"
    private val KEY_PERIOD_COUNT = "period_count"
    private val KEY_DAY_PREFIX = "day_"
    private val KEY_DAY_COUNT = "day_count"
    private val KEY_DISPLAY_MODE = "display_mode"
    private val KEY_SCHEDULES = "schedules_data"
    private val KEY_CURRENT_SCHEDULE_ID = "current_schedule_id"
    private val KEY_SEMESTER_START_DATE = "semester_start_date"
    private val KEY_COURSE_HEIGHT = "course_height"
    private val KEY_FONT_SIZE = "font_size"
    private val KEY_THEME_MODE = "theme_mode"
    
    enum class CourseHeight(val value: Int, val label: String) {
        COMPACT(60, "紧凑"),
        STANDARD(80, "标准"),
        LARGE(100, "大"),
        XLARGE(120, "超大")
    }
    
    enum class FontSize(val value: Int, val label: String) {
        SMALL(10, "小"),
        NORMAL(12, "标准"),
        LARGE(14, "大"),
        XLARGE(16, "超大")
    }

    fun saveCustomPeriods(periods: List<String>) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_PERIOD_COUNT, periods.size)
        periods.forEachIndexed { index, period ->
            editor.putString("$KEY_PERIOD_PREFIX$index", period)
        }
        editor.apply()
    }

    fun getCustomPeriods(): List<String> {
        val count = sharedPreferences.getInt(KEY_PERIOD_COUNT, 0)
        return if (count > 0) {
            (0 until count).mapNotNull { index ->
                sharedPreferences.getString("$KEY_PERIOD_PREFIX$index", null)
            }
        } else {
            CourseDataSource.defaultPeriods
        }
    }

    fun saveDisplayDays(days: List<Int>) {
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_DAY_COUNT, days.size)
        days.forEachIndexed { index, day ->
            editor.putInt("$KEY_DAY_PREFIX$index", day)
        }
        editor.apply()
    }

    fun getDisplayDays(): List<Int> {
        val count = sharedPreferences.getInt(KEY_DAY_COUNT, 0)
        return if (count > 0) {
            (0 until count).map { index ->
                sharedPreferences.getInt("$KEY_DAY_PREFIX$index", 0)
            }.filter { it > 0 }
        } else {
            listOf(1, 2, 3, 4, 5)
        }
    }

    fun resetToDefault() {
        sharedPreferences.edit().clear().apply()
    }

    fun hasCustomPeriods(): Boolean {
        return sharedPreferences.contains(KEY_PERIOD_COUNT)
    }

    fun saveDisplayMode(mode: String) {
        sharedPreferences.edit().putString(KEY_DISPLAY_MODE, mode).apply()
    }

    fun getDisplayMode(): String {
        return sharedPreferences.getString(KEY_DISPLAY_MODE, "WORKDAYS") ?: "WORKDAYS"
    }

    fun saveSchedules(schedules: List<UserSchedule>) {
        val jsonArray = JSONArray()
        for (s in schedules) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            val daysArray = JSONArray()
            for (day in s.schedule) {
                val dayObj = JSONObject()
                dayObj.put("dayOfWeek", day.dayOfWeek)
                dayObj.put("dayName", day.dayName)
                val coursesArray = JSONArray()
                for (c in day.courses) {
                    val courseObj = JSONObject()
                    courseObj.put("id", c.id)
                    courseObj.put("courseId", c.courseId)
                    courseObj.put("name", c.name)
                    courseObj.put("teacher", c.teacher)
                    courseObj.put("location", c.location)
                    courseObj.put("dayOfWeek", c.dayOfWeek)
                    courseObj.put("startPeriod", c.startPeriod)
                    courseObj.put("endPeriod", c.endPeriod)
                    courseObj.put("color", c.color)
                    courseObj.put("credits", c.credits)
                    courseObj.put("note", c.note)
                    val weeksArr = JSONArray()
                    c.weeks.forEach { weeksArr.put(it) }
                    courseObj.put("weeks", weeksArr)
                    coursesArray.put(courseObj)
                }
                dayObj.put("courses", coursesArray)
                daysArray.put(dayObj)
            }
            obj.put("schedule", daysArray)
            jsonArray.put(obj)
        }
        sharedPreferences.edit().putString(KEY_SCHEDULES, jsonArray.toString()).apply()
    }

    fun loadSchedules(): List<UserSchedule> {
        val jsonStr = sharedPreferences.getString(KEY_SCHEDULES, null) ?: return emptyList()
        val jsonArray = JSONArray(jsonStr)
        val schedules = mutableListOf<UserSchedule>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val daysArray = obj.getJSONArray("schedule")
            val days = mutableListOf<ScheduleDay>()
            for (j in 0 until daysArray.length()) {
                val dayObj = daysArray.getJSONObject(j)
                val coursesArray = dayObj.getJSONArray("courses")
                val courses = mutableListOf<Course>()
                for (k in 0 until coursesArray.length()) {
                    val c = coursesArray.getJSONObject(k)
                    val weeksArr = c.getJSONArray("weeks")
                    val weeks = (0 until weeksArr.length()).map { weeksArr.getInt(it) }
                    courses.add(
                        Course(
                            id = c.optString("id", UUID.randomUUID().toString()),
                            courseId = c.optString("courseId", ""),
                            name = c.optString("name", ""),
                            teacher = c.optString("teacher", ""),
                            location = c.optString("location", ""),
                            dayOfWeek = c.optInt("dayOfWeek", 1),
                            startPeriod = c.optInt("startPeriod", 1),
                            endPeriod = c.optInt("endPeriod", 2),
                            color = c.optString("color", CourseDataSource.courseColors[0]),
                            credits = c.optString("credits", ""),
                            note = c.optString("note", ""),
                            weeks = weeks
                        )
                    )
                }
                days.add(
                    ScheduleDay(
                        dayOfWeek = dayObj.optInt("dayOfWeek", 1),
                        dayName = dayObj.optString("dayName", ""),
                        courses = courses
                    )
                )
            }
            schedules.add(
                UserSchedule(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    name = obj.optString("name", "课表"),
                    schedule = days
                )
            )
        }
        return schedules
    }

    fun saveCurrentScheduleId(id: String) {
        sharedPreferences.edit().putString(KEY_CURRENT_SCHEDULE_ID, id).apply()
    }

    fun getCurrentScheduleId(): String? {
        return sharedPreferences.getString(KEY_CURRENT_SCHEDULE_ID, null)
    }

    fun saveSemesterStartDate(date: String) {
        sharedPreferences.edit().putString(KEY_SEMESTER_START_DATE, date).apply()
    }

    fun getSemesterStartDate(): String? {
        return sharedPreferences.getString(KEY_SEMESTER_START_DATE, null)
    }
    
    fun saveCourseHeight(height: CourseHeight) {
        sharedPreferences.edit().putInt(KEY_COURSE_HEIGHT, height.value).apply()
    }
    
    fun getCourseHeight(): CourseHeight {
        val value = sharedPreferences.getInt(KEY_COURSE_HEIGHT, CourseHeight.STANDARD.value)
        return CourseHeight.values().find { it.value == value } ?: CourseHeight.STANDARD
    }
    
    fun saveFontSize(size: FontSize) {
        sharedPreferences.edit().putInt(KEY_FONT_SIZE, size.value).apply()
    }
    
    fun getFontSize(): FontSize {
        val value = sharedPreferences.getInt(KEY_FONT_SIZE, FontSize.NORMAL.value)
        return FontSize.values().find { it.value == value } ?: FontSize.NORMAL
    }
    
    fun saveThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
    
    fun getThemeMode(): ThemeMode {
        val name = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return ThemeMode.values().find { it.name == name } ?: ThemeMode.SYSTEM
    }
}
