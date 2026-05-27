package com.apesource.lession2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apesource.lession2.data.Course
import com.apesource.lession2.data.CourseDataSource
import com.apesource.lession2.data.CourseJsonUtil
import com.apesource.lession2.data.ScheduleDay
import com.apesource.lession2.data.SemesterCalculator
import com.apesource.lession2.data.SettingsManager
import com.apesource.lession2.data.UserSchedule
import com.apesource.lession2.ui.*
import com.apesource.lession2.ui.theme.Lession2Theme
import com.apesource.lession2.ui.theme.ThemeMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager(this)

        setContent {
            val themeMode = remember { mutableStateOf(settingsManager.getThemeMode()) }
            Lession2Theme(themeMode = themeMode.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(Screen.Schedule) }
                    var currentTab by remember { mutableStateOf(TabItem.CourseTable) }
                    val selectedCourse = remember { mutableStateOf<Course?>(null) }
                    val selectionArea = remember { mutableStateOf<SelectionArea?>(null) }
                    val prefilledTimeSlot = remember { mutableStateOf<PrefilledTimeSlot?>(null) }
                    var showImportDialog by remember { mutableStateOf(false) }
                    var showNewScheduleDialog by remember { mutableStateOf(false) }
                    var newScheduleName by remember { mutableStateOf("") }

                    var periods by remember { mutableStateOf(settingsManager.getCustomPeriods()) }
                    var displayDays by remember { mutableStateOf(settingsManager.getDisplayDays()) }
                    var displayMode by remember {
                        mutableStateOf(
                            WeekDisplayMode.valueOf(settingsManager.getDisplayMode())
                        )
                    }

                    var allSchedules by remember {
                        mutableStateOf(settingsManager.loadSchedules())
                    }
                    var currentScheduleId by remember {
                        mutableStateOf<String?>(null)
                    }

                    LaunchedEffect(allSchedules) {
                        if (currentScheduleId == null) {
                            currentScheduleId = settingsManager.getCurrentScheduleId()
                                ?: allSchedules.firstOrNull()?.id
                        }
                    }

                    val currentUserSchedule by remember {
                        derivedStateOf {
                            allSchedules.find { it.id == currentScheduleId }
                        }
                    }

                    val allSchedule by remember {
                        derivedStateOf {
                            currentUserSchedule?.schedule
                                ?: CourseDataSource.getMockSchedule()
                        }
                    }

                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy/M/d")

                    val savedStartDate = remember { settingsManager.getSemesterStartDate() }
                    val customStartDate = remember(savedStartDate) {
                        SemesterCalculator.parseDate(savedStartDate ?: "")
                    }

                    var displayWeek by remember { mutableIntStateOf(SemesterCalculator.getCurrentWeek()) }

                    var semesterStartDate by remember { mutableStateOf(settingsManager.getSemesterStartDate() ?: "") }

                    val displayDate = remember(displayWeek, customStartDate) {
                        SemesterCalculator.getDateForWeek(displayWeek, customStartDate)
                    }
                    val dayOfWeek = displayDate.dayOfWeek.value
                    val dayName = CourseDataSource.weekDays[dayOfWeek - 1]
                    val dateStr = displayDate.format(dateFormatter)
                    val weekNumber = displayWeek

                    val allCourses = remember(displayWeek) {
                        derivedStateOf {
                            allSchedule.flatMap { it.courses }
                                .filter { it.weeks.contains(displayWeek) }
                        }
                    }

                    fun persistSchedules() {
                        settingsManager.saveSchedules(allSchedules)
                        if (currentScheduleId != null) {
                            settingsManager.saveCurrentScheduleId(currentScheduleId!!)
                        }
                    }

                    fun switchToSchedule(scheduleId: String) {
                        currentScheduleId = scheduleId
                        settingsManager.saveCurrentScheduleId(scheduleId)
                    }

                    fun createNewSchedule(name: String) {
                        val id = UUID.randomUUID().toString()
                        val newSchedule = UserSchedule(
                            id = id,
                            name = name,
                            schedule = CourseDataSource.weekDays.mapIndexed { index, dayName ->
                                ScheduleDay(
                                    dayOfWeek = index + 1,
                                    dayName = dayName,
                                    courses = emptyList()
                                )
                            }
                        )
                        allSchedules = allSchedules + newSchedule
                        currentScheduleId = id
                        persistSchedules()
                    }

                    fun deleteSchedule(scheduleId: String) {
                        if (allSchedules.size <= 1) return
                        allSchedules = allSchedules.filter { it.id != scheduleId }
                        if (currentScheduleId == scheduleId) {
                            currentScheduleId = allSchedules.firstOrNull()?.id
                        }
                        persistSchedules()
                    }

                    fun renameSchedule(scheduleId: String, newName: String) {
                        allSchedules = allSchedules.map {
                            if (it.id == scheduleId) it.copy(name = newName) else it
                        }
                        persistSchedules()
                    }

                    fun updateCurrentSchedule(newSchedule: List<ScheduleDay>) {
                        allSchedules = allSchedules.map {
                            if (it.id == currentScheduleId) it.copy(schedule = newSchedule) else it
                        }
                        persistSchedules()
                    }

                    fun handleClearAllCourses() {
                        val currentSchedule = allSchedule.toMutableList()
                        for (day in currentSchedule.indices) {
                            currentSchedule[day] = currentSchedule[day].copy(courses = emptyList())
                        }
                        updateCurrentSchedule(currentSchedule)
                        Toast.makeText(this@MainActivity, "已清空全部课程", Toast.LENGTH_SHORT).show()
                    }

                    fun getWeekNumber(date: LocalDate): Int {
                        return SemesterCalculator.getWeekForDate(date)
                    }

                    fun resetSelection() {
                        selectionArea.value = null
                    }

                    fun handleCourseClick(course: Course) {
                        resetSelection()
                        selectedCourse.value = course
                    }

                    fun handleSelectionComplete(selection: SelectionArea) {
                        prefilledTimeSlot.value = PrefilledTimeSlot(
                            dayOfWeek = selection.dayOfWeek,
                            startPeriod = selection.startPeriod,
                            endPeriod = selection.endPeriod,
                            color = CourseDataSource.courseColors[0]
                        )
                        resetSelection()
                        currentScreen = Screen.AddCourse
                    }

                    fun handleSelectionChange(selection: SelectionArea?) {
                        selectionArea.value = selection
                    }

                    var editingCourseId by remember { mutableStateOf<String?>(null) }
                    var editingEntryId by remember { mutableStateOf<String?>(null) }

                    fun handleSaveCourse(courses: List<Course>) {
                        val entryIdToRemove = editingEntryId
                        val currentSchedule = allSchedule.toMutableList()
                        if (entryIdToRemove != null) {
                            // 编辑模式：删除所有同名的旧条目，再用新条目替换
                            val oldEntry = currentSchedule.flatMap { it.courses }.find { it.id == entryIdToRemove }
                            val removeName = oldEntry?.name ?: ""
                            for (day in currentSchedule.indices) {
                                val updatedDay = currentSchedule[day].copy(
                                    courses = currentSchedule[day].courses.filter {
                                        it.name != removeName || it.name.isBlank()
                                    }
                                )
                                currentSchedule[day] = updatedDay
                            }
                            editingCourseId = null
                            editingEntryId = null
                        }
                        for (course in courses) {
                            val dayIndex = currentSchedule.indexOfFirst { it.dayOfWeek == course.dayOfWeek }
                            if (dayIndex >= 0) {
                                val updatedDay = currentSchedule[dayIndex].copy(
                                    courses = currentSchedule[dayIndex].courses + course
                                )
                                currentSchedule[dayIndex] = updatedDay
                            }
                        }
                        updateCurrentSchedule(currentSchedule)
                        prefilledTimeSlot.value = null
                        currentScreen = Screen.Schedule
                    }

                    fun handleDeleteCourse(course: Course) {
                        if (currentScheduleId == null) return
                        val targetCourseId = course.courseId.ifEmpty { course.id }
                        val currentSchedule = allSchedule.toMutableList()
                        for (day in currentSchedule.indices) {
                            val updatedDay = currentSchedule[day].copy(
                                courses = currentSchedule[day].courses.filter { it.courseId != targetCourseId && it.id != course.id }
                            )
                            currentSchedule[day] = updatedDay
                        }
                        updateCurrentSchedule(currentSchedule)
                        selectedCourse.value = null
                    }

                    fun handleCopyCourse(course: Course) {
                        val json = CourseJsonUtil.exportToJson(listOf(course))
                        val clipboard =
                            getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("课程JSON", json)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this@MainActivity, "课程JSON已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        selectedCourse.value = null
                    }

                    fun handleEditCourse(course: Course) {
                        val targetCourseId = course.courseId.ifEmpty { course.id }
                        editingCourseId = course.id
                        editingEntryId = course.id
                        // 按课程名查找所有同名课程的时间段
                        val relatedCourses = allSchedule.flatMap { it.courses }
                            .filter { it.name == course.name }
                            .distinctBy { it.id }
                        val relatedTimeSlots = relatedCourses.map { rc ->
                            TimeSlot(
                                dayOfWeek = rc.dayOfWeek,
                                startPeriod = rc.startPeriod,
                                endPeriod = rc.endPeriod,
                                teacher = rc.teacher,
                                location = rc.location,
                                weeks = rc.weeks.toSet()
                            )
                        }
                        prefilledTimeSlot.value = PrefilledTimeSlot(
                            dayOfWeek = course.dayOfWeek,
                            startPeriod = course.startPeriod,
                            endPeriod = course.endPeriod,
                            courseName = course.name,
                            teacher = course.teacher,
                            location = course.location,
                            color = course.color,
                            credits = course.credits,
                            note = course.note,
                            courseId = targetCourseId,
                            weeks = course.weeks,
                            relatedTimeSlots = relatedTimeSlots
                        )
                        selectedCourse.value = null
                        currentScreen = Screen.AddCourse
                    }

                    fun handleShare() {
                        if (currentScheduleId == null) return
                        val allCoursesForExport = allSchedule.flatMap { it.courses }
                        val json = CourseJsonUtil.exportToJson(allCoursesForExport)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, json)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "分享课程表")
                        startActivity(shareIntent)
                    }

                    fun handleImport(courses: List<Course>) {
                        val currentSchedule = allSchedule.toMutableList()
                        for (course in courses) {
                            val dayIndex = currentSchedule.indexOfFirst { it.dayOfWeek == course.dayOfWeek }
                            if (dayIndex >= 0) {
                                // 替换同一天同一时间段内的旧课程，其余保留
                                val filtered = currentSchedule[dayIndex].courses.filter { existing ->
                                    existing.endPeriod < course.startPeriod || existing.startPeriod > course.endPeriod
                                }
                                currentSchedule[dayIndex] = currentSchedule[dayIndex].copy(
                                    courses = filtered + course
                                )
                            }
                        }
                        updateCurrentSchedule(currentSchedule)
                        Toast.makeText(this@MainActivity, "成功导入 ${courses.size} 门课程", Toast.LENGTH_SHORT).show()
                    }

                    LaunchedEffect(allSchedules) {
                        if (allSchedules.isEmpty()) {
                            val mockSchedule = CourseDataSource.getMockSchedule()
                            val defaultSchedule = UserSchedule(
                                id = UUID.randomUUID().toString(),
                                name = "我的课表",
                                schedule = mockSchedule
                            )
                            allSchedules = listOf(defaultSchedule)
                            currentScheduleId = defaultSchedule.id
                            persistSchedules()
                        }
                    }

                    BackHandler(enabled = currentScreen != Screen.Schedule) {
                        currentScreen = Screen.Schedule
                    }

                    when (currentScreen) {
                        Screen.Schedule -> {
                            BackHandler(enabled = selectionArea.value != null) {
                                resetSelection()
                            }

                            Scaffold(
                                topBar = {
                                    Column(
                                        modifier = Modifier.statusBarsPadding()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f, fill = false)
                                            ) {
                                                Text(
                                                    text = "第${weekNumber}周 周${dayName[1]}",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp
                                                )
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            if (currentTab == TabItem.CourseTable) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(onClick = {
                                                        currentScreen = Screen.AddCourse
                                                    }) {
                                                        Icon(Icons.Default.Add, contentDescription = "添加课程", tint = MaterialTheme.colorScheme.onSurface)
                                                    }
                                                    ImportMenu(
                                                        onShare = ::handleShare,
                                                        onShareCodeImport = { showImportDialog = true }
                                                    )
                                                    MoreMenu(
                                                    currentWeek = displayWeek,
                                                    onWeekChange = { displayWeek = it },
                                                    onOpenTermSettings = { currentScreen = Screen.Settings },
                                                    onOpenPeriodSettings = { currentScreen = Screen.PeriodSettings },
                                                    onOpenGlobalSettings = { currentScreen = Screen.GlobalSettings },
                                                    onOpenCourseManage = { currentScreen = Screen.CourseManage },
                                                    onNewSchedule = {
                                                        newScheduleName = ""
                                                        showNewScheduleDialog = true
                                                    },
                                                    onOpenScheduleManage = {
                                                        currentScreen = Screen.ScheduleManage
                                                    },
                                                    schedules = allSchedules,
                                                    currentScheduleId = currentScheduleId,
                                                    onSwitchSchedule = ::switchToSchedule
                                                )
                                            }
                                            }
                                        }
                                    }
                                },
                                bottomBar = {
                                    NavigationBar {
                                        TabItem.values().forEach { tab ->
                                            NavigationBarItem(
                                                icon = { Icon(tab.icon, contentDescription = tab.title) },
                                                label = { Text(tab.title) },
                                                selected = currentTab == tab,
                                                onClick = { currentTab = tab },
                                                alwaysShowLabel = true,
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = Color(0xFF3B82F6),
                                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    selectedTextColor = Color(0xFF3B82F6),
                                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            ) { paddingValues ->
                                when (currentTab) {
                                    TabItem.CourseTable -> {
                                        val coroutineScope = rememberCoroutineScope()
                                        var dragOffset by remember { mutableFloatStateOf(0f) }
                                        val slideAnim = remember { Animatable(0f) }
                                        var isAnimating by remember { mutableStateOf(false) }
                                        var animTargetWeek by remember { mutableIntStateOf(displayWeek) }
                                        var containerWidthPx by remember { mutableFloatStateOf(0f) }

                                        val translateX = remember {
                                            derivedStateOf {
                                                if (isAnimating) slideAnim.value else dragOffset
                                            }
                                        }

                                        val prevWeek = remember(displayWeek) { (displayWeek - 1).coerceAtLeast(1) }
                                        val nextWeek = remember(displayWeek) { (displayWeek + 1).coerceAtMost(20) }

                                        val prevFiltered = remember {
                                            derivedStateOf {
                                                allSchedule
                                                    .filter { it.dayOfWeek in displayDays }
                                                    .map { day -> day.copy(courses = day.courses.filter { it.weeks.contains(prevWeek) }) }
                                            }
                                        }
                                        val currentFiltered = remember {
                                            derivedStateOf {
                                                allSchedule
                                                    .filter { it.dayOfWeek in displayDays }
                                                    .map { day -> day.copy(courses = day.courses.filter { it.weeks.contains(displayWeek) }) }
                                            }
                                        }
                                        val nextFiltered = remember {
                                            derivedStateOf {
                                                allSchedule
                                                    .filter { it.dayOfWeek in displayDays }
                                                    .map { day -> day.copy(courses = day.courses.filter { it.weeks.contains(nextWeek) }) }
                                            }
                                        }

                                        val scheduleLeft = remember {
                                            derivedStateOf {
                                                if (animTargetWeek < displayWeek) prevFiltered.value else currentFiltered.value
                                            }
                                        }
                                        val scheduleRight = remember {
                                            derivedStateOf {
                                                if (animTargetWeek > displayWeek) nextFiltered.value else currentFiltered.value
                                            }
                                        }
                                        val dateLeft = remember(displayWeek, animTargetWeek, customStartDate) {
                                            derivedStateOf {
                                                if (animTargetWeek < displayWeek) SemesterCalculator.getDateForWeek(displayWeek - 1, customStartDate)
                                                else SemesterCalculator.getDateForWeek(displayWeek, customStartDate)
                                            }
                                        }
                                        val dateRight = remember(displayWeek, animTargetWeek, customStartDate) {
                                            derivedStateOf {
                                                if (animTargetWeek > displayWeek) SemesterCalculator.getDateForWeek(displayWeek + 1, customStartDate)
                                                else SemesterCalculator.getDateForWeek(displayWeek, customStartDate)
                                            }
                                        }

                                        val displayDateForTable = remember(displayWeek, customStartDate) {
                                            SemesterCalculator.getDateForWeek(displayWeek, customStartDate)
                                        }

                                        val containerWidthDp = with(LocalDensity.current) { containerWidthPx.toDp() }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(paddingValues)
                                                .padding(8.dp)
                                                .pointerInput(Unit) {
                                                    detectHorizontalDragGestures(
                                                        onDragEnd = {
                                                            if (isAnimating) return@detectHorizontalDragGestures
                                                            coroutineScope.launch {
                                                                val threshold = containerWidthPx * 0.25f
                                                                if (abs(dragOffset) > threshold && dragOffset != 0f) {
                                                                    isAnimating = true
                                                                    val toRight = dragOffset > 0
                                                                    val targetWeek = if (toRight)
                                                                        (displayWeek - 1).coerceAtLeast(1)
                                                                    else
                                                                        (displayWeek + 1).coerceAtMost(20)
                                                                    
                                                                    if (targetWeek != displayWeek) {
                                                                        animTargetWeek = targetWeek
                                                                        val targetOffset = if (toRight) containerWidthPx else -containerWidthPx

                                                                        slideAnim.snapTo(dragOffset)
                                                                        slideAnim.animateTo(targetOffset, tween(250, easing = FastOutSlowInEasing))
                                                                        
                                                                        displayWeek = targetWeek
                                                                        animTargetWeek = displayWeek
                                                                        slideAnim.snapTo(0f)
                                                                    }

                                                                    isAnimating = false
                                                                    dragOffset = 0f
                                                                } else {
                                                                    coroutineScope.launch {
                                                                        slideAnim.animateTo(0f, tween(150, easing = EaseOut))
                                                                    }
                                                                    dragOffset = 0f
                                                                }
                                                            }
                                                        }
                                                    ) { change, dragAmount ->
                                                        change.consume()
                                                        if (!isAnimating) {
                                                            dragOffset += dragAmount
                                                        }
                                                    }
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clipToBounds()
                                                    .onSizeChanged { containerWidthPx = it.width.toFloat() }
                                                    .graphicsLayer {
                                                        translationX = translateX.value
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Box(modifier = Modifier.width(containerWidthDp)) {
                                                        ScheduleTable(
                                                            schedule = scheduleLeft.value,
                                                            periods = periods,
                                                            onCourseClick = ::handleCourseClick,
                                                            onSelectionComplete = ::handleSelectionComplete,
                                                            selectionArea = selectionArea.value,
                                                            onSelectionChange = ::handleSelectionChange,
                                                            currentDate = dateLeft.value,
                                                            courseHeight = settingsManager.getCourseHeight(),
                                                            fontSize = settingsManager.getFontSize()
                                                        )
                                                    }
                                                    Box(modifier = Modifier.width(containerWidthDp)) {
                                                        ScheduleTable(
                                                            schedule = scheduleRight.value,
                                                            periods = periods,
                                                            onCourseClick = ::handleCourseClick,
                                                            onSelectionComplete = ::handleSelectionComplete,
                                                            selectionArea = selectionArea.value,
                                                            onSelectionChange = ::handleSelectionChange,
                                                            currentDate = dateRight.value,
                                                            courseHeight = settingsManager.getCourseHeight(),
                                                            fontSize = settingsManager.getFontSize()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    TabItem.Schedule -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(paddingValues)
                                        ) {
                                            TodayScheduleView(
                                                allCourses = allCourses.value,
                                                periods = periods,
                                                currentDate = displayDate,
                                                onCourseClick = ::handleCourseClick,
                                                fontSize = settingsManager.getFontSize()
                                            )
                                        }
                                    }
                                    TabItem.Study -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(paddingValues),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "学习",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    TabItem.Profile -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(paddingValues),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "我的",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                selectedCourse.value?.let { course ->
                                    CourseDetailDialog(
                                        course = course,
                                        periods = periods,
                                        onDismiss = {
                                            selectedCourse.value = null
                                        },
                                        onDelete = {
                                            handleDeleteCourse(course)
                                        },
                                        onEdit = {
                                            handleEditCourse(course)
                                        },
                                        onCopy = {
                                            handleCopyCourse(course)
                                        }
                                    )
                                }

                                if (showImportDialog) {
                                    ImportJsonDialog(
                                        onImport = { courses ->
                                            handleImport(courses)
                                        },
                                        onDismiss = {
                                            showImportDialog = false
                                        }
                                    )
                                }

                                if (showNewScheduleDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showNewScheduleDialog = false },
                                        title = {
                                            Text(
                                                text = "新建课表",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        },
                                        text = {
                                            OutlinedTextField(
                                                value = newScheduleName,
                                                onValueChange = { newScheduleName = it },
                                                label = { Text("请输入课表名称") },
                                                placeholder = { Text("例如：大二下学期") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    if (newScheduleName.isNotBlank()) {
                                                        createNewSchedule(newScheduleName.trim())
                                                        showNewScheduleDialog = false
                                                    }
                                                },
                                                enabled = newScheduleName.isNotBlank()
                                            ) {
                                                Text("创建", color = Color(0xFF3B82F6))
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showNewScheduleDialog = false }) {
                                                Text("取消")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Screen.Settings -> {
                            SettingsScreen(
                                semesterStartDate = semesterStartDate,
                                onSave = { newSemesterStartDate ->
                                    settingsManager.saveSemesterStartDate(newSemesterStartDate)
                                    semesterStartDate = newSemesterStartDate
                                    displayWeek = SemesterCalculator.getCurrentWeek()
                                    currentScreen = Screen.Schedule
                                },
                                onBack = { currentScreen = Screen.Schedule }
                            )
                        }
                        Screen.GlobalSettings -> {
                            GlobalSettingsScreen(
                                courseHeight = settingsManager.getCourseHeight(),
                                fontSize = settingsManager.getFontSize(),
                                themeMode = settingsManager.getThemeMode(),
                                onSave = { newCourseHeight, newFontSize, newThemeMode ->
                                    settingsManager.saveCourseHeight(newCourseHeight)
                                    settingsManager.saveFontSize(newFontSize)
                                    settingsManager.saveThemeMode(newThemeMode)
                                    themeMode.value = newThemeMode
                                },
                                onBack = { currentScreen = Screen.Schedule }
                            )
                        }
                        Screen.PeriodSettings -> {
                            PeriodSettingsScreen(
                                periods = periods,
                                displayDays = displayDays,
                                displayMode = displayMode,
                                onSave = { newPeriods, newDisplayDays, newDisplayMode ->
                                    settingsManager.saveCustomPeriods(newPeriods)
                                    settingsManager.saveDisplayDays(newDisplayDays)
                                    settingsManager.saveDisplayMode(newDisplayMode.name)
                                    periods = newPeriods
                                    displayDays = newDisplayDays
                                    displayMode = newDisplayMode
                                    currentScreen = Screen.Schedule
                                },
                                onReset = {
                                    settingsManager.resetToDefault()
                                    periods = settingsManager.getCustomPeriods()
                                    displayDays = settingsManager.getDisplayDays()
                                    displayMode = WeekDisplayMode.valueOf(settingsManager.getDisplayMode())
                                    currentScreen = Screen.Schedule
                                },
                                onBack = { currentScreen = Screen.Schedule }
                            )
                        }
                        Screen.AddCourse -> {
                            AddCourseScreen(
                                periods = periods,
                                prefilledTimeSlot = prefilledTimeSlot.value,
                                allExistingCourses = allSchedule.flatMap { it.courses },
                                totalWeeks = 20,
                                onSave = ::handleSaveCourse,
                                onBack = {
                                    prefilledTimeSlot.value = null
                                    currentScreen = Screen.Schedule
                                }
                            )
                        }
                        Screen.CourseManage -> {
                            CourseManageScreen(
                                allCourses = allSchedule.flatMap { it.courses },
                                onDeleteCourse = ::handleDeleteCourse,
                                onEditCourse = ::handleEditCourse,
                                onClearAll = ::handleClearAllCourses,
                                onBack = { currentScreen = Screen.Schedule }
                            )
                        }
                        Screen.ScheduleManage -> {
                            ScheduleManageScreen(
                                schedules = allSchedules,
                                currentScheduleId = currentScheduleId,
                                onSelectSchedule = { id ->
                                    switchToSchedule(id)
                                    currentScreen = Screen.Schedule
                                },
                                onDeleteSchedule = ::deleteSchedule,
                                onRenameSchedule = ::renameSchedule,
                                onBack = { currentScreen = Screen.Schedule }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class Screen {
    Schedule,
    Settings,
    GlobalSettings,
    PeriodSettings,
    AddCourse,
    CourseManage,
    ScheduleManage
}

enum class TabItem(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String) {
    Schedule(Icons.Default.CalendarMonth, "日程"),
    CourseTable(Icons.Default.Grid3x3, "课表"),
    Study(Icons.Default.Book, "学习"),
    Profile(Icons.Default.Person, "我的")
}
