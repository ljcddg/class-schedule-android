package com.apesource.lession2.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.apesource.lession2.data.Course
import com.apesource.lession2.data.CourseDataSource

private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldLight = Color(0xFFD1FAE5)
private val AmberAccent = Color(0xFFF59E0B)
private val IndigoAccent = Color(0xFF6366F1)
private val RoseAccent = Color(0xFFF43F5E)
private val CyanAccent = Color(0xFF06B6D4)

data class TimeSlot(
    val dayOfWeek: Int = 1,
    val startPeriod: Int = 1,
    val endPeriod: Int = 2,
    val teacher: String = "",
    val location: String = "",
    val weeks: Set<Int> = (1..20).toSet()
)

data class PrefilledTimeSlot(
    val dayOfWeek: Int,
    val startPeriod: Int,
    val endPeriod: Int,
    val courseName: String = "",
    val teacher: String = "",
    val location: String = "",
    val color: String = "",
    val credits: String = "",
    val note: String = "",
    val courseId: String = "",
    val weeks: List<Int> = (1..20).toList(),
    val relatedTimeSlots: List<TimeSlot> = emptyList()
)

val colorPalette = listOf(
    "#FF3B82F6", "#FF6366F1", "#FF8B5CF6", "#FFA855F7",
    "#FFEC4899", "#FFF43F5E", "#FFEF4444", "#FFF97316",
    "#FFF59E0B", "#FFEAB308", "#FF84CC16", "#FF22C55E",
    "#FF10B981", "#FF14B8A6", "#FF06B6D4", "#FF0EA5E9",
    "#FF3B82F6", "#FF64748B", "#FF475569", "#FF334155",
    "#FF1E293B", "#FF78716C", "#FFA8A29E", "#FFD6D3D1",
    "#FFFDA4AF", "#FFFBBF24", "#FFA3E635", "#FF6EE7B7",
    "#FF67E8F9", "#FF93C5FD", "#FFC4B5FD", "#FFF0ABFC",
    "#FFF9A8D4", "#FFFCA5A5", "#FFFDBA74", "#FFFDE047",
    "#FF86EFAC", "#FF5EEAD4", "#FF7DD3FC", "#FFA5B4FC"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(
    periods: List<String>,
    prefilledTimeSlot: PrefilledTimeSlot? = null,
    allExistingCourses: List<Course> = emptyList(),
    totalWeeks: Int = 20,
    onSave: (List<Course>) -> Unit,
    onBack: () -> Unit
) {
    // 用 prefilledTimeSlot 的 hashCode 做 key，每次编辑新课程时同步初始化，无延迟
    val initKey = prefilledTimeSlot.hashCode()
    var courseName by remember(initKey) { mutableStateOf(prefilledTimeSlot?.courseName ?: "") }
    var selectedColor by remember(initKey) { mutableStateOf(prefilledTimeSlot?.color?.ifEmpty { null } ?: colorPalette[0]) }
    var credits by remember(initKey) { mutableStateOf(prefilledTimeSlot?.credits ?: "") }
    var note by remember(initKey) { mutableStateOf(prefilledTimeSlot?.note ?: "") }
    var timeSlots by remember(initKey) {
        val p = prefilledTimeSlot
        val slots = if (p != null && p.relatedTimeSlots.isNotEmpty()) {
            p.relatedTimeSlots
        } else if (p != null) {
            listOf(TimeSlot(p.dayOfWeek, p.startPeriod, p.endPeriod, p.teacher, p.location, p.weeks.toSet()))
        } else {
            listOf(TimeSlot())
        }
        mutableStateOf(slots)
    }
    var showCreditsDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    val suggestedTags = listOf("培训班", "Java EE企业级开发新技术", "软件工程", "数据库原理", "人工智能", "机器学习")
    val isEditing = prefilledTimeSlot?.courseId?.isNotEmpty() == true

    val existingTeachers = remember(allExistingCourses) {
        allExistingCourses.map { it.teacher }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val existingLocations = remember(allExistingCourses) {
        allExistingCourses.map { it.location }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val hasUnsavedChanges = courseName.isNotBlank() ||
            credits.isNotBlank() ||
            note.isNotBlank() ||
            timeSlots.any { it.teacher.isNotBlank() || it.location.isNotBlank() }

    fun handleBackPress() {
        if (hasUnsavedChanges) {
            showExitDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = !showExitDialog) {
        handleBackPress()
    }

    if (showExitDialog) {
        ExitConfirmDialog(
            courseName = courseName,
            timeSlots = timeSlots,
            prefilledTimeSlot = prefilledTimeSlot,
            selectedColor = selectedColor,
            credits = credits,
            note = note,
            onSave = { onSave(it) },
            onLeave = { onBack() },
            onDismiss = { showExitDialog = false }
        )
    }

    if (showCreditsDialog) {
        CreditsInputDialog(
            credits = credits,
            onConfirm = {
                credits = it
                showCreditsDialog = false
            },
            onDismiss = { showCreditsDialog = false }
        )
    }

    fun buildCourseList(): List<Course> {
        val sharedCourseId = if (prefilledTimeSlot?.courseId?.isNotEmpty() == true)
            prefilledTimeSlot.courseId
        else
            java.util.UUID.randomUUID().toString()
        return timeSlots.map { slot ->
            Course(
                id = java.util.UUID.randomUUID().toString(),
                courseId = sharedCourseId,
                name = courseName,
                teacher = slot.teacher,
                location = slot.location,
                dayOfWeek = slot.dayOfWeek,
                startPeriod = slot.startPeriod,
                endPeriod = slot.endPeriod,
                color = selectedColor,
                credits = credits,
                note = note,
                weeks = slot.weeks.toList().sorted()
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "编辑课程" else "添加课程",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = { handleBackPress() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (courseName.isNotBlank() && timeSlots.isNotEmpty()) {
                                onSave(buildCourseList())
                            }
                        }
                    ) {
                        Text(
                            text = "保存",
                            style = MaterialTheme.typography.titleMedium,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    timeSlots = timeSlots + TimeSlot()
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加时间段")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InputRow(
                    icon = Icons.Default.Book,
                    label = "课程名称",
                    value = courseName,
                    onValueChange = { courseName = it }
                )
            }

            item {
                Text(
                    text = "标签",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestedTags.size) { index ->
                        val tag = suggestedTags[index]
                        val isSelected = courseName == tag
                        AssistChip(
                            onClick = { courseName = tag },
                            label = { Text(tag, maxLines = 1) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            item {
                ColorPickerCard(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
            }

            item {
                ClickableInputRow(
                    icon = Icons.Default.School,
                    label = "学分（可不填）",
                    value = credits.ifEmpty { "点击设置" },
                    accentColor = CyanAccent,
                    onClick = { showCreditsDialog = true }
                )
            }

            item {
                InputRow(
                    icon = Icons.Default.Note,
                    label = "备注（可不填）",
                    value = note,
                    onValueChange = { note = it }
                )
            }

            item {
                JsonTemplateCard()
            }

            timeSlots.forEachIndexed { index, timeSlot ->
                item(key = "timeslot_$index") {
                    TimeSlotCard(
                        index = index,
                        timeSlot = timeSlot,
                        periods = periods,
                        totalWeeks = totalWeeks,
                        existingTeachers = existingTeachers,
                        existingLocations = existingLocations,
                        courseName = courseName,
                        selectedColor = selectedColor,
                        canDelete = timeSlots.size > 1,
                        onUpdate = { updated ->
                            timeSlots = timeSlots.mapIndexed { i, s -> if (i == index) updated else s }
                        },
                        onDelete = {
                            timeSlots = timeSlots.filterIndexed { i, _ -> i != index }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorPickerCard(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Palette, contentDescription = "颜色", tint = EmeraldPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "选择颜色",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(selectedColor)))
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDialog) {
        ColorPaletteDialog(
            selectedColor = selectedColor,
            onColorSelected = {
                onColorSelected(it)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPaletteDialog(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var hexInput by remember { mutableStateOf(selectedColor) }
    var previewColor by remember { mutableStateOf(selectedColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择颜色",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(android.graphics.Color.parseColor(previewColor))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = previewColor,
                        color = if (android.graphics.Color.parseColor(previewColor).let {
                            val r = (it shr 16) and 0xFF
                            val g = (it shr 8) and 0xFF
                            val b = it and 0xFF
                            (r * 0.299 + g * 0.587 + b * 0.114) < 140
                        }) Color.White else Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "预设颜色",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(10),
                    modifier = Modifier.height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = true
                ) {
                    items(colorPalette) { color ->
                        val isSelected = previewColor == color
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .then(
                                    if (isSelected) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(6.dp))
                                    else Modifier
                                )
                                .clickable {
                                    previewColor = color
                                    hexInput = color
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "自定义颜色（HEX）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        hexInput = input
                        val cleaned = input.trim().let {
                            if (!it.startsWith("#")) "#$it" else it
                        }
                        if (cleaned.length == 7 || cleaned.length == 9) {
                            try {
                                android.graphics.Color.parseColor(cleaned)
                                previewColor = cleaned
                            } catch (_: Exception) { }
                        }
                    },
                    label = { Text("#RRGGBB") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        cursorColor = EmeraldPrimary
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(previewColor) }) {
                Text("确定", color = EmeraldPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = RoseAccent)
            }
        }
    )
}

@Composable
fun ClickableInputRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = accentColor)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (value == "点击设置") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotCard(
    index: Int,
    timeSlot: TimeSlot,
    periods: List<String>,
    totalWeeks: Int,
    existingTeachers: List<String>,
    existingLocations: List<String>,
    courseName: String,
    selectedColor: String,
    canDelete: Boolean,
    onUpdate: (TimeSlot) -> Unit,
    onDelete: () -> Unit
) {
    var showTeacherDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showWeekDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(android.graphics.Color.parseColor(selectedColor)))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (courseName.isNotBlank()) "$courseName · 时间段${index + 1}" else "时间段 ${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (canDelete) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除",
                            tint = RoseAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { showTimeDialog = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccessTime, contentDescription = "授课时间", tint = AmberAccent)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "授课时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${CourseDataSource.weekDays[timeSlot.dayOfWeek - 1]} 第${timeSlot.startPeriod}-${timeSlot.endPeriod}节",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { showWeekDialog = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = "周次", tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "周次",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (timeSlot.weeks.size == totalWeeks) "全周"
                           else if (timeSlot.weeks.isEmpty()) "未选择"
                           else formatWeekRange(timeSlot.weeks.toList().sorted(), totalWeeks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ClickableInputRow(
                icon = Icons.Default.Person,
                label = "授课老师",
                value = timeSlot.teacher.ifEmpty { "点击设置" },
                accentColor = IndigoAccent,
                onClick = { showTeacherDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ClickableInputRow(
                icon = Icons.Default.Map,
                label = "上课地点",
                value = timeSlot.location.ifEmpty { "点击设置" },
                accentColor = RoseAccent,
                onClick = { showLocationDialog = true }
            )
        }
    }

    if (showTimeDialog) {
        TimePickerDialog(
            dayOfWeek = timeSlot.dayOfWeek,
            startPeriod = timeSlot.startPeriod,
            endPeriod = timeSlot.endPeriod,
            dayNames = CourseDataSource.weekDays,
            periodCount = periods.size,
            onConfirm = { day, start, end ->
                onUpdate(timeSlot.copy(dayOfWeek = day, startPeriod = start, endPeriod = end))
                showTimeDialog = false
            },
            onDismiss = { showTimeDialog = false }
        )
    }

    if (showWeekDialog) {
        WeekPickerDialog(
            totalWeeks = totalWeeks,
            selectedWeeks = timeSlot.weeks,
            onConfirm = { weeks ->
                onUpdate(timeSlot.copy(weeks = weeks))
                showWeekDialog = false
            },
            onDismiss = { showWeekDialog = false }
        )
    }

    if (showTeacherDialog) {
        SelectionDialog(
            title = "选择授课老师",
            items = existingTeachers,
            customValue = timeSlot.teacher,
            placeholderText = "输入老师姓名",
            onSelect = { teacher ->
                onUpdate(timeSlot.copy(teacher = teacher))
                showTeacherDialog = false
            },
            onDismiss = { showTeacherDialog = false }
        )
    }

    if (showLocationDialog) {
        SelectionDialog(
            title = "选择上课地点",
            items = existingLocations,
            customValue = timeSlot.location,
            placeholderText = "输入地点名称",
            onSelect = { location ->
                onUpdate(timeSlot.copy(location = location))
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
fun TimePickerDialog(
    dayOfWeek: Int,
    startPeriod: Int,
    endPeriod: Int,
    dayNames: List<String>,
    periodCount: Int,
    onConfirm: (dayOfWeek: Int, startPeriod: Int, endPeriod: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(dayOfWeek - 1) }
    var selectedStart by remember { mutableIntStateOf(startPeriod - 1) }
    var selectedEnd by remember { mutableIntStateOf(endPeriod - 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(selectedDay + 1, selectedStart + 1, selectedEnd + 1)
            }) {
                Text("确定", color = EmeraldPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = RoseAccent)
            }
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "授课时间",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "滑动滚轮将目标项移至中央高亮区即可选中",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        "教学日",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF3B82F6)
                    )
                    Text(
                        "起始节次",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        "结束节次",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF59E0B)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WheelPicker(
                        items = dayNames,
                        selectedIndex = selectedDay,
                        onSelected = { selectedDay = it },
                        modifier = Modifier.width(80.dp)
                    )

                    WheelPicker(
                        items = (1..periodCount).map { "$it" },
                        selectedIndex = selectedStart,
                        onSelected = {
                            selectedStart = it
                            if (selectedEnd < selectedStart) {
                                selectedEnd = selectedStart
                            }
                        },
                        modifier = Modifier.width(80.dp)
                    )

                    WheelPicker(
                        items = (1..periodCount).map { "$it" },
                        selectedIndex = selectedEnd,
                        onSelected = {
                            if (it >= selectedStart) {
                                selectedEnd = it
                            }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun WeekPickerDialog(
    totalWeeks: Int,
    selectedWeeks: Set<Int>,
    onConfirm: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var currentSelection by remember { mutableStateOf(selectedWeeks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "选择周次",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = currentSelection.size == totalWeeks,
                        onClick = { currentSelection = (1..totalWeeks).toSet() },
                        label = { Text("全周") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = currentSelection.isNotEmpty() && currentSelection.all { it % 2 == 1 },
                        onClick = { currentSelection = (1..totalWeeks).filter { it % 2 == 1 }.toSet() },
                        label = { Text("单周") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = currentSelection.isNotEmpty() && currentSelection.all { it % 2 == 0 },
                        onClick = { currentSelection = (1..totalWeeks).filter { it % 2 == 0 }.toSet() },
                        label = { Text("双周") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..totalWeeks).toList()) { week ->
                            val isSelected = currentSelection.contains(week)
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) EmeraldPrimary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) EmeraldPrimary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        currentSelection = if (isSelected) {
                                            currentSelection - week
                                        } else {
                                            currentSelection + week
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$week",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (currentSelection.size == totalWeeks) "已选全部周次"
                           else if (currentSelection.isEmpty()) "未选择任何周次"
                           else formatWeekRange(currentSelection.toList().sorted(), totalWeeks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            if (currentSelection.isEmpty()) {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = RoseAccent)
                }
            } else {
                TextButton(onClick = { onConfirm(currentSelection) }) {
                    Text("确定", color = EmeraldPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = RoseAccent)
            }
        }
    )
}

@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeightDp = 48.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeightDp.toPx() }
    val boxHeightDp = 220.dp
    val verticalPaddingDp = (boxHeightDp - itemHeightDp) / 2
    val repeatFactor = 50
    val totalCount = items.size * repeatFactor
    val startIdx = selectedIndex + items.size * (repeatFactor / 2)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIdx)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            delay(80)
            val offset = listState.firstVisibleItemScrollOffset
            val idx = listState.firstVisibleItemIndex
            val nearest = if (offset > itemHeightPx / 2) idx + 1 else idx
            val nearestClamped = nearest.coerceIn(0, totalCount - 1)
            val realIdx = nearestClamped % items.size
            if (idx != nearestClamped || (idx == nearestClamped && offset > 0)) {
                coroutineScope.launch { listState.animateScrollToItem(nearestClamped) }
            }
            if (selectedIndex != realIdx) {
                onSelected(realIdx)
            }
        }
    }

    Box(
        modifier = modifier
            .height(boxHeightDp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val h = size.height / 2f
                    val highlightTop = h - itemHeightPx / 2f

                    drawRect(
                        color = EmeraldPrimary.copy(alpha = 0.10f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, highlightTop),
                        size = androidx.compose.ui.geometry.Size(size.width, itemHeightPx)
                    )

                    val dashW = 8.dp.toPx()
                    val dashG = 4.dp.toPx()

                    drawLine(
                        color = EmeraldPrimary.copy(alpha = 0.35f),
                        start = androidx.compose.ui.geometry.Offset(8.dp.toPx(), highlightTop),
                        end = androidx.compose.ui.geometry.Offset(size.width - 8.dp.toPx(), highlightTop),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashW, dashG), 0f)
                    )
                    drawLine(
                        color = EmeraldPrimary.copy(alpha = 0.35f),
                        start = androidx.compose.ui.geometry.Offset(8.dp.toPx(), highlightTop + itemHeightPx),
                        end = androidx.compose.ui.geometry.Offset(size.width - 8.dp.toPx(), highlightTop + itemHeightPx),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashW, dashG), 0f)
                    )
                }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = verticalPaddingDp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalCount) { index ->
                val realIdx = index % items.size
                val isSelected = realIdx == selectedIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[realIdx],
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = if (isSelected) 18.sp else 14.sp,
                        color = if (isSelected) EmeraldPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionDialog(
    title: String,
    items: List<String>,
    customValue: String,
    placeholderText: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(customValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    placeholder = { Text(placeholderText) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        cursorColor = EmeraldPrimary
                    )
                )

                if (items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "已有选项",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (items.size <= 6) {
                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { textValue = item }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (textValue == item) EmeraldPrimary else Color.Transparent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (textValue == item) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(items) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { textValue = item }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (textValue == item) EmeraldPrimary else Color.Transparent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (textValue == item) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSelect(textValue) }
            ) {
                Text("确定", color = EmeraldPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = RoseAccent)
            }
        }
    )
}

@Composable
fun InputRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = getIconTint(label))
        Spacer(modifier = Modifier.width(12.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = EmeraldPrimary,
                focusedLabelColor = EmeraldPrimary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

fun getIconTint(label: String): Color {
    return when {
        label.contains("学分") -> CyanAccent
        label.contains("备注") -> AmberAccent
        label.contains("老师") -> IndigoAccent
        label.contains("地点") -> RoseAccent
        label.contains("课程") -> EmeraldPrimary
        else -> EmeraldPrimary
    }
}

@Composable
fun JsonTemplateCard() {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val templateJson = """{
  "version": 1,
  "courses": [
    {
      "name": "高等数学",
      "teacher": "王教授",
      "location": "教学楼A-301",
      "dayOfWeek": 1,
      "startPeriod": 1,
      "endPeriod": 2,
      "color": "#FF6366F1",
      "credits": "4",
      "note": "带课本",
      "weeks": [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20]
    }
  ]
}"""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = IndigoAccent)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "JSON 导入模板",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = IndigoAccent
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "将模板发给 AI，描述你的课程后 AI 会返回填好的 JSON，再通过「分享口令导入」即可",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = templateJson,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("课程JSON模板", templateJson)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "模板已复制，发送给 AI 即可", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = IndigoAccent)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("一键复制模板", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ExitConfirmDialog(
    courseName: String,
    timeSlots: List<TimeSlot>,
    prefilledTimeSlot: PrefilledTimeSlot?,
    selectedColor: String,
    credits: String,
    note: String,
    onSave: (List<Course>) -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "是否保存当前的编辑？",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text("选择\"保存\"将保留所做的修改，选择\"离开\"将放弃修改。")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    if (courseName.isNotBlank() && timeSlots.isNotEmpty()) {
                        val sharedCourseId = if (prefilledTimeSlot?.courseId?.isNotEmpty() == true)
                            prefilledTimeSlot.courseId
                        else
                            java.util.UUID.randomUUID().toString()
                        val courses = timeSlots.mapIndexed { idx, slot ->
                            Course(
                                id = java.util.UUID.randomUUID().toString(),
                                courseId = sharedCourseId,
                                name = courseName,
                                teacher = slot.teacher,
                                location = slot.location,
                                dayOfWeek = slot.dayOfWeek,
                                startPeriod = slot.startPeriod,
                                endPeriod = slot.endPeriod,
                                color = selectedColor,
                                credits = credits,
                                note = note,
                                weeks = slot.weeks.toList().sorted()
                            )
                        }
                        onSave(courses)
                    } else {
                        onLeave()
                    }
                }
            ) {
                Text("保存", color = EmeraldPrimary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onLeave()
                }
            ) {
                Text("离开", color = RoseAccent)
            }
        }
    )
}

@Composable
fun CreditsInputDialog(
    credits: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var dialogCredits by remember { mutableStateOf(credits) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "请输入学分",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            OutlinedTextField(
                value = dialogCredits,
                onValueChange = { dialogCredits = it },
                label = { Text("学分") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    cursorColor = EmeraldPrimary
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(dialogCredits)
                }
            ) {
                Text("确定", color = EmeraldPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = RoseAccent)
            }
        }
    )
}

private fun formatWeekRange(sortedWeeks: List<Int>, totalWeeks: Int): String {
    if (sortedWeeks.isEmpty()) return "未选择"
    if (sortedWeeks.size == totalWeeks) return "全周"

    val ranges = mutableListOf<String>()
    var start = sortedWeeks[0]
    var end = sortedWeeks[0]

    for (i in 1 until sortedWeeks.size) {
        if (sortedWeeks[i] == end + 1) {
            end = sortedWeeks[i]
        } else {
            ranges.add(if (start == end) "$start" else "$start-$end")
            start = sortedWeeks[i]
            end = sortedWeeks[i]
        }
    }
    ranges.add(if (start == end) "$start" else "$start-$end")
    return ranges.joinToString(",") + "周"
}

@Composable
fun DashedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .drawBehind {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.35f),
                    start = Offset(16.dp.toPx(), size.height / 2),
                    end = Offset(size.width - 16.dp.toPx(), size.height / 2),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(8.dp.toPx(), 4.dp.toPx()), 0f
                    )
                )
            }
    )
}
