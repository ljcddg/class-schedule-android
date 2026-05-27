package com.apesource.lession2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.apesource.lession2.data.Course
import com.apesource.lession2.data.CourseJsonUtil

@Composable
fun ImportJsonDialog(
    onImport: (List<Course>) -> Unit,
    onDismiss: () -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "分享口令导入",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "请将好友分享的课程口令粘贴到下方输入框中",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = {
                        jsonText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = {
                        Text(
                            text = "在此粘贴 JSON 格式的课程数据...",
                            fontSize = 13.sp
                        )
                    },
                    shape = RoundedCornerShape(10.dp)
                )

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            if (jsonText.isBlank()) {
                                errorMessage = "请输入课程数据"
                                return@Button
                            }
                            when (val result = CourseJsonUtil.importFromJson(jsonText)) {
                                is CourseJsonUtil.ImportResult.Success -> {
                                    if (result.courses.isEmpty()) {
                                        errorMessage = "未解析到任何课程"
                                    } else {
                                        onImport(result.courses)
                                        onDismiss()
                                    }
                                }
                                is CourseJsonUtil.ImportResult.Error -> {
                                    errorMessage = "解析失败：${result.message}"
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("确认导入")
                    }
                }
            }
        }
    }
}
