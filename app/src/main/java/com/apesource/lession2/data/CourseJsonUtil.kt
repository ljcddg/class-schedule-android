package com.apesource.lession2.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object CourseJsonUtil {

    private const val VERSION = 2

    fun exportToJson(courses: List<Course>): String {
        val root = JSONObject()
        root.put("version", VERSION)
        val coursesArray = JSONArray()
        for (course in courses) {
            val courseObj = JSONObject()
            courseObj.put("id", course.id)
            courseObj.put("courseId", course.courseId.ifEmpty { course.id })
            courseObj.put("name", course.name)
            courseObj.put("teacher", course.teacher)
            courseObj.put("location", course.location)
            courseObj.put("dayOfWeek", course.dayOfWeek)
            courseObj.put("startPeriod", course.startPeriod)
            courseObj.put("endPeriod", course.endPeriod)
            courseObj.put("color", course.color)
            courseObj.put("credits", course.credits)
            courseObj.put("note", course.note)
            val weeksArray = JSONArray()
            course.weeks.forEach { weeksArray.put(it) }
            courseObj.put("weeks", weeksArray)
            coursesArray.put(courseObj)
        }
        root.put("courses", coursesArray)
        return root.toString(2)
    }

    fun importFromJson(jsonString: String): ImportResult {
        return try {
            val root = JSONObject(jsonString.trim())
            val coursesArray = root.getJSONArray("courses")
            val rawCourses = mutableListOf<Course>()

            for (i in 0 until coursesArray.length()) {
                val obj = coursesArray.getJSONObject(i)
                val course = Course(
                    id = if (obj.has("id")) obj.getString("id") else UUID.randomUUID().toString(),
                    courseId = obj.optString("courseId", ""),
                    name = obj.optString("name", ""),
                    teacher = obj.optString("teacher", ""),
                    location = obj.optString("location", ""),
                    dayOfWeek = obj.optInt("dayOfWeek", 1),
                    startPeriod = obj.optInt("startPeriod", 1),
                    endPeriod = obj.optInt("endPeriod", 2),
                    color = obj.optString("color", CourseDataSource.courseColors[0]),
                    credits = obj.optString("credits", ""),
                    note = obj.optString("note", ""),
                    weeks = if (obj.has("weeks")) {
                        val arr = obj.getJSONArray("weeks")
                        (0 until arr.length()).map { arr.getInt(it) }
                    } else (1..20).toList()
                )
                rawCourses.add(course)
            }

            // 向后兼容：为没有 courseId 的课程自动生成
            val processed = processCourseIds(rawCourses)
            ImportResult.Success(processed)
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "JSON 解析失败")
        }
    }

    /**
     * 为缺少 courseId 的课程自动生成 courseId：
     * - 按 name + teacher + color 分组
     * - 同组共享一个新生成的 courseId
     * - 已有 courseId 的保持不变
     */
    private fun processCourseIds(courses: List<Course>): List<Course> {
        val groupMap = mutableMapOf<String, String>() // key: name|teacher|color -> generatedCourseId

        return courses.map { course ->
            if (course.courseId.isNotBlank()) {
                course
            } else {
                val key = "${course.name}|${course.teacher}|${course.color}"
                val generatedId = groupMap.getOrPut(key) { UUID.randomUUID().toString() }
                course.copy(courseId = generatedId)
            }
        }
    }

    sealed class ImportResult {
        data class Success(val courses: List<Course>) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
