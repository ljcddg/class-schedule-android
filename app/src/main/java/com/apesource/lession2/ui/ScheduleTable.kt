package com.apesource.lession2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apesource.lession2.data.Course
import com.apesource.lession2.data.ScheduleDay
import com.apesource.lession2.data.SettingsManager
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class SelectionArea(
    val dayIndex: Int,
    val startPeriod: Int,
    val endPeriod: Int,
    val dayOfWeek: Int
)

data class MergedCourseCell(
    val courseId: String,
    val name: String,
    val teacher: String,
    val location: String,
    val color: String,
    val credits: String,
    val note: String,
    val startPeriod: Int,
    val endPeriod: Int,
    val dayOfWeek: Int,
    val weeks: List<Int>,
    val originalCourses: List<Course>
)

/**
 * 按 courseId + dayOfWeek + location 合并连续节次
 */
private fun mergeCoursesOnDay(courses: List<Course>): List<MergedCourseCell> {
    if (courses.isEmpty()) return emptyList()

    val sorted = courses.sortedBy { it.startPeriod }
    val result = mutableListOf<MergedCourseCell>()
    val used = mutableSetOf<String>()

    for (i in sorted.indices) {
        val c = sorted[i]
        if (c.id in used) continue

        val groupKey = c.courseId.ifEmpty { c.id }
        val group = mutableListOf(c)
        used.add(c.id)
        var mergedStart = c.startPeriod
        var mergedEnd = c.endPeriod

        var extended: Boolean
        do {
            extended = false
            for (j in sorted.indices) {
                val other = sorted[j]
                if (other.id in used) continue
                val otherKey = other.courseId.ifEmpty { other.id }
                // 按 courseId + dayOfWeek + location 合并，必须全部匹配且节次连续
                if (otherKey == groupKey &&
                    other.dayOfWeek == c.dayOfWeek &&
                    other.location == c.location
                ) {
                    if (other.startPeriod == mergedEnd + 1 ||
                        other.endPeriod + 1 == mergedStart
                    ) {
                        mergedStart = minOf(mergedStart, other.startPeriod)
                        mergedEnd = maxOf(mergedEnd, other.endPeriod)
                        group.add(other)
                        used.add(other.id)
                        extended = true
                    }
                }
            }
        } while (extended)

        result.add(
            MergedCourseCell(
                courseId = groupKey,
                name = c.name,
                teacher = c.teacher,
                location = c.location,
                color = c.color,
                credits = c.credits,
                note = c.note,
                startPeriod = mergedStart,
                endPeriod = mergedEnd,
                dayOfWeek = c.dayOfWeek,
                weeks = c.weeks,
                originalCourses = group.sortedBy { it.startPeriod }
            )
        )
    }

    return result.sortedBy { it.startPeriod }
}

@Composable
fun ScheduleTable(
    schedule: List<ScheduleDay>,
    periods: List<String>,
    onCourseClick: (Course) -> Unit,
    onSelectionComplete: (SelectionArea) -> Unit,
    selectionArea: SelectionArea? = null,
    onSelectionChange: (SelectionArea?) -> Unit,
    currentDate: LocalDate,
    courseHeight: SettingsManager.CourseHeight = SettingsManager.CourseHeight.STANDARD,
    fontSize: SettingsManager.FontSize = SettingsManager.FontSize.NORMAL,
    onCourseMove: ((courseId: String, dayDelta: Int, periodDelta: Int) -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    val firstDayOfWeek =
        currentDate.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

    val mergedByDay = remember(schedule) {
        schedule.map { day ->
            mergeCoursesOnDay(day.courses)
        }
    }

    val dashColor = Color(0xFFCBD5E1)
    val cellBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    val cellHeight = courseHeight.value.dp
    val cellHeightPx = with(LocalDensity.current) { cellHeight.toPx() }
    val fontSizeSp = fontSize.value.sp
    val courseCorner = 6.dp

    // 拖动课程的状态
    var dragState by remember { mutableStateOf<CourseDragState?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        // 表头行
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(cellHeight)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "节次",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            schedule.forEachIndexed { index, day ->
                val date = firstDayOfWeek.plusDays(index.toLong())
                val isToday = date.isEqual(currentDate)
                val dayNumber = date.dayOfMonth

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(cellHeight)
                        .background(
                            if (isToday) Color(0xFFEBF5FF)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(0.5.dp, cellBorderColor)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isToday) Color(0xFF3B82F6)
                            else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isToday) Color(0xFF3B82F6)
                                    else Color.Transparent
                                )
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNumber",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 节次行
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            periods.forEachIndexed { periodIndex, period ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 节次标签
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(cellHeight)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(0.5.dp, cellBorderColor)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${periodIndex + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            Text(
                                text = period.replace("-", "~"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 8.sp
                            )
                        }
                    }

                    // 每天每节次的单元格
                    schedule.forEachIndexed { dayIndex, day ->
                        val mergedList = mergedByDay.getOrElse(dayIndex) { emptyList() }
                        val mergedCourse = mergedList.find {
                            periodIndex + 1 in it.startPeriod..it.endPeriod
                        }
                        val hasCourse = mergedCourse != null
                        val activeMerged = mergedCourse
                        val isCourseStart = hasCourse && activeMerged!!.startPeriod == periodIndex + 1
                        val isCourseEnd = hasCourse && activeMerged!!.endPeriod == periodIndex + 1
                        val isMiddleCell = hasCourse && !isCourseStart && !isCourseEnd

                        // 当前 cell 是否是拖动源
                        val isDragging = dragState?.let { ds ->
                            ds.courseId == activeMerged?.courseId &&
                            ds.sourceDayIndex == dayIndex &&
                            periodIndex + 1 in activeMerged.startPeriod..activeMerged.endPeriod
                        } ?: false

                        // 检测上下邻接 cell 是否属于同一课程
                        val courseAbove = if (hasCourse && periodIndex > 0)
                            mergedList.find { periodIndex in it.startPeriod..it.endPeriod } else null
                        val courseBelow = if (hasCourse && periodIndex + 2 <= periods.size)
                            mergedList.find { periodIndex + 2 in it.startPeriod..it.endPeriod } else null
                        val sameAbove = courseAbove != null && courseAbove === activeMerged
                        val sameBelow = courseBelow != null && courseBelow === activeMerged

                        val isSelected = selectionArea != null &&
                                dayIndex == selectionArea.dayIndex &&
                                periodIndex + 1 >= selectionArea.startPeriod &&
                                periodIndex + 1 <= selectionArea.endPeriod

                        val cellBackground = when {
                            isDragging -> Color(android.graphics.Color.parseColor(activeMerged!!.color)).copy(alpha = 0.4f)
                            isSelected -> Color(0xFF3B82F6)
                            activeMerged != null -> Color(android.graphics.Color.parseColor(activeMerged.color))
                            else -> Color.Transparent
                        }

                        // --- 纯 drawBehind 渲染，不 clip 不 border 不 padding ---
                        val cellModifier = Modifier
                            .weight(1f)
                            .height(cellHeight)
                            .drawBehind {
                                val borderWidth = 1.5.dp.toPx()
                                val cornerPx = courseCorner.toPx()
                                val extendPx = 2.dp.toPx()

                                if (hasCourse) {
                                    // 背景：只在同课块内部延伸覆盖缝隙，不同课程间不延伸保留虚线
                                    val bgTop = if (sameAbove) -extendPx else 0f
                                    val bgBottom = if (sameBelow) size.height + extendPx else size.height
                                    drawRoundRect(
                                        color = cellBackground,
                                        topLeft = Offset(0f, bgTop),
                                        size = Size(size.width, bgBottom - bgTop),
                                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                                    )

                                    if (!isSelected) {
                                        val left = 0f
                                        val top = 0f
                                        val right = size.width
                                        val bottom = size.height
                                        val w = borderWidth

                                        if (isCourseStart && isCourseEnd) {
                                            // 单独 cell：四边白边 + 四角圆角
                                            drawRoundRect(
                                                color = Color.White,
                                                topLeft = Offset(left, top),
                                                size = Size(right - left, bottom - top),
                                                cornerRadius = CornerRadius(cornerPx),
                                                style = Stroke(w)
                                            )
                                        } else if (isCourseStart) {
                                            // 顶部 cell：上边 + 左右边（到本 cell 底部） + 上角圆角
                                            val arcSize = Size(cornerPx * 2, cornerPx * 2)
                                            drawLine(Color.White, Offset(left + cornerPx, top), Offset(right - cornerPx, top), w)
                                            drawLine(Color.White, Offset(left, top + cornerPx), Offset(left, bottom), w)
                                            drawLine(Color.White, Offset(right, top + cornerPx), Offset(right, bottom), w)
                                            drawArc(Color.White, 180f, 90f, false,
                                                topLeft = Offset(left, top), size = arcSize, style = Stroke(w))
                                            drawArc(Color.White, 270f, 90f, false,
                                                topLeft = Offset(right - cornerPx * 2, top), size = arcSize, style = Stroke(w))
                                        } else if (isCourseEnd) {
                                            // 底部 cell：下边 + 左右边（从本 cell 顶部开始） + 下角圆角
                                            val arcSize = Size(cornerPx * 2, cornerPx * 2)
                                            drawLine(Color.White, Offset(left, top), Offset(left, bottom - cornerPx), w)
                                            drawLine(Color.White, Offset(right, top), Offset(right, bottom - cornerPx), w)
                                            drawLine(Color.White, Offset(left + cornerPx, bottom), Offset(right - cornerPx, bottom), w)
                                            drawArc(Color.White, 90f, 90f, false,
                                                topLeft = Offset(left, bottom - cornerPx * 2), size = arcSize, style = Stroke(w))
                                            drawArc(Color.White, 0f, 90f, false,
                                                topLeft = Offset(right - cornerPx * 2, bottom - cornerPx * 2), size = arcSize, style = Stroke(w))
                                        } else {
                                            // 中间 cell：左右白边（同课块内部延伸衔接，边界不延伸）
                                            val lineTop = if (sameAbove) -extendPx else 0f
                                            val lineBottom = if (sameBelow) bottom + extendPx else bottom
                                            drawLine(Color.White, Offset(left, lineTop), Offset(left, lineBottom), w)
                                            drawLine(Color.White, Offset(right, lineTop), Offset(right, lineBottom), w)
                                        }
                                    }
                                } else if (isSelected) {
                                    drawRect(color = cellBackground, topLeft = Offset(0f, 0f), size = size)
                                } else {
                                    drawRect(color = Color.Transparent, topLeft = Offset(0f, 0f), size = size)
                                }

                                // 网格线：右侧虚线
                                drawLine(
                                    color = dashColor,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 0.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f
                                    )
                                )
                                // 网格线：底部分隔（同课块内部不画，不同课程间画虚线）
                                if (!hasCourse || !sameBelow) {
                                    drawLine(
                                        color = dashColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f
                                        )
                                    )
                                }
                            }
                            .then(
                                if (hasCourse) {
                                    // 课程块：支持长按拖动
                                    Modifier.pointerInput(dayIndex, periodIndex) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                dragState = CourseDragState(
                                                    courseId = activeMerged!!.courseId,
                                                    sourceDayIndex = dayIndex,
                                                    sourceStartPeriod = activeMerged.startPeriod
                                                )
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                            },
                                            onDragEnd = {
                                                dragState?.let { ds ->
                                                    // 计算落点（基于累积偏移量，这里简化处理）
                                                }
                                                dragState = null
                                            },
                                            onDragCancel = {
                                                dragState = null
                                            }
                                        )
                                    }.clickable {
                                        onCourseClick(activeMerged!!.originalCourses.first())
                                    }
                                } else if (isSelected) {
                                    // 选区：点击确认 + 拖动调整
                                    Modifier
                                        .clickable { onSelectionComplete(selectionArea!!) }
                                        .pointerInput(dayIndex, periodIndex) {
                                            detectVerticalDragGestures { _, dragAmount ->
                                                val periodDelta = (dragAmount / cellHeightPx).toInt()
                                                if (kotlin.math.abs(periodDelta) >= 1 && selectionArea != null) {
                                                    val currentSelection = selectionArea!!
                                                    val newEnd = currentSelection.endPeriod + periodDelta
                                                    val clampedEnd = newEnd.coerceIn(1, periods.size)
                                                    if (clampedEnd != currentSelection.endPeriod) {
                                                        onSelectionChange(
                                                            currentSelection.copy(
                                                                endPeriod = maxOf(clampedEnd, currentSelection.startPeriod),
                                                                startPeriod = minOf(clampedEnd, currentSelection.startPeriod)
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                } else {
                                    // 空白格：长按显示 + 号，点击进入添加课程
                                    Modifier.pointerInput(dayIndex, periodIndex) {
                                        detectTapGestures(
                                            onLongPress = {
                                                onSelectionChange(
                                                    SelectionArea(
                                                        dayIndex = dayIndex,
                                                        startPeriod = periodIndex + 1,
                                                        endPeriod = periodIndex + 1,
                                                        dayOfWeek = day.dayOfWeek
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            )

                        Box(cellModifier) {
                            if (hasCourse) {
                                // 收集所有信息行
                                val lines = buildList {
                                    add(Triple(activeMerged!!.name, fontSizeSp, FontWeight.SemiBold))
                                    if (activeMerged.location.isNotBlank())
                                        add(Triple(activeMerged.location, (fontSize.value - 1).coerceAtLeast(8).sp, FontWeight.Normal))
                                    if (activeMerged.teacher.isNotBlank())
                                        add(Triple(activeMerged.teacher, (fontSize.value - 1).coerceAtLeast(8).sp, FontWeight.Normal))
                                    if (activeMerged.credits.isNotBlank())
                                        add(Triple("${activeMerged.credits} 学分", (fontSize.value - 2).coerceAtLeast(8).sp, FontWeight.Normal))
                                    if (activeMerged.note.isNotBlank())
                                        add(Triple(activeMerged.note, (fontSize.value - 2).coerceAtLeast(8).sp, FontWeight.Normal))
                                }

                                // 当前 cell 在课块中的位置（从 0 开始）
                                val posInBlock =
                                    periodIndex + 1 - (activeMerged.startPeriod)
                                val blockCellCount =
                                    activeMerged.endPeriod - activeMerged.startPeriod + 1

                                // 行数均分到每个 cell：首 cell 尽量少行（让内容分散），末 cell 兜底
                                val linesPerCell = if (blockCellCount == 1) lines.size
                                    else maxOf(1, (lines.size + blockCellCount - 1) / blockCellCount)
                                val begin = posInBlock * linesPerCell
                                val end = minOf(lines.size, begin + linesPerCell)

                                if (begin < lines.size) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(horizontal = 5.dp, vertical = 3.dp)
                                    ) {
                                        for (i in begin until end) {
                                            val (text, fs, fw) = lines[i]
                                            val alpha = when (i) {
                                                0 -> 1f
                                                1 -> 0.85f
                                                2 -> 0.7f
                                                3 -> 0.65f
                                                else -> 0.6f
                                            }
                                            Text(
                                                text = text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = alpha),
                                                fontWeight = fw,
                                                fontSize = fs,
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else if (isSelected && (periodIndex + 1 == selectionArea!!.startPeriod)) {
                                // 选区第一个 cell 显示添加图标
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "添加课程",
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CourseDragState(
    val courseId: String,
    val sourceDayIndex: Int,
    val sourceStartPeriod: Int
)
