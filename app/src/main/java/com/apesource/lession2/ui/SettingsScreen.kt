package com.apesource.lession2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.apesource.lession2.data.SemesterCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EmeraldPrimary = Color(0xFF10B981)
private val RoseAccent = Color(0xFFF43F5E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    semesterStartDate: String = "",
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var currentSemesterStartDate by remember { mutableStateOf(semesterStartDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "上课时间") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(modifier = Modifier.height(80.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onSave(currentSemesterStartDate) },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text(text = "保存设置")
                    }
                }
            }
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
                    text = "通过开学日期或当前周数计算学期",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            item {
                SemesterDatePicker(
                    currentDate = currentSemesterStartDate,
                    onDateChanged = { currentSemesterStartDate = it }
                )
            }
        }
    }
}

@Composable
fun SemesterDatePicker(
    currentDate: String,
    onDateChanged: (String) -> Unit
) {
    val parsed = SemesterCalculator.parseDate(currentDate)
    var year by remember(currentDate) {
        mutableIntStateOf(parsed?.year ?: java.time.LocalDate.now().year)
    }
    var month by remember(currentDate) {
        mutableIntStateOf(parsed?.monthValue ?: 1)
    }
    var day by remember(currentDate) {
        mutableIntStateOf(parsed?.dayOfMonth ?: 1)
    }

    val hasCustomDate = currentDate.isNotBlank()
    val context = LocalContext.current

    LaunchedEffect(year, month, day) {
        val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
        val clampedDay = day.coerceIn(1, daysInMonth)
        val newDate = String.format("%04d-%02d-%02d", year, month, clampedDay)
        if (newDate != currentDate) {
            onDateChanged(newDate)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "开学日期",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (hasCustomDate) "已自定义" else "上下滚动选择，留空则自动推算",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (hasCustomDate) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (hasCustomDate) {
                    TextButton(onClick = {
                        onDateChanged("")
                        Toast.makeText(context, "已清除，恢复自动推算", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重置", color = RoseAccent, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val selectedDate = String.format("%04d-%02d-%02d", year, month, day)
                    Text(
                        text = selectedDate,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    Text(
                        text = "当前第 ${SemesterCalculator.calculateWeekFromStart(selectedDate)} 周",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val pickerModifier = Modifier.weight(1f).fillMaxHeight()
                val labelStyle = Modifier.padding(bottom = 6.dp)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "年",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = labelStyle
                    )
                    MiniWheelPicker(
                        items = (2020..2030).map { it.toString() },
                        selectedIndex = year - 2020,
                        onSelected = { year = it + 2020 },
                        modifier = pickerModifier
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "月",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = labelStyle
                    )
                    MiniWheelPicker(
                        items = (1..12).map { it.toString().padStart(2, '0') },
                        selectedIndex = month - 1,
                        onSelected = { month = it + 1 },
                        modifier = pickerModifier
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "日",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = labelStyle
                    )
                    val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
                    MiniWheelPicker(
                        items = (1..daysInMonth).map { it.toString().padStart(2, '0') },
                        selectedIndex = day.coerceIn(1, daysInMonth) - 1,
                        onSelected = { day = it + 1 },
                        modifier = pickerModifier
                    )
                }
            }
        }
    }
}

@Composable
fun MiniWheelPicker(
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
            delay(120)
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val nearest = if (offset >= itemHeightPx / 2) firstVisible + 1 else firstVisible
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        // 高亮条覆盖整行宽度
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val highlightCenter = size.height / 2f
                    val highlightTop = highlightCenter - itemHeightPx / 2f

                    drawRect(
                        color = EmeraldPrimary.copy(alpha = 0.10f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, highlightTop),
                        size = androidx.compose.ui.geometry.Size(size.width, itemHeightPx)
                    )

                    val dashW = 6.dp.toPx()
                    val dashG = 3.dp.toPx()

                    drawLine(
                        color = EmeraldPrimary.copy(alpha = 0.30f),
                        start = androidx.compose.ui.geometry.Offset(0f, highlightTop),
                        end = androidx.compose.ui.geometry.Offset(size.width, highlightTop),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashW, dashG), 0f)
                    )
                    drawLine(
                        color = EmeraldPrimary.copy(alpha = 0.30f),
                        start = androidx.compose.ui.geometry.Offset(0f, highlightTop + itemHeightPx),
                        end = androidx.compose.ui.geometry.Offset(size.width, highlightTop + itemHeightPx),
                        strokeWidth = 1.dp.toPx(),
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
            items(totalCount) { idx ->
                val realIdx = idx % items.size
                val isCenter = realIdx == centerIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[realIdx],
                        fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                        fontSize = if (isCenter) 17.sp else 13.sp,
                        color = if (isCenter) EmeraldPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
