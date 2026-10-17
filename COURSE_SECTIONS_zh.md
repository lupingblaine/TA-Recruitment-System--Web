# 课程 Section 与 TA 需求

## 操作入口

管理员和 Organiser 登录后，在侧栏打开 **Course Sections**。
管理员可维护所有课程；Organiser 只能查看和维护当前分配给自己的课程。
若没有课程，管理员先在 Global Modules 创建课程并指定 Organiser。

每个 Section 属于一个现有课程记录，学期沿用课程的 Term。
本轮尚未新增同一课程跨学期的独立开课实例；修改课程 Term 前，应核对其所有 Section。

## 创建和编辑

1. 选择 Course，点击 **Add section**。
2. 填写 Section 名称，例如 Lecture、Lab A、Discussion 1。
3. 选择类型：**Lecture**、**Lab** 或 **Discussion**。
4. 填写 **TA positions requested** 和 **Hours per TA per week**。
5. 添加 Weekly meeting times。每条包括周几、开始时间和结束时间；同一 Section 可配置多个时段。
6. 按需填写地点和备注，点击 **Save section**。

例如 Lecture 可记录周一、周三 10:00–10:50；Lab A 可记录周二 14:00–17:00，
需要 2 名 TA，每名 TA 预计每周投入 8 小时。

所有时间使用 `America/Los_Angeles`（UCI 本地时区），以 24 小时 `HH:mm` 格式填写。
同一 Section 的时段不能重复或重叠，相邻时段允许相接。
结束时间可填写 `24:00`，跨午夜应拆成两天。
不同 Section 之间可能安排在同一时段，本轮不会自动判定课程之间或学生与课程之间的冲突。

TA 人数必须为 0–1000 的整数，每名 TA 每周工时必须在 0–168 小时范围内。
若无需 TA，人数和工时都填 0；否则两者均应为正数。
同一课程内的 Section 名称不区分大小写且不得重复，归档的名称也保留。

## 汇总与归档

列表显示 Section 类型、每周上课时间、TA 需求、地点和备注。
汇总只计算 active Section：

- **TA positions requested**：各 Section 所需 TA 人数之和。
- **Combined TA hours / week**：各 Section 的人数 × 每名 TA 工时之和。

这些是需求统计，不是已录用人数或最终分配结果。
Section 的需求与旧版 Job posting 的人数分别记录，本轮不会自动同步或合并计数。

使用 **Archive** 隐藏不再使用的 Section。勾选 **Show archived sections** 后可查看并 **Restore**。
归档不删除上课时间或 TA 需求，但不再计入需求汇总；归档后先恢复才能编辑。
已有 Section（包括归档 Section）的课程不能直接删除，以免丢失关联记录。
Section 也不能通过编辑移动到另一门课程；需要时在目标课程新建并归档旧记录。

## 权限与数据

每次查询、创建、编辑、归档和恢复都会在后端重新检查课程归属。
Organiser 无法通过伪造 moduleId 或直接请求其他 Section ID 来绕过权限。
课程转交给另一位 Organiser 后，Section 的管理权限随当前课程归属变化。
申请人没有本管理接口的读写权限。
重要操作会写入系统日志，记录操作人员、时间、Section ID 和课程 ID。

启动时自动执行 V4，增加 `course_sections` 表和课程外键、名称唯一性及需求数值约束。
现有课程、职位、申请、申请周期和时间安排快照不变，不会自动补造 Section。
本次升级前的备份为 `backups/tamo-before-course-sections.dump`。

## API

以下路径前缀为 `/tamo/api/admin/course-sections` 或 `/tamo/api/organiser/course-sections`：

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/modules` | 获取当前工作人员有权管理的课程 |
| GET | `/modules/{moduleId}/sections` | 获取课程信息、Section 列表与需求汇总 |
| POST | `/modules/{moduleId}/sections` | 创建 Section |
| PUT | `/sections/{sectionId}` | 编辑 Section |
| DELETE | `/sections/{sectionId}` | 归档 Section，可恢复 |
| POST | `/sections/{sectionId}/restore` | 恢复归档 Section |

创建和编辑请求示例：

```json
{
  "name": "Lab A",
  "type": "lab",
  "timeZone": "America/Los_Angeles",
  "meetingTimes": [
    { "dayOfWeek": 2, "startTime": "14:00", "endTime": "17:00" }
  ],
  "taPositions": 2,
  "hoursPerWeek": 8,
  "location": "Room 101",
  "notes": "Lab section"
}
```

星期为 1（周一）至 7（周日）；每个 Section 至少 1 条、最多 70 条会议时段。
类型值为 `lecture`、`lab`、`discussion`。名称最长 120 字符，地点 200 字符，备注 2000 字符。
格式或数值错误返回 400，越权返回 403，不存在返回 404，名称重复或编辑归档记录等冲突返回 409。


现已支持 [TA 正式分配](TA_ASSIGNMENTS_zh.md)。存在有效分配的 Section 需先取消分配，才能编辑或归档。
