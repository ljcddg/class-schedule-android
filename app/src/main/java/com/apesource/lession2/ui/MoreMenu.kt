package com.apesource.lession2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import android.widget.Toast
import com.apesource.lession2.data.UserSchedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenu(
    currentWeek: Int = 1,
    onWeekChange: (Int) -> Unit = {},
    onOpenTermSettings: () -> Unit = {},
    onOpenPeriodSettings: () -> Unit = {},
    onOpenGlobalSettings: () -> Unit = {},
    onOpenCourseManage: () -> Unit = {},
    onNewSchedule: () -> Unit = {},
    onOpenScheduleManage: () -> Unit = {},
    schedules: List<UserSchedule> = emptyList(),
    currentScheduleId: String? = null,
    onSwitchSchedule: (String) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var sliderWeek by remember(currentWeek) { mutableStateOf(currentWeek.toFloat()) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        if (isExpanded) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { isExpanded = false }
            ) {
                Column(
                    modifier = Modifier
                        .width(340.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "周数",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "修改当前周",
                            fontSize = 14.sp,
                            color = Color(0xFF3B82F6)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${sliderWeek.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.width(32.dp)
                        )
                        Slider(
                            value = sliderWeek,
                            onValueChange = { sliderWeek = it },
                            onValueChangeFinished = { onWeekChange(sliderWeek.toInt()) },
                            valueRange = 1f..20f,
                            steps = 18,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6),
                                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "课表",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "新建课表",
                                fontSize = 14.sp,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.clickable {
                                    isExpanded = false
                                    onNewSchedule()
                                }
                            )
                            Text(
                                text = "管理",
                                fontSize = 14.sp,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.clickable {
                                    isExpanded = false
                                    onOpenScheduleManage()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (schedules.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(schedules) { schedule ->
                                val isActive = schedule.id == currentScheduleId
                                val courseCount = schedule.schedule.sumOf { it.courses.size }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isActive) Color(0xFFEBF5FF)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            onSwitchSchedule(schedule.id)
                                            isExpanded = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isActive) Color(0xFF3B82F6)
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isActive) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                text = schedule.name.take(1),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = schedule.name,
                                            fontSize = 14.sp,
                                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$courseCount 门课程",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isActive) {
                                        Text(
                                            text = "当前",
                                            fontSize = 11.sp,
                                            color = Color(0xFF3B82F6),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionItem(
                            icon = Icons.Default.Schedule,
                            title = "上课时间",
                            onClick = {
                                isExpanded = false
                                onOpenTermSettings()
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Default.TableRows,
                            title = "课表设置",
                            onClick = {
                                isExpanded = false
                                onOpenPeriodSettings()
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Default.Checklist,
                            title = "已添课程",
                            onClick = {
                                isExpanded = false
                                onOpenCourseManage()
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Default.HelpOutline,
                            title = "常见问题",
                            onClick = {
                                Toast.makeText(context, "常见问题功能开发中", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionItem(
                            icon = Icons.Default.Info,
                            title = "关于",
                            onClick = {
                                Toast.makeText(context, "关于功能开发中", Toast.LENGTH_SHORT).show()
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Default.Chat,
                            title = "联系我们",
                            onClick = {
                                Toast.makeText(context, "联系我们功能开发中", Toast.LENGTH_SHORT).show()
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Default.Settings,
                            title = "全局设置",
                            onClick = {
                                isExpanded = false
                                onOpenGlobalSettings()
                            }
                        )
                        QuickActionItem(
                            icon = Icons.Default.ScheduleSend,
                            title = "课程时钟",
                            onClick = {
                                Toast.makeText(context, "课程时钟功能开发中", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
