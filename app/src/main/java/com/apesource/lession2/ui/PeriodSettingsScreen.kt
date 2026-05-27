package com.apesource.lession2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apesource.lession2.data.CourseDataSource
import com.apesource.lession2.data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EmeraldPrimary = Color(0xFF10B981)

enum class WeekDisplayMode {
    WORKDAYS,
    FULL_WEEK,
    CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSettingsScreen(
    periods: List<String>,
    displayDays: List<Int>,
    displayMode: WeekDisplayMode,
    onSave: (List<String>, List<Int>, WeekDisplayMode) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val editablePeriods = remember { periods.toMutableStateList() }
    val editableDisplayDays = remember { displayDays.toMutableStateList() }
    var currentDisplayMode by remember { mutableStateOf(displayMode) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "课表设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            onSave(
                                editablePeriods.toList(),
                                editableDisplayDays.toList(),
                                currentDisplayMode
                            )
                        },
                        modifier = Modifier.width(150.dp)
                    ) {
                        Text(text = "保存设置")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "显示设置",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                WeekDisplayModeSelector(
                    displayMode = currentDisplayMode,
                    onModeChange = { newMode ->
                        currentDisplayMode = newMode
                        when (newMode) {
                            WeekDisplayMode.WORKDAYS -> {
                                editableDisplayDays.clear()
                                editableDisplayDays.addAll(listOf(1, 2, 3, 4, 5))
                            }
                            WeekDisplayMode.FULL_WEEK -> {
                                editableDisplayDays.clear()
                                editableDisplayDays.addAll(listOf(1, 2, 3, 4, 5, 6, 7))
                            }
                            WeekDisplayMode.CUSTOM -> {}
                        }
                    }
                )

                if (currentDisplayMode == WeekDisplayMode.CUSTOM) {
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomWeekDaysSelector(
                        selectedDays = editableDisplayDays,
                        onDayToggle = { day ->
                            if (editableDisplayDays.contains(day)) {
                                editableDisplayDays.remove(day)
                            } else {
                                editableDisplayDays.add(day)
                                editableDisplayDays.sort()
                            }
                        }
                    )
                }
            }

            item {
                HorizontalDivider()
                Text(
                    text = "课程节次时间",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "点击每个节次展开后，上下滚动以调整起止时间",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            itemsIndexed(editablePeriods) { index, period ->
                PeriodEditItem(
                    index = index + 1,
                    period = period,
                    onDelete = {
                        if (editablePeriods.size > 1) {
                            editablePeriods.removeAt(index)
                        }
                    },
                    onTimeChanged = { newPeriod ->
                        editablePeriods[index] = newPeriod
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val lastPeriod = editablePeriods.lastOrNull() ?: "08:00-08:45"
                        val parts = lastPeriod.split("-")
                        val endParts = parts.getOrElse(1) { "08:45" }.split(":")
                        val endH = endParts.getOrElse(0) { "08" }.toIntOrNull() ?: 8
                        val endM = endParts.getOrElse(1) { "45" }.toIntOrNull() ?: 45
                        val newStartH = if (endM >= 50) endH + 1 else endH
                        val newStartM = (endM + 5) % 60
                        val newEndH = if (newStartM >= 50) newStartH + 1 else newStartH
                        val newEndM = (newStartM + 40) % 60
                        val newPeriod = "${newStartH.toString().padStart(2, '0')}:${newStartM.toString().padStart(2, '0')}-${newEndH.toString().padStart(2, '0')}:${newEndM.toString().padStart(2, '0')}"
                        editablePeriods.add(newPeriod)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("添加节次")
                }
            }
        }
    }
}

@Composable
fun WeekDisplayModeSelector(
    displayMode: WeekDisplayMode,
    onModeChange: (WeekDisplayMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WeekModeButton(
            text = "仅工作日",
            description = "周一至周五",
            selected = displayMode == WeekDisplayMode.WORKDAYS,
            onClick = { onModeChange(WeekDisplayMode.WORKDAYS) }
        )
        WeekModeButton(
            text = "完整一周",
            description = "周一至周日",
            selected = displayMode == WeekDisplayMode.FULL_WEEK,
            onClick = { onModeChange(WeekDisplayMode.FULL_WEEK) }
        )
        WeekModeButton(
            text = "自定义选择",
            description = "自行选择需要显示的天数",
            selected = displayMode == WeekDisplayMode.CUSTOM,
            onClick = { onModeChange(WeekDisplayMode.CUSTOM) }
        )
    }
}

@Composable
fun WeekModeButton(
    text: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CardDefaults.shape
            ),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CustomWeekDaysSelector(
    selectedDays: List<Int>,
    onDayToggle: (Int) -> Unit
) {
    Column {
        Text(
            text = "选择要显示的星期",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        CourseDataSource.weekDays.forEachIndexed { index, dayName ->
            val dayNumber = index + 1
            val isSelected = selectedDays.contains(dayNumber)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onDayToggle(dayNumber) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = isSelected, onCheckedChange = { onDayToggle(dayNumber) })
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = dayName, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun PeriodEditItem(
    index: Int,
    period: String,
    onDelete: () -> Unit,
    onTimeChanged: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val parts = period.split("-")
    val startParts = parts[0].split(":")
    val endParts = parts[1].split(":")
    val startHour = startParts[0].toIntOrNull() ?: 8
    val startMin = startParts[1].toIntOrNull() ?: 0
    val endHour = endParts[0].toIntOrNull() ?: 9
    val endMin = endParts[1].toIntOrNull() ?: 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第${index}节",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(60.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = period,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isExpanded) "收起" else "点击修改",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "删除节次", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }

            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Text("开始时间", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF10B981))
                        Text("结束时间", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFF59E0B))
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    var localStartH by remember { mutableIntStateOf(startHour) }
                    var localStartM by remember { mutableIntStateOf(startMin) }
                    var localEndH by remember { mutableIntStateOf(endHour) }
                    var localEndM by remember { mutableIntStateOf(endMin) }

                    Row(modifier = Modifier.fillMaxWidth().height(180.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        MiniWheelPicker2(
                            items = (0..23).map { it.toString().padStart(2, '0') },
                            selectedIndex = localStartH,
                            onSelected = { localStartH = it; onTimeChanged(formatTime(localStartH, localStartM, localEndH, localEndM)) },
                            modifier = Modifier.width(60.dp)
                        )
                        Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                        MiniWheelPicker2(
                            items = listOf("00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"),
                            selectedIndex = localStartM / 5,
                            onSelected = { localStartM = it * 5; onTimeChanged(formatTime(localStartH, localStartM, localEndH, localEndM)) },
                            modifier = Modifier.width(60.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("—", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(modifier = Modifier.width(16.dp))
                        MiniWheelPicker2(
                            items = (0..23).map { it.toString().padStart(2, '0') },
                            selectedIndex = localEndH,
                            onSelected = { localEndH = it; onTimeChanged(formatTime(localStartH, localStartM, localEndH, localEndM)) },
                            modifier = Modifier.width(60.dp)
                        )
                        Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterVertically))
                        MiniWheelPicker2(
                            items = listOf("00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"),
                            selectedIndex = localEndM / 5,
                            onSelected = { localEndM = it * 5; onTimeChanged(formatTime(localStartH, localStartM, localEndH, localEndM)) },
                            modifier = Modifier.width(60.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(sh: Int, sm: Int, eh: Int, em: Int): String {
    return "${sh.toString().padStart(2, '0')}:${sm.toString().padStart(2, '0')}-${eh.toString().padStart(2, '0')}:${em.toString().padStart(2, '0')}"
}

@Composable
fun MiniWheelPicker2(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeightDp = 40.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeightDp.toPx() }
    val boxHeightDp = 160.dp
    val verticalPaddingDp = (boxHeightDp - itemHeightDp) / 2
    val repeatFactor = 50
    val totalCount = items.size * repeatFactor
    val startIdx = selectedIndex.coerceIn(0, items.lastIndex) + items.size * (repeatFactor / 2)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIdx)
    val coroutineScope = rememberCoroutineScope()

    var centerIndex by remember { mutableIntStateOf(startIdx % items.size) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            delay(60)
            val offset = listState.firstVisibleItemScrollOffset
            val firstVisible = listState.firstVisibleItemIndex
            val nearest = if (offset > itemHeightPx / 2) firstVisible + 1 else firstVisible
            val nearestClamped = nearest.coerceIn(0, totalCount - 1)
            val realIdx = nearestClamped % items.size
            if (firstVisible != nearestClamped || (firstVisible == nearestClamped && offset > 0)) {
                coroutineScope.launch { listState.animateScrollToItem(nearestClamped) }
            }
            centerIndex = realIdx
            if (selectedIndex != realIdx) {
                onSelected(realIdx)
            }
        }
    }

    Box(
        modifier = modifier
            .height(boxHeightDp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize().drawBehind {
            val h = size.height / 2f
            val highlightTop = h - itemHeightPx / 2f
            drawRect(
                color = EmeraldPrimary.copy(alpha = 0.10f),
                topLeft = androidx.compose.ui.geometry.Offset(0f, highlightTop),
                size = androidx.compose.ui.geometry.Size(size.width, itemHeightPx)
            )
            val dashW = 6.dp.toPx()
            val dashG = 3.dp.toPx()
            drawLine(
                color = EmeraldPrimary.copy(alpha = 0.30f),
                start = androidx.compose.ui.geometry.Offset(4.dp.toPx(), highlightTop),
                end = androidx.compose.ui.geometry.Offset(size.width - 4.dp.toPx(), highlightTop),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashW, dashG), 0f)
            )
            drawLine(
                color = EmeraldPrimary.copy(alpha = 0.30f),
                start = androidx.compose.ui.geometry.Offset(4.dp.toPx(), highlightTop + itemHeightPx),
                end = androidx.compose.ui.geometry.Offset(size.width - 4.dp.toPx(), highlightTop + itemHeightPx),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashW, dashG), 0f)
            )
        })

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = verticalPaddingDp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(totalCount) { idx ->
                val realIdx = idx % items.size
                val isCenter = realIdx == centerIndex
                Box(modifier = Modifier.fillMaxWidth().height(itemHeightDp), contentAlignment = Alignment.Center) {
                    Text(
                        text = items[realIdx],
                        fontWeight = if (isCenter) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = if (isCenter) 18.sp else 13.sp,
                        color = if (isCenter) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
