package com.apesource.lession2.data

data class Course(
    val id: String,
    val courseId: String = "",
    val name: String,
    val teacher: String,
    val location: String,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val endPeriod: Int,
    val color: String,
    val credits: String = "",
    val note: String = "",
    val weeks: List<Int> = (1..20).toList()
)

data class ScheduleDay(
    val dayOfWeek: Int,
    val dayName: String,
    val courses: List<Course>
)

data class UserSchedule(
    val id: String,
    val name: String,
    val schedule: List<ScheduleDay>
)

object CourseDataSource {
    val courseColors = listOf(
        "#FF6366F1", "#FFEC4899", "#FF10B981", "#FFF59E0B",
        "#FF8B5CF6", "#FF06B6D4", "#FFF97316", "#FF14B8A6"
    )

    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    val defaultPeriods = listOf(
        "08:00-08:45",
        "08:55-09:40",
        "10:00-10:45",
        "10:55-11:40",
        "14:00-14:45",
        "14:55-15:40",
        "16:00-16:45",
        "16:55-17:40",
        "19:00-19:45",
        "19:55-20:40"
    )

    fun getMockSchedule(): List<ScheduleDay> {
        val w3to18 = (3..18).toList()
        val w9to16 = (9..16).toList()
        val w1to20 = (1..20).toList()

        return listOf(
            ScheduleDay(
                dayOfWeek = 1,
                dayName = "周一",
                courses = listOf(
                    Course(
                        id = "mock_01",
                        courseId = "course_se",
                        name = "软件工程综合技能训练",
                        teacher = "",
                        location = "软件工程实验室",
                        dayOfWeek = 1,
                        startPeriod = 9,
                        endPeriod = 10,
                        color = "#FF00BFFF",
                        weeks = w3to18
                    ),
                    Course(
                        id = "mock_02",
                        courseId = "course_se",
                        name = "软件工程综合技能训练",
                        teacher = "",
                        location = "软件工程实验室",
                        dayOfWeek = 1,
                        startPeriod = 7,
                        endPeriod = 8,
                        color = "#FF6366F1",
                        weeks = w3to18
                    )
                )
            ),
            ScheduleDay(
                dayOfWeek = 2,
                dayName = "周二",
                courses = listOf(
                    Course(
                        id = "mock_03",
                        courseId = "course_javaee",
                        name = "Java EE企业级开发新技术",
                        teacher = "刘斌",
                        location = "软件工程实验室",
                        dayOfWeek = 2,
                        startPeriod = 7,
                        endPeriod = 8,
                        color = "#FF9932CC",
                        weeks = w3to18
                    ),
                    Course(
                        id = "mock_04",
                        courseId = "course_ml",
                        name = "机器学习",
                        teacher = "阿茹罕",
                        location = "计算机基础教学实验室（1）",
                        dayOfWeek = 2,
                        startPeriod = 1,
                        endPeriod = 2,
                        color = "#FF00BFFF",
                        weeks = w9to16
                    ),
                    Course(
                        id = "mock_05",
                        courseId = "course_ml",
                        name = "机器学习",
                        teacher = "阿茹罕",
                        location = "3-310",
                        dayOfWeek = 2,
                        startPeriod = 3,
                        endPeriod = 4,
                        color = "#FF00BFFF",
                        weeks = w9to16
                    ),
                    Course(
                        id = "mock_06",
                        courseId = "course_camp",
                        name = "训练营",
                        teacher = "刘斌",
                        location = "3-321",
                        dayOfWeek = 2,
                        startPeriod = 9,
                        endPeriod = 10,
                        color = "#FFFCA5A5",
                        weeks = w1to20
                    )
                )
            ),
            ScheduleDay(
                dayOfWeek = 3,
                dayName = "周三",
                courses = emptyList()
            ),
            ScheduleDay(
                dayOfWeek = 4,
                dayName = "周四",
                courses = listOf(
                    Course(
                        id = "mock_07",
                        courseId = "course_javaee",
                        name = "Java EE企业级开发新技术",
                        teacher = "刘斌",
                        location = "软件工程实验室",
                        dayOfWeek = 4,
                        startPeriod = 7,
                        endPeriod = 8,
                        color = "#FF9932CC",
                        weeks = w3to18
                    ),
                    Course(
                        id = "mock_08",
                        courseId = "course_ml",
                        name = "机器学习",
                        teacher = "阿茹罕",
                        location = "3-310",
                        dayOfWeek = 4,
                        startPeriod = 3,
                        endPeriod = 4,
                        color = "#FF00BFFF",
                        weeks = w9to16
                    ),
                    Course(
                        id = "mock_09",
                        courseId = "course_camp",
                        name = "训练营",
                        teacher = "刘斌",
                        location = "3-321",
                        dayOfWeek = 4,
                        startPeriod = 9,
                        endPeriod = 10,
                        color = "#FFFCA5A5",
                        weeks = w1to20
                    )
                )
            ),
            ScheduleDay(
                dayOfWeek = 5,
                dayName = "周五",
                courses = emptyList()
            ),
            ScheduleDay(
                dayOfWeek = 6,
                dayName = "周六",
                courses = emptyList()
            ),
            ScheduleDay(
                dayOfWeek = 7,
                dayName = "周日",
                courses = emptyList()
            )
        )
    }
}