# Class Schedule Android

一款功能丰富的 Android 原生课程表应用，采用 Jetpack Compose 实现。

## ✨ 功能特性

- 📅 **周视图滑动切换**：左右滑动预览 20 周课程
- 📝 **自定义时间段**：灵活配置每节课的起止时间，支持增减节次
- 📆 **开学日期推算**：通过设定开学日期或当前周数，自动计算学期周数
- 🎨 **课程卡片**：跨时段合并展示，虚线分割，浅蓝色全局背景
- 📋 **多课表管理**：新建、切换、管理多套课程表
- 📥 **JSON 导入导出**：提供模版快速导入，支持分享口令
- 📅 **日程视图**：今日课程折叠展示已完成课程
- ⚙️ **设置**：工作日/完整周/自定义星期显示
- 🔧 **分离设置页**：
  - 「上课时间」：仅负责开学日期/周数推算
  - 「课表设置」：负责显示模式、节次编辑

## 🛠️ 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose
- **架构**：单 Activity + Composable 路由
- **持久化**：SharedPreferences
- **最低支持**：Android 8.0 (API 26)

## 📦 项目结构

```
app/src/main/java/com/apesource/lession2/
├── MainActivity.kt                    # 入口、路由、状态管理
├── data/
│   ├── CourseDataSource.kt            # 示例课程数据
│   ├── SettingsManager.kt             # SharedPreferences 封装
│   └── SemesterCalculator.kt          # 学期周数计算
├── ui/
│   ├── ScheduleTable.kt               # 课表表格组件
│   ├── TodayScheduleView.kt           # 今日日程视图
│   ├── AddCourseScreen.kt             # 添加/编辑课程
│   ├── SettingsScreen.kt              # 「上课时间」（开学日期）
│   ├── PeriodSettingsScreen.kt        # 「课表设置」（节次+显示模式）
│   ├── CourseManageScreen.kt          # 已添课程管理
│   ├── ScheduleManageScreen.kt        # 多课表管理
│   ├── MoreMenu.kt                    # 顶部更多菜单
│   └── ImportMenu.kt                  # 导入菜单
```

## 🚀 快速开始

### 构建与安装

```bash
# 克隆仓库
git clone https://github.com/ljcddg/class-schedule-android.git
cd class-schedule-android

# 用 Gradle 构建（需要 Android Studio）
./gradlew assembleDebug
```

或者直接在 Android Studio 打开项目，同步 Gradle 后构建。

## 📸 截图

<img width="1080" height="2400" alt="8379e99da5706e2e46cca288516bdb6d" src="https://github.com/user-attachments/assets/121ae7c2-b740-43f6-b9b0-c4b72bbbd560" />


## 📄 许可证

MIT License
