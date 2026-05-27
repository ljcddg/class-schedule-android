package com.apesource.lession2.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun ImportMenu(
    onShare: () -> Unit = {},
    onShareCodeImport: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
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
                imageVector = Icons.Default.Download,
                contentDescription = "导入",
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
                        .width(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    ImportMenuItem(
                        icon = Icons.Default.Send,
                        title = "分享课程",
                        onClick = {
                            onShare()
                            isExpanded = false
                        }
                    )
                    ImportMenuItem(
                        icon = Icons.Default.Key,
                        title = "分享口令导入",
                        onClick = {
                            onShareCodeImport()
                            isExpanded = false
                        }
                    )
                    ImportMenuItem(
                        icon = Icons.Default.Code,
                        title = "JSON导入模板",
                        onClick = {
                            showTemplateDialog = true
                            isExpanded = false
                        }
                    )
                    ImportMenuItem(
                        icon = Icons.Default.School,
                        title = "从教务导入",
                        onClick = {
                            Toast.makeText(context, "从教务导入功能开发中", Toast.LENGTH_SHORT).show()
                            isExpanded = false
                        }
                    )
                    ImportMenuItem(
                        icon = Icons.Default.Grid3x3,
                        title = "Excel导入",
                        onClick = {
                            Toast.makeText(context, "Excel导入功能开发中", Toast.LENGTH_SHORT).show()
                            isExpanded = false
                        }
                    )
                    ImportMenuItem(
                        icon = Icons.Default.Code,
                        title = "HTML导入",
                        onClick = {
                            Toast.makeText(context, "HTML导入功能开发中", Toast.LENGTH_SHORT).show()
                            isExpanded = false
                        }
                    )
                    ImportMenuItem(
                        icon = Icons.Default.Backup,
                        title = "从备份导入",
                        onClick = {
                            Toast.makeText(context, "从备份导入功能开发中", Toast.LENGTH_SHORT).show()
                            isExpanded = false
                        },
                        isLast = true
                    )
                }
            }
        }

        if (showTemplateDialog) {
            ImportTemplateDialog(
                onDismiss = { showTemplateDialog = false }
            )
        }
    }
}

@Composable
fun ImportMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun ImportTemplateDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val templateJson = """{
  "version": 1,
  "courses": [
    {
      "name": "课程名称",
      "teacher": "教师姓名",
      "location": "上课地点",
      "dayOfWeek": 1,
      "startPeriod": 1,
      "endPeriod": 2,
      "color": "#FF6366F1",
      "credits": "4",
      "note": "备注信息",
      "weeks": [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20]
    }
  ]
}"""

    var showCopyToast by remember { mutableStateOf(false) }

    if (showCopyToast) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showCopyToast = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF6366F1))
        },
        title = {
            Text(
                text = "AI 图片识别导入",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEEF2FF)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📷", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "第1步：拍摄课表照片",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4338CA)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0FDF4)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📋", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "第2步：将下方模板 + 照片一起发给 AI",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "告诉 AI：「请按照模板格式，识别图片中的课程表，生成 JSON 数据」",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF166534).copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF7ED)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📥", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "第3步：复制 AI 返回的 JSON",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9A3412)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "回到「分享口令导入」，粘贴 JSON 即可一键导入全部课程",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9A3412).copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "JSON 模板（每门课程只需以下字段）：",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "name=课程名  teacher=教师  location=地点\n" +
                           "dayOfWeek=1~7(周一~周日)  startPeriod/endPeriod=节次\n" +
                           "color=#RRGGBB  credits=学分  weeks=周次数组",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

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

                if (showCopyToast) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ 模板已复制，去粘贴给 AI 吧！",
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("课程JSON模板", templateJson)
                    clipboard.setPrimaryClip(clip)
                    showCopyToast = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("复制模板 + 发给 AI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
