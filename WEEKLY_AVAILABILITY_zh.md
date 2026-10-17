# 学生每周可用时间

## 学生填写

1. 登录申请人账号，打开侧栏 **Weekly Availability**，或从 Profile 页进入。
2. 选择申请周期。只有已开放且在起止时间范围内的周期允许修改。
3. 使用 **Add available time** 添加每周可工作时段。周一至周日均可，每天可添加多个时段。
4. 使用 **Add conflict** 记录上课、其他工作等已知冲突，可填写原因。
5. 填写其他限制或备注，点击 **Save availability**。

所有星期和时间使用 UCI 所在的 `America/Los_Angeles` 时区，跟随该时区的夏令时规则。
时间按英文界面的 24 小时 `HH:mm` 格式填写，如 `09:00`、`17:30`。
这是一份每周重复的安排，不是某一天的具体日期。

每个时段必须在同一天内，结束晚于开始；跨午夜请拆成两天。
结束时间可以填写 `24:00`。同类时段不能重复或重叠，相邻时段允许相接。
可工作时段也不能覆盖已知冲突；例如 09:00–17:00 中间有 12:00–13:00 的课程，
应填写 09:00–12:00 和 13:00–17:00 两个可工作时段。

若该周期完全没有可工作时间，删除所有可工作时段后勾选
**I have no available working times this period**，再保存。
明确声明“无可用时间”与“从未填写”是两种不同状态。

草稿周期不向学生提供填写入口；关闭、尚未开始或已截止的周期可查看已保存记录，但不能修改。
每个周期分别保存，不会从另一个周期自动复制。

## 提交与快照

提交职位申请前，必须先保存该周期的安排（也可以明确声明无可用时间）。
申请弹窗会展示已保存的可用时间、冲突和备注，并提供编辑入口。
真正提交时，后端从数据库复制当前学生、当前周期的安排作为该申请的独立快照。
客户端不能通过提交伪造的快照来替换服务器记录。

之后修改 Weekly Availability，只影响未来提交的申请，不会更改先前申请。
学生可在 **Application Tracker → Submitted availability** 查看自己提交时的安排。
本轮没有新增修改已提交快照的流程。

## 工作人员查看

- Organiser：在 Pipeline 中打开 **View Full Application**，查看 **Weekly availability · submitted snapshot**。
- 管理员：在 **Application Periods → View applications → Submitted schedule** 查看。

工作人员始终查看申请自身的快照，不会用学生最新填写的内容替换历史快照。
Organiser 仍然只能查看自己有权审核的职位申请。
旧申请不会补造时间安排，页面显示 **No weekly availability was recorded for this application.**

## 数据与 API

启动时自动执行 V3 数据库迁移，增加 `weekly_availabilities` 表和
`applications.availability_snapshot` JSONB 字段。已有申请 ID、周期和审核状态保留。
升级前备份位于 `backups/tamo-before-weekly-availability.dump`。

接口前缀为 `/tamo/api`，均需要 Applicant 会话：

| 方法 | 路径 | 用途 |
|---|---|---|
| GET | `/applicant/availability-periods` | 获取已发布的非历史周期及是否可编辑 |
| GET | `/applicant/availability/{periodId}` | 读取当前登录学生在指定周期的安排 |
| PUT | `/applicant/availability/{periodId}` | 保存当前登录学生的安排 |

保存请求示例：

```json
{
  "timeZone": "America/Los_Angeles",
  "availableSlots": [
    { "dayOfWeek": 1, "startTime": "09:00", "endTime": "12:00" },
    { "dayOfWeek": 1, "startTime": "13:00", "endTime": "17:00" }
  ],
  "conflictSlots": [
    { "dayOfWeek": 1, "startTime": "12:00", "endTime": "13:00", "reason": "Class" }
  ],
  "noAvailability": false,
  "notes": "Other restrictions"
}
```

`dayOfWeek` 为 1（周一）至 7（周日）。每类最多 70 个时段，冲突原因最多 200 字符，备注最多 2000 字符。
学生身份取自登录会话，接口不接受另一位学生的身份作为修改目标。
本轮提供时间采集和快照。[课程 Section 时间表](COURSE_SECTIONS_zh.md) 已可维护；现已支持[基于提交快照的 Section 时间匹配](SECTION_MATCHING_zh.md)。
