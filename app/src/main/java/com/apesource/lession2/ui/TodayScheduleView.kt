package com.apesource.lession2.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apesource.lession2.data.Course
import com.apesource.lession2.data.SettingsManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class MergedCourse(
    val name: String,
    val teacher: String,
    val location: String,
    val dayOfWeek: Int,
    val startPeriod: Int,
    val endPeriod: Int,
    val color: String,
    val credits: String,
    val note: String,
    val ids: List<String>
)

@Composable
fun TodayScheduleView(
    allCourses: List<Course>,
    periods: List<String>,
    currentDate: LocalDate,
    onCourseClick: (Course) -> Unit,
    fontSize: SettingsManager.FontSize = SettingsManager.FontSize.NORMAL
) {
    val todayDayOfWeek = currentDate.dayOfWeek.value

    val todayCourses = allCourses.filter { it.dayOfWeek == todayDayOfWeek }
        .sortedBy { it.startPeriod }

    if (todayCourses.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFFB0BEC5),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "今日暂无课程",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF90A4AE)
                )
            }
        }
        return
    }

    val mergedCourses = mergeCourses(todayCourses, periods)
    val now = LocalTime.now()
    val expandedState = remember { mutableStateMapOf<Int, Boolean>() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "今日",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "共${todayCourses.size}节课",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(mergedCourses) { index, merged ->
                val lastPeriodEndStr = getPeriodEndTime(periods, merged.endPeriod)
                val firstPeriodStartStr = getPeriodStartTime(periods, merged.startPeriod)
                val lastPeriodEndTime = try {
                    if (lastPeriodEndStr.isNotBlank()) {
                        LocalTime.parse(lastPeriodEndStr, DateTimeFormatter.ofPattern("HH:mm"))
                    } else {
                        LocalTime.MIDNIGHT
                    }
                } catch (e: Exception) {
                    LocalTime.of(23, 59)
                }
                val isCompleted = now.isAfter(lastPeriodEndTime)

                val isExpanded = expandedState[index] ?: !isCompleted

                if (index > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                CourseAgendaCard(
                    merged = merged,
                    timeRange = "${firstPeriodStartStr}-${lastPeriodEndStr}",
                    index = index,
                    isCompleted = isCompleted,
                    isExpanded = isExpanded,
                    onToggle = {
                        expandedState[index] = !(expandedState[index] ?: !isCompleted)
                    },
                    onCourseClick = {
                        val course = todayCourses.find { it.id == merged.ids.first() }
                        if (course != null) onCourseClick(course)
                    },
                    fontSize = fontSize
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun CourseAgendaCard(
    merged: MergedCourse,
    timeRange: String,
    index: Int,
    isCompleted: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCourseClick: () -> Unit,
    fontSize: SettingsManager.FontSize = SettingsManager.FontSize.NORMAL
) {
    val courseColor = try {
        Color(android.graphics.Color.parseColor(merged.color))
    } catch (e: Exception) {
        Color(0xFF6366F1)
    }

    val backgroundColor = when {
        isCompleted && isExpanded -> Color(0xFFF5F5F5)
        else -> Color.White
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isExpanded) 100.dp else 48.dp)
                    .background(courseColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "已完成",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = merged.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF616161) else Color(0xFF212121),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    if (isCompleted) {
                        IconButton(
                            onClick = onToggle,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "收起" else "展开",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFFBDBDBD) else Color(0xFF78909C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) Color(0xFF9E9E9E) else Color(0xFF546E7A),
                        fontSize = fontSize.value.sp
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        HorizontalDivider(
                            color = Color(0xFFE0E0E0),
                            thickness = 0.5.dp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isCompleted) Color(0xFFBDBDBD) else Color(0xFF78909C),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = merged.teacher,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) Color(0xFF9E9E9E) else Color(0xFF546E7A),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isCompleted) Color(0xFFBDBDBD) else Color(0xFF78909C),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = merged.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) Color(0xFF9E9E9E) else Color(0xFF546E7A),
                                fontSize = 12.sp
                            )
                        }

                        if (merged.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = merged.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) Color(0xFFBDBDBD) else Color(0xFF78909C),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

private fun mergeCourses(
    courses: List<Course>,
    periods: List<String>
): List<MergedCourse> {
    if (courses.isEmpty()) return emptyList()

    val sorted = courses.sortedBy { it.startPeriod }
    val merged = mutableListOf<MergedCourse>()

    var current = MergedCourse(
        name = sorted[0].name,
        teacher = sorted[0].teacher,
        location = sorted[0].location,
        dayOfWeek = sorted[0].dayOfWeek,
        startPeriod = sorted[0].startPeriod,
        endPeriod = sorted[0].endPeriod,
        color = sorted[0].color,
        credits = sorted[0].credits,
        note = sorted[0].note,
        ids = mutableListOf(sorted[0].id)
    )

    for (i in 1 until sorted.size) {
        val next = sorted[i]
        // 按 courseId + 连续节次合并
        val currentKey = sorted[i - 1].courseId.ifEmpty { sorted[i - 1].id }
        val nextKey = next.courseId.ifEmpty { next.id }
        if (nextKey == currentKey &&
            next.startPeriod == current.endPeriod + 1
        ) {
            current = current.copy(
                endPeriod = next.endPeriod,
                ids = current.ids + next.id
            )
        } else {
            merged.add(current)
            current = MergedCourse(
                name = next.name,
                teacher = next.teacher,
                location = next.location,
                dayOfWeek = next.dayOfWeek,
                startPeriod = next.startPeriod,
                endPeriod = next.endPeriod,
                color = next.color,
                credits = next.credits,
                note = next.note,
                ids = mutableListOf(next.id)
            )
        }
    }
    merged.add(current)

    return merged
}

private fun getPeriodStartTime(periods: List<String>, periodIndex: Int): String {
    val idx = periodIndex - 1
    if (idx < 0 || idx >= periods.size) return ""
    return periods[idx].split("-")[0]
}

private fun getPeriodEndTime(periods: List<String>, periodIndex: Int): String {
    val idx = periodIndex - 1
    if (idx < 0 || idx >= periods.size) return ""
    return periods[idx].split("-")[1]
}
